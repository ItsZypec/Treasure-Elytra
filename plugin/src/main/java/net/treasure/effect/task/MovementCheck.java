package net.treasure.effect.task;

import lombok.AllArgsConstructor;
import net.treasure.core.player.PlayerManager;
import net.treasure.util.TimeKeeper;
import org.bukkit.util.Vector;

@AllArgsConstructor
public class MovementCheck implements Runnable {

    final PlayerManager playerManager;

    @Override
    public void run() {
        TimeKeeper.increaseTime();
        var iterator = playerManager.getData().entrySet().iterator();
        while (iterator.hasNext()) {
            var set = iterator.next();

            var data = set.getValue();

            var player = data.getPlayer();
            if (player == null) {
                iterator.remove();
                continue;
            }

            if (data.getCurrentEffect() == null)
                continue;

            var location = player.getLocation();
            var last = data.getLastVector();
            var current = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            data.setLastVector(current);

            if (!current.equals(last)) {
                data.resetInterval();
            } else {
                data.increaseInterval();
            }
        }
    }
}