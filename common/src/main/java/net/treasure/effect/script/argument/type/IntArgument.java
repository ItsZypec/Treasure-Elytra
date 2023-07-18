package net.treasure.effect.script.argument.type;

import lombok.AllArgsConstructor;
import net.treasure.effect.data.EffectData;
import net.treasure.effect.exception.ReaderException;
import net.treasure.effect.script.Script;
import net.treasure.effect.script.argument.ScriptArgument;
import net.treasure.effect.script.reader.ReaderContext;
import net.treasure.effect.script.variable.Variable;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class IntArgument implements ScriptArgument<Integer> {

    Object value;

    public static IntArgument read(ReaderContext<?> context) throws ReaderException {
        var arg = context.value();
        try {
            return new IntArgument(Integer.parseInt(arg)).validate(context);
        } catch (Exception e) {
            return new IntArgument(arg).validate(context);
        }
    }

    @Override
    public Integer get(Player player, Script script, EffectData data) {
        if (value == null) return null;
        else if (value instanceof Integer i) return i;
        else if (value instanceof String s) return data.getVariable(script.getEffect(), s).y().intValue();
        else return null;
    }

    @Override
    public IntArgument validate(ReaderContext<?> context) throws ReaderException {
        var arg = context.value();
        try {
            Integer.parseInt(arg);
            return this;
        } catch (Exception e) {
            arg = Variable.replace(arg);
            if (context.effect().isValidVariable(arg))
                return new IntArgument(arg);
            else
                throw new ReaderException("Valid values for Integer argument: integers, {variable}");
        }
    }
}