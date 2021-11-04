package net.cladium.effect;

import lombok.Getter;
import net.cladium.core.CladiumPlugin;
import net.cladium.effect.listener.GlideListener;
import net.cladium.effect.task.ParticleTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class EffectManager {

    @Getter
    private FileConfiguration config;

    @Getter
    private final List<Effect> effects;

    public EffectManager() {
        effects = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(new GlideListener(), CladiumPlugin.getInstance());
        Bukkit.getScheduler().runTaskTimerAsynchronously(CladiumPlugin.getInstance(), new ParticleTask(), 0, 1);
    }

    public Effect get(String key) {
        return effects.stream().filter(color -> color.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    public boolean load() {
        try {
            File file = new File(CladiumPlugin.getInstance().getDataFolder(), "effects.yml");
            if (!file.exists())
                CladiumPlugin.getInstance().saveResource("effects.yml", false);
            config = YamlConfiguration.loadConfiguration(file);
            return file.exists();
        } catch (Exception e) {
            CladiumPlugin.getInstance().getLogger().log(Level.WARNING, "Couldn't load/create effects.yml", e);
            return false;
        }
    }

    public void reload() {
        if (load()) {
            effects.clear();
            loadEffects();
        }
    }

    public void loadEffects() {
        if (config == null)
            return;

        ConfigurationSection section = config.getConfigurationSection("effects");
        if (section == null)
            return;

        for (String key : section.getKeys(false)) {
            String path = key + ".";
            if (!section.contains(path + "onTick"))
                continue;
            Effect effect = new Effect(
                    key,
                    ChatColor.translateAlternateColorCodes('&', section.getString(path + "displayName", key)),
                    section.getString(path + "armorColor"),
                    section.getString(path + "permission"),
                    section.getStringList(path + "onTick.do"),
                    section.getStringList(path + "onTick.doPost"));

            if (section.contains(path + "variables")) {
                for (String var : section.getStringList(path + "variables"))
                    effect.getVariables().add(var);
            }

            effect.setInterval(section.getInt(path + "onTick.interval", 1));
            effect.setTimes(section.getInt(path + "onTick.times", 1));

            effects.add(effect);
        }
    }
}