package net.treasure.effect.script.variable.cycle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.treasure.effect.data.EffectData;
import net.treasure.effect.handler.HandlerEvent;
import net.treasure.effect.script.Script;
import net.treasure.effect.script.argument.type.DoubleArgument;
import org.bukkit.entity.Player;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class VariableCycle extends Script {

    String variable;
    Operator operator = Operator.ADD;
    DoubleArgument step, min, max;
    boolean revertWhenDone = true;

    boolean forward = true;

    @Override
    public TickResult tick(Player player, EffectData data, HandlerEvent event, int times) {
        var pair = data.getVariable(effect, variable);
        if (pair == null) return TickResult.NORMAL;

        var current = pair.y();

        var step = this.step.get(player, this, data);
        var min = this.min.get(player, this, data);
        var max = this.max.get(player, this, data);

        current += forward ? (step) : (-step);
        if (forward ? current >= max : current <= min) {
            current = revertWhenDone ? (forward ? max : min) : min;
            forward = revertWhenDone != forward;
        }

        pair.y(current);
        return TickResult.NORMAL;
    }

    @Override
    public Script clone() {
        return new VariableCycle(variable, operator, step, min, max, revertWhenDone, true);
    }

    public enum Operator {
        ADD,
        MULTIPLY
    }
}