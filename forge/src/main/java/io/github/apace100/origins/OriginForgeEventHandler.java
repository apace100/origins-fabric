package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.components.ForgePlayerOriginComponent;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.forge.ModComponentsImpl;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Origins.MODID)
public class OriginForgeEventHandler {

	@SubscribeEvent
	public static void modifyBreakSpeed(PlayerEvent.BreakSpeed event) {
		PlayerEntity player = event.getPlayer();
		event.setNewSpeed(OriginComponent.modify(player, ModifyBreakSpeedPower.class, event.getNewSpeed(), p -> p.doesApply(player.world, event.getPos())));
		if (PowerTypes.AQUA_AFFINITY.isActive(player)) {
			if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
				event.setNewSpeed(5 * event.getNewSpeed());
			if (!player.isOnGround() && player.isInsideWaterOrBubbleColumn())
				event.setNewSpeed(5 * event.getNewSpeed());
		}
	}

	@SubscribeEvent
	public static void modifyDamageTaken(LivingDamageEvent event) {
		LivingEntity entityLiving = event.getEntityLiving();
		event.setAmount(OriginComponent.modify(entityLiving, ModifyDamageTakenPower.class, event.getAmount(), p -> p.doesApply(event.getSource(), event.getAmount()), p -> p.executeActions(event.getSource().getAttacker())));
	}

	@SubscribeEvent
	public static void modifyDamageDealt(LivingHurtEvent event) {
		//Forge only fires on LivingEntity. So we're using that.
		LivingEntity target = event.getEntityLiving();
		DamageSource source = event.getSource();
		if (event.getSource().isProjectile()) {
			event.setAmount(OriginComponent.modify(source.getAttacker(), ModifyProjectileDamagePower.class, event.getAmount(), p -> p.doesApply(source, event.getAmount(), target), p -> p.executeActions(target)));
		} else {
			event.setAmount(OriginComponent.modify(source.getAttacker(), ModifyDamageDealtPower.class, event.getAmount(), p -> p.doesApply(source, event.getAmount(), target), p -> p.executeActions(target)));
		}
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity)
			event.addCapability(Origins.identifier("origin_component"), new ForgePlayerOriginComponent((PlayerEntity) event.getObject()));
	}

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
			ModComponents.getOriginComponent(event.player).serverTick();
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			copy(ModComponentsImpl.ORIGIN_COMPONENT_CAPABILITY, event);
			ModComponents.syncOriginComponent(event.getPlayer());
		}
	}

	public static <T> void copy(Capability<T> cap, PlayerEvent.Clone event) {
		event.getPlayer().getCapability(cap).ifPresent(target -> event.getOriginal().getCapability(cap).ifPresent(source -> cap.readNBT(target, null, cap.writeNBT(source, null))));
	}
}
