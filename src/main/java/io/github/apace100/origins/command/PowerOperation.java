package io.github.apace100.origins.command;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.apace100.origins.power.VariableIntPower;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.TranslatableText;

// Very similar to OperationArgumentType, but modified to make it work with resources.
public class PowerOperation implements ArgumentType<PowerOperation.Operation> {
    public static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(new TranslatableText("arguments.operation.invalid"));
    public static final SimpleCommandExceptionType DIVISION_ZERO_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("arguments.operation.div0"));
    
    public static PowerOperation operation() {
        return new PowerOperation();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, builder);
    }

    public PowerOperation.Operation parse(StringReader stringReader) throws CommandSyntaxException {
        if (!stringReader.canRead()) throw INVALID_OPERATION.create();

        int i = stringReader.getCursor();
        while(stringReader.canRead() && stringReader.peek() != ' ') stringReader.skip();

        String stringOperator = stringReader.getString().substring(i, stringReader.getCursor());
        switch (stringOperator) {
            case "=":
                return (resource, score) -> {
                    resource.setValue(score.getScore());
                };
            case "+=":
                return (resource, score) -> {
                    resource.setValue(resource.getValue()+score.getScore());
                };
            case "-=":
                return (resource, score) -> {
                    resource.setValue(resource.getValue()-score.getScore());
                };
            case "*=":
                return (resource, score) -> {
                    resource.setValue(resource.getValue()*score.getScore());
                };
            case "/=":
                return (resource, score) -> {
                    int r = resource.getValue();
                    int s = score.getScore();
                    if (s == 0) {
                        throw DIVISION_ZERO_EXCEPTION.create();
                    } else {
                        resource.setValue(Math.floorDiv(r, s));
                    }
                };
            case "%=":
                return (resource, score) -> {
                    int r = resource.getValue();
                    int s = score.getScore();
                    if (s == 0) {
                        throw DIVISION_ZERO_EXCEPTION.create();
                    } else {
                        resource.setValue(Math.floorMod(r, s));
                    }
                };
            case "<":
                return (resource, score) -> {
                    resource.setValue(Math.min(resource.getValue(),score.getScore()));
                };
            case ">":
                return (resource, score) -> {
                    resource.setValue(Math.max(resource.getValue(),score.getScore()));
                };
            case "><":
                return (resource, score) -> {
                    int v = score.getScore();
                    score.setScore(resource.getValue());
                    resource.setValue(v);
                };
            default:
                throw INVALID_OPERATION.create();
        }
    }

    public interface Operation {
        public void apply(VariableIntPower resource, ScoreboardPlayerScore score) throws CommandSyntaxException;
    }
}
