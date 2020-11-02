package io.github.apace100.origins.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OriginArgument implements ArgumentType<Origin> {
   public static final DynamicCommandExceptionType ORIGIN_NOT_FOUND = new DynamicCommandExceptionType((p_208663_0_) -> {
      return new TranslatableText("commands.origin.origin_not_found", p_208663_0_);
   });

   public static OriginArgument origin() {
      return new OriginArgument();
   }

   public Origin parse(StringReader p_parse_1_) throws CommandSyntaxException {
      Identifier id = Identifier.fromCommandInput(p_parse_1_);
      try {
    	  return OriginRegistry.get(id);
      } catch(IllegalArgumentException e) {
    	  throw ORIGIN_NOT_FOUND.create(id);
      }
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      OriginLayer layer = null;
      try {
         layer = context.getArgument("layer", OriginLayer.class);
      } catch(Exception e) {
         // no-op :)
      }
      if(layer != null) {
         List<Identifier> ids = new LinkedList<>(layer.getOrigins());
         ids.add(Origin.EMPTY.getIdentifier());
         return CommandSource.suggestIdentifiers(ids.stream(), builder);
      } else {
         return CommandSource.suggestIdentifiers(OriginRegistry.identifiers(), builder);
      }
   }
}