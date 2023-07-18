package net.treasure.gui;

import lombok.Getter;
import net.treasure.TreasureParticles;
import net.treasure.configuration.ConfigurationGenerator;
import net.treasure.configuration.DataHolder;
import net.treasure.gui.config.GUIElements;
import net.treasure.gui.config.GUILayout;
import net.treasure.gui.config.GUISounds;
import net.treasure.gui.config.GUIStyle;
import net.treasure.gui.listener.GUIListener;
import net.treasure.gui.task.GUITask;
import net.treasure.gui.type.GUI;
import net.treasure.gui.type.admin.AdminGUI;
import net.treasure.gui.type.color.ColorsGUI;
import net.treasure.gui.type.effects.EffectsGUI;
import net.treasure.gui.type.mixer.MixerGUI;
import net.treasure.gui.type.mixer.effect.TickHandlersGUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class GUIManager implements DataHolder {

    public static final String VERSION = "1.1.0";

    final ConfigurationGenerator generator;
    YamlConfiguration config;

    final GUIElements elements = new GUIElements();
    final GUISounds sounds = new GUISounds();
    GUIStyle style;

    int taskId = -5, interval = 2;
    float colorCycleSpeed = 0.85f;

    public GUIManager() {
        this.generator = new ConfigurationGenerator("gui.yml");
        Bukkit.getPluginManager().registerEvents(new GUIListener(), TreasureParticles.getPlugin());
        EffectsGUI.configure(this);
        ColorsGUI.configure(this);
        AdminGUI.configure(this);
        MixerGUI.configure(this);
        TickHandlersGUI.configure();
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public boolean initialize() {

        this.config = generator.generate();
        if (!checkVersion()) {
            if (!TreasureParticles.isAutoUpdateEnabled()) {
                TreasureParticles.newVersionInfo(this);
            } else {
                generator.reset();
                config = generator.getConfiguration();
                TreasureParticles.generatedNewFile(this);
            }
        }

        interval = config.getInt("animation.interval", interval);
        colorCycleSpeed = (float) config.getDouble("animation.color-cycle-speed", getColorCycleSpeed());
        if (taskId != -5 && !config.getBoolean("animation.enabled", true)) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -5;
        } else if (taskId == -5 && config.getBoolean("animation.enabled", true)) {
            taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(TreasureParticles.getPlugin(), new GUITask(), 0, interval).getTaskId();
        }

        style = getCurrentStyle();
        if (style == null) {
            TreasureParticles.logger().warning("Couldn't set GUI style");
            return false;
        }

        elements.initialize(this);
        sounds.initialize(this);

        EffectsGUI.setItems();
        ColorsGUI.setItems();
        MixerGUI.setItems();
        TickHandlersGUI.setItems();
        return true;
    }

    @Override
    public void reload() {
        initialize();
    }

    public GUIStyle getCurrentStyle() {
        var id = config.getString("current-style");
        if (id == null) return null;
        return new GUIStyle(
                id,
                Stream.of(GUI.values())
                        .collect(Collectors.toMap(
                                gui -> gui,
                                gui -> new GUILayout(config.getStringList("styles." + id + "." + gui.id() + ".layout").toArray(String[]::new))
                        ))
        );
    }
}