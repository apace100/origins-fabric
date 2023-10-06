package io.github.apace100.origins.mixin;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class OriginUpgradeMixin {

    @Shadow
    private ServerPlayerEntity owner;

    @Shadow
    public abstract AdvancementProgress getProgress(AdvancementEntry advancement);

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;endTrackingCompleted(Lnet/minecraft/advancement/AdvancementEntry;)V"))
    private void checkOriginUpgrade(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {

        AdvancementProgress progress = this.getProgress(advancement);
        if (!progress.isDone()) {
            return;
        }

        Origin.get(owner).forEach((originLayer, origin) -> origin.getUpgrade(advancement).ifPresent(originUpgrade -> {
            try {

                Origin upgradeTo = OriginRegistry.get(originUpgrade.upgradeToOrigin());
                OriginComponent component = ModComponents.ORIGIN.get(owner);

                component.setOrigin(originLayer, upgradeTo);
                component.sync();

                String announcement = originUpgrade.announcement();
                if (announcement != null) {
                    owner.sendMessage(Text.translatable(announcement).formatted(Formatting.GOLD), false);
                }

            } catch (Exception e) {
                Origins.LOGGER.error("Could not perform Origins upgrade from \"{}\" to \"{}\", as the upgrade origin did not exist!", origin.getIdentifier(), originUpgrade.upgradeToOrigin().toString());
            }
        }));

    }

}
