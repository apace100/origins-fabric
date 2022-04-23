package io.github.apace100.origins.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class LayerArgumentType implements ArgumentType<Identifier> {
   public static final DynamicCommandExceptionType LAYER_NOT_FOUND = new DynamicCommandExceptionType((p_208663_0_) -> {
      return new TranslatableText("commands.origin.layer_not_found", p_208663_0_);
   });

   public static LayerArgumentType layer() {
      return new LayerArgumentType();
   }

   public Identifier parse(StringReader p_parse_1_) throws CommandSyntaxException {
      return Identifier.fromCommandInput(p_parse_1_);
   }

   public static OriginLayer getLayer(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
      Identifier id = context.getArgument(argumentName, Identifier.class);
      try {
         return OriginLayers.getLayer(id);
      } catch(IllegalArgumentException e) {
         throw LAYER_NOT_FOUND.create(id);
      }
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(OriginLayers.getLayers().stream().map(OriginLayer::getIdentifier), builder);
   }
}