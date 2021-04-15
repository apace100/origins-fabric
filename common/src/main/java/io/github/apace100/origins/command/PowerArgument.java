package io.github.apace100.origins.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeReference;
import net.minecraft.util.Identifier;

public class PowerArgument implements ArgumentType<PowerType<?>> {

    public static PowerArgument power() {
        return new PowerArgument();
    }
    
    public PowerType<?> parse(StringReader reader) throws CommandSyntaxException {
        Identifier id = Identifier.fromCommandInput(reader);
        return new PowerTypeReference(id);
    }
    
}
