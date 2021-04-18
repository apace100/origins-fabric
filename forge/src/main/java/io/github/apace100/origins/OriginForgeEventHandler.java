package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.components.ForgePlayerOriginComponent;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModComponentsArchitectury;
import io.github.apace100.origins.registry.forge.ModComponentsArchitecturyImpl;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Origins.MODID)
public class OriginForgeEventHandler {
	@SubscribeEvent
	public static void modifyBreakSpeed(PlayerEvent.BreakSpeed event) {
		PlayerEntity player = event.getPlayer();
		float hardness = event.getState().getHardness(player.world, event.getPos());
		if (hardness <= 0)
			return;
		float speed = event.getNewSpeed();
		if (PowerTypes.AQUA_AFFINITY.isActive(player)) {
			if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
				speed *= 5;
			if (!player.isOnGround() && player.isInsideWaterOrBubbleColumn())
				speed *= 5;
		}

		int toolFactor = ForgeHooks.canHarvestBlock(event.getState(), event.getPlayer(), event.getPlayer().world, event.getPos()) ? 30 : 100;
		float factor = hardness * toolFactor;
		speed = OriginComponent.modify(player, ModifyBreakSpeedPower.class, speed * factor, p -> p.doesApply(player.world, event.getPos())) / factor;
		event.setNewSpeed(speed);
	}

	@SubscribeEvent
	public static void breakBlock(BlockEvent.BreakEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(event.getWorld(), event.getPos(), true);
			OriginComponent.getPowers(event.getPlayer(), ActionOnBlockBreakPower.class).stream().filter(p -> p.doesApply(cachedBlockPosition))
					.forEach(aobbp -> aobbp.executeActions(!event.isCanceled(), event.getPos(), null));
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
			ModComponentsArchitectury.getOriginComponent(event.player).serverTick();
		}
	}

	@SubscribeEvent
	public static void playerClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			copy(ModComponentsArchitecturyImpl.ORIGIN_COMPONENT_CAPABILITY, event);
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity)
			ModComponentsArchitectury.syncWith((ServerPlayerEntity) event.getPlayer(), event.getPlayer());
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity)
			ModComponentsArchitectury.syncWith((ServerPlayerEntity) event.getPlayer(), event.getPlayer());
	}

	@SubscribeEvent
	public static void trackNew(EntityJoinWorldEvent event) {
		if (event.getWorld().isClient())
			return;
		Entity entity = event.getEntity();
		Packet<?> packet = ModComponentsArchitecturyImpl.buildOtherPacket(entity);
		if (packet != null)
			PacketDistributor.TRACKING_ENTITY_AND_SELF.with(event::getEntity).send(packet);
	}

	@SubscribeEvent
	public static void trackEntity(PlayerEvent.StartTracking event) {
		if (event.getPlayer() instanceof ServerPlayerEntity && event.getTarget() instanceof PlayerEntity) {
			ModComponentsArchitectury.syncWith((ServerPlayerEntity) event.getPlayer(), event.getTarget());
		}
	}

	public static <T> void copy(Capability<T> cap, PlayerEvent.Clone event) {
		event.getPlayer().getCapability(cap).ifPresent(target -> event.getOriginal().getCapability(cap).ifPresent(source -> cap.readNBT(target, null, cap.writeNBT(source, null))));
	}
}
