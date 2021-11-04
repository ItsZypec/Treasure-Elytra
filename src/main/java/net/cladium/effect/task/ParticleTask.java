package net.cladium.effect.task;

import net.cladium.core.CladiumPlugin;
import net.cladium.effect.player.EffectData;
import net.cladium.util.TimeKeeper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ParticleTask implements Runnable {

    @Override
    public void run() {
        TimeKeeper.increaseTime();
        if (TimeKeeper.getTimeElapsed() > 1000L * 60)
            TimeKeeper.reset();
        Iterator<Map.Entry<UUID, EffectData>> iterator = CladiumPlugin.getInstance().getPlayerManager().getPlayersData().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, EffectData> set = iterator.next();

            UUID uuid = set.getKey();
            EffectData data = set.getValue();

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                iterator.remove();
                continue;
            }

            if (!data.isEnabled())
                continue;

            if (!player.isGliding())
                continue;

            if (data.getCurrentEffect() != null)
                data.getCurrentEffect().doTick(player, data);
        }
    }
}