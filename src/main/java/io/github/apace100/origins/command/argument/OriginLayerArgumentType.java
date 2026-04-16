package io.github.apace100.origins.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayerManager;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class OriginLayerArgumentType implements ArgumentType<Identifier> {

   public static final DynamicCommandExceptionType LAYER_NOT_FOUND = new DynamicCommandExceptionType(
       o -> Text.stringifiedTranslatable("commands.origin.layer_not_found", o)
   );

   public static OriginLayerArgumentType layer() {
      return new OriginLayerArgumentType();
   }

   public static <S> OriginLayer getLayer(CommandContext<S> context, String argumentName) throws CommandSyntaxException {
      Identifier id = context.getArgument(argumentName, Identifier.class);
      return OriginLayerManager.getResult(id)
          .result()
          .filter(OriginLayer::isEnabled)
          .orElseThrow(() -> LAYER_NOT_FOUND.create(id));
   }

   @Override
   public Identifier parse(StringReader reader) throws CommandSyntaxException {
      return Identifier.fromCommandInputNonEmpty(reader);
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(OriginLayerManager.values().stream().filter(OriginLayer::isEnabled).map(OriginLayer::getId), builder);
   }

}
