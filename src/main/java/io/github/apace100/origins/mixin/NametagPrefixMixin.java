package io.github.apace100.origins.mixin;

import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class NametagPrefixMixin {

    @ModifyArg(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"), index = 1)
    private Text appendOriginsNametagPrefix(Text name) {
        if(OriginsClient.config.renderOriginNametagPrefix) {
            LiteralText combinedPrefix = new LiteralText("");
            ModComponents.ORIGIN.get(this).getOrigins().entrySet().stream()
                .filter(entry -> !entry.getKey().isHidden())
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> combinedPrefix.append(entry.getValue().getNametagPrefix()));
            return combinedPrefix.equals(LiteralText.EMPTY) ? name : combinedPrefix.append(name);
        }
        return name;
    }

}
