package io.github.apace100.origins.mixin;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class NametagPrefixMixin {

    @ModifyArg(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"), index = 1)
    private Text appendOriginsNametagPrefix(Text name) {

        if (!Origins.config.enableNametagPrefix) {
            return name;
        }

        MutableText combinedPrefix = Text.literal("");
        ModComponents.ORIGIN.get(this).getOrigins().entrySet().stream()
            .filter(entry -> !(entry.getKey().isHidden() || Origins.config.nametagPrefixBlacklist.contains(entry.getKey().getIdentifier().toString())))
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> combinedPrefix.append(entry.getValue().getNametagPrefix()));

        return combinedPrefix;

    }

}
