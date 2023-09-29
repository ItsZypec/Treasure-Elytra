package net.treasure.gui.type.admin;

import net.treasure.TreasureParticles;
import net.treasure.color.ColorManager;
import net.treasure.color.data.RGBColorData;
import net.treasure.color.generator.Gradient;
import net.treasure.effect.EffectManager;
import net.treasure.effect.handler.HandlerEvent;
import net.treasure.gui.GUIManager;
import net.treasure.gui.config.GUISounds;
import net.treasure.gui.task.GUITask;
import net.treasure.gui.type.GUI;
import net.treasure.gui.type.GUIType;
import net.treasure.gui.type.effects.EffectsGUI;
import net.treasure.player.PlayerManager;
import net.treasure.util.item.CustomItem;
import net.treasure.util.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class AdminGUI extends GUI {

    private final EffectManager effectManager;
    private final PlayerManager playerManager;
    private final ColorManager colorManager;

    public AdminGUI(GUIManager manager) {
        super(manager, GUIType.EFFECTS);
        this.effectManager = TreasureParticles.getEffectManager();
        this.playerManager = TreasureParticles.getPlayerManager();
        this.colorManager = TreasureParticles.getColorManager();
    }

    @Override
    public void open(Player player) {
        open(player, null, null, 0);
    }

    public void open(Player player, FilterCategory filterCategory, HandlerEvent filterEvent, int page) {
        // Variables
        var data = playerManager.getEffectData(player);
        var colorCycleSpeed = manager.getColorCycleSpeed();
        var effectSlots = EffectsGUI.DEFAULT_ICON.slots();
        var maxEffects = effectSlots.length;

        // Effects
        var effects = effectManager.getEffects().stream().filter(effect -> filterCategory == null || ((filterEvent == null || effect.getEvents().contains(filterEvent)) && (filterCategory == FilterCategory.SUPPORTED_EVENTS || (filterCategory == FilterCategory.HAS_PERMISSION && effect.getPermission() != null) || (filterCategory == FilterCategory.NO_PERMISSION && effect.getPermission() == null)))).toList();

        // Create inventory
        var holder = new AdminHolder(filterCategory, filterEvent);
        var inventory = Bukkit.createInventory(holder, layout.getSize(), MessageUtils.parseLegacy("<red>TreasureParticles [ᴀᴅᴍɪɴ]"));
        holder.setInventory(inventory);
        holder.setPage(page);

        super.commonItems(player, holder)
                .pageItems(player, holder, (page + 1) * maxEffects < effects.size(), () -> open(player, holder.getCategory(), holder.getEvent(), holder.getPage() - 1));

        // Filter button
        for (int slot : FILTER.slots())
            holder.setItem(slot,
                    new CustomItem(FILTER.item())
                            .setDisplayName(MessageUtils.gui("<red>Filter"))
                            .addLore(MessageUtils.gui("<dark_gray>" + (filterCategory == null ? "None" : filterCategory.translation())))
                            .addLore(filterCategory != FilterCategory.SUPPORTED_EVENTS ? null : Arrays.stream(HandlerEvent.values()).map(event -> MessageUtils.gui("<dark_gray> • <" + (event == filterEvent ? "green" : "gray") + ">" + translations.get("events." + event.translationKey()))).toList())
                            .addLore("",
                                    filterCategory != FilterCategory.SUPPORTED_EVENTS ? null : MessageUtils.gui("<yellow>Left/Right click to change filter"),
                                    MessageUtils.gui("<gold>Middle click to change filter category")
                            ),
                    event -> {
                        var sound = GUISounds.FILTER;
                        if (event.getClick() == ClickType.MIDDLE) {
                            var filter = holder.getCategory();
                            var values = FilterCategory.values();

                            var ordinal = filter == null ? (event.isRightClick() ? values.length - 1 : 0) : filter.ordinal() + (event.isRightClick() ? -1 : 1);
                            var newFilter = ordinal >= values.length || ordinal < 0 ? null : values[ordinal];
                            open(player, newFilter, holder.getEvent(), 0);
                        } else if (holder.getCategory() == FilterCategory.SUPPORTED_EVENTS) {
                            var filter = holder.getEvent();
                            var values = HandlerEvent.values();

                            var ordinal = filter == null ? (event.isRightClick() ? values.length - 1 : 0) : filter.ordinal() + (event.isRightClick() ? -1 : 1);
                            var newFilter = ordinal >= values.length || ordinal < 0 ? null : values[ordinal];
                            open(player, holder.getCategory(), newFilter, 0);
                        }
                        GUISounds.play(player, sound);
                    });

        int index = 0;
        for (int i = page * maxEffects; i < (page + 1) * maxEffects; i++) {
            if (effects.size() <= i) break;

            int where = effectSlots[index];

            var effect = effects.get(i);
            Color color = null;
            RGBColorData colorData = null;

            var colorGroup = effect.getColorGroup();
            var canUseAny = colorGroup != null && colorGroup.canUseAny(player);

            if (effect.getArmorColor() != null) {
                var scheme = colorManager.getColorScheme(effect.getArmorColor());
                if (scheme != null) {
                    colorData = new RGBColorData(scheme, colorCycleSpeed, true, false);
                    color = colorData.next(null);
                } else {
                    try {
                        color = Gradient.hex2Rgb(effect.getArmorColor());
                    } catch (Exception ignored) {
                        TreasureParticles.logger().warning(effect.getPrefix() + "Unknown armor color value: " + effect.getArmorColor());
                    }
                }
            } else if (canUseAny) {
                var preference = data.getColorPreference(effect);
                var scheme = preference == null ? colorGroup.getAvailableOptions().get(0).colorScheme() : preference;
                colorData = new RGBColorData(scheme, colorCycleSpeed, true, false);
                color = colorData.next(null);
            }

            holder.setItem(where, new CustomItem(effect.getIcon())
                    .setDisplayName(MessageUtils.gui(effect.getDisplayName()))
                    .addLore(MessageUtils.gui("<dark_gray>" + effect.getKey()), "")
                    .addLore(effect.getDescription())
                    .addLore(effect.getDescription() != null ? ChatColor.AQUA.toString() : null)
                    .addLore(
                            MessageUtils.gui("<gray>Permission: <yellow>" + (effect.getPermission() == null ? "None" : effect.getPermission())),
                            MessageUtils.gui("<gray>Caching: <yellow>" + (effect.isCachingEnabled() ? "Enabled" : "Disabled")),
                            MessageUtils.gui("<gray>Interval: <yellow>" + effect.getInterval())
                    )
                    .addLore(colorGroup != null ? MessageUtils.gui("<gray>Color Group: <yellow>" + colorGroup.getKey()) : null)
                    .changeArmorColor(color)
                    .glow(effect.equals(data.getCurrentEffect()))
                    .addItemFlags(ItemFlag.values()), null, colorData);
            index += 1;
        }

        player.openInventory(inventory);
        if (holder.hasAnimation()) GUITask.getPlayers().add(player);
    }
}