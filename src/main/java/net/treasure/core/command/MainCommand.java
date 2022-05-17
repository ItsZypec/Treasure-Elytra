package net.treasure.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import net.treasure.core.TreasurePlugin;
import net.treasure.core.command.gui.EffectsGUI;
import net.treasure.effect.Effect;
import net.treasure.locale.Messages;
import net.treasure.util.message.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("treasureelytra|trelytra|tre")
public class MainCommand extends BaseCommand {

    @HelpCommand
    @CommandPermission("%basecmd")
    public void menu(Player player) {
        new EffectsGUI().open(player, 0);
    }

    @Subcommand("toggle")
    public void toggle(Player player) {
        var data = TreasurePlugin.getInstance().getPlayerManager().getPlayerData(player);
        data.setEffectsEnabled(!data.isEffectsEnabled());
        MessageUtils.sendParsed(player, String.format(Messages.EFFECT_TOGGLE, data.isEffectsEnabled() ? Messages.ENABLED : Messages.DISABLED));
    }

    @Private
    @CommandPermission("%basecmd")
    @Subcommand("select|sel")
    @CommandCompletion("@effects")
    public void selectEffect(Player player, Effect effect) {
        if (effect.getPermission() != null && !player.hasPermission(effect.getPermission())) {
            MessageUtils.sendParsed(player, Messages.EFFECT_NO_PERMISSION);
            return;
        }
        TreasurePlugin.getInstance().getPlayerManager().getPlayerData(player).setCurrentEffect(player, effect);
        MessageUtils.sendParsed(player, String.format(Messages.EFFECT_SELECTED, effect.getDisplayName()));
    }

    @Subcommand("reload|rl")
    @CommandPermission("%admincmd")
    public void reload(CommandSender sender) {
        MessageUtils.sendParsed(sender, Messages.RELOADING);
        TreasurePlugin.getInstance().reload();
        MessageUtils.sendParsed(sender, Messages.RELOADED);
    }
}