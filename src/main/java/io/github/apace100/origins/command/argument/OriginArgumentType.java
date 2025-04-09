package io.github.apace100.origins.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.origins.origin.*;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class OriginArgumentType implements ArgumentType<Identifier> {

   public static final DynamicCommandExceptionType ORIGIN_NOT_FOUND = new DynamicCommandExceptionType(
       o -> Text.translatable("commands.origin.origin_not_found", o)
   );

   public static OriginArgumentType origin() {
      return new OriginArgumentType();
   }

   public static <S> Origin getOrigin(CommandContext<S> context, String argumentName) throws CommandSyntaxException {
      Identifier id = context.getArgument(argumentName, Identifier.class);
      return OriginManager
          .getOptional(id)
          .orElseThrow(() -> ORIGIN_NOT_FOUND.create(id));
   }

   @Override
   public Identifier parse(StringReader reader) throws CommandSyntaxException {
      return Identifier.fromCommandInputNonEmpty(reader);
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

      try {

         OriginLayer layer = context.getArgument("layer", OriginLayer.class);
         Stream.Builder<Identifier> origins = Stream.builder();

         origins.add(Origin.EMPTY.getId());
         layer.getOrigins().forEach(origins);

         return CommandSource.suggestIdentifiers(origins.build(), builder);

      }

      catch (Exception e) {
         return CommandSource.suggestIdentifiers(Stream.of(), builder);
      }

   }

}
