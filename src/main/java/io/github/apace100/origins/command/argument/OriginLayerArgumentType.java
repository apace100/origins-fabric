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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class OriginLayerArgumentType implements ArgumentType<OriginLayer> {

   public static final DynamicCommandExceptionType LAYER_NOT_FOUND = new DynamicCommandExceptionType(
       o -> Text.translatable("commands.origin.layer_not_found", o)
   );

   public static OriginLayerArgumentType layer() {
      return new OriginLayerArgumentType();
   }

   public static OriginLayer getLayer(CommandContext<ServerCommandSource> context, String argumentName) {
      return context.getArgument(argumentName, OriginLayer.class);
   }

   @Override
   public OriginLayer parse(StringReader reader) throws CommandSyntaxException {
      Identifier id = Identifier.fromCommandInputNonEmpty(reader);
      return OriginLayerManager.getResult(id)
          .result()
          .orElseThrow(() -> LAYER_NOT_FOUND.create(id));
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(OriginLayerManager.values().stream().filter(OriginLayer::isEnabled).map(OriginLayer::getId), builder);
   }

}
