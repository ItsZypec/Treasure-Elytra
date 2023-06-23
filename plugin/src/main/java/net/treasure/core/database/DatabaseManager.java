package net.treasure.core.database;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.treasure.core.TreasurePlugin;
import net.treasure.core.database.impl.MySQL;
import net.treasure.core.database.impl.SQLite;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.function.Function;

public class DatabaseManager {

    public static String TYPE, TABLE;

    @Getter
    @Accessors(fluent = true)
    private Database instance;
    private final HashMap<String, Function<FileConfiguration, Database>> validDatabases;

    public DatabaseManager() {
        this.validDatabases = new HashMap<>();
        validDatabases.put("sqlite", config -> new SQLite());
        validDatabases.put("mysql", config -> new MySQL(config.getString("database.address"), config.getString("database.port", "3306"), config.getString("database.name"), config.getString("database.user"), config.getString("database.password")));
    }

    public boolean initialize(TreasurePlugin plugin) {
        var config = plugin.getConfig();

        if (!config.contains("database")) {
            plugin.getLogger().warning("Couldn't find database section in the config.yml");
            return false;
        }

        var type = config.getString("database.type");
        if (type == null)
            type = "sqlite";

        var applier = validDatabases.get(type);

        if (applier == null) {
            plugin.getLogger().warning("Invalid database type: " + type);
            return false;
        }

        this.instance = applier.apply(config);
        if (this.instance == null) {
            plugin.getLogger().warning("Couldn't initialize the database");
            return false;
        }

        if (!instance.connect()) {
            plugin.getLogger().warning("Couldn't connect to the database");
            return false;
        }

        TYPE = type;
        TABLE = config.getString("database.table", "trparticles_data");

        instance.update("CREATE TABLE IF NOT EXISTS `" + TABLE + "` (`uuid` varchar(100) NOT NULL, `data` BLOB NOT NULL, PRIMARY KEY(`uuid`))");
        return true;
    }

    public void close() {
        if (instance != null)
            instance.close();
    }
}