package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.components.ForgePlayerOriginComponent;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.forge.ModComponentsImpl;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraftforge.common.ForgeHooks;
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

	/**
	 * This event makes some assumptions:
	 * <ol>
	 *     <li>The tool used is the right one:<BR>
	 *     If this assumption is broken, {@link Operation#ADDITION} will be about 3.3x less powerful
	 *     than they should be.<BR>
	 *     The correct way to do this would be to call {@link ForgeHooks#canHarvestBlock(BlockState, PlayerEntity, BlockView, BlockPos)}
	 *     unfortunately this would both slow down the game AND may cause unwanted behaviour in other mods.
	 *     </li>
	 *     <li>The break speed scales with the previously modified values:
	 *     If this assumption is broken, {@link Operation#MULTIPLY_BASE} will be more powerful than
	 *     it should, but this seems to be the assumption made by the fabric version.
	 *     </li>
	 * </ol>
	 */
	@SubscribeEvent
	public static void modifyBreakSpeed(PlayerEvent.BreakSpeed event) {
		PlayerEntity player = event.getPlayer();
		float speed = event.getNewSpeed();
		if (PowerTypes.AQUA_AFFINITY.isActive(player)) {
			if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
				speed *= 5;
			if (!player.isOnGround() && player.isInsideWaterOrBubbleColumn())
				speed *= 5;
		}

		int toolFactor = 30; //30 for effective tool, 100 for ineffective tool.
		float factor = event.getState().getHardness(player.world, event.getPos()) * toolFactor;
		speed = OriginComponent.modify(player, ModifyBreakSpeedPower.class, speed * factor, p -> p.doesApply(player.world, event.getPos())) / factor;
		event.setNewSpeed(speed);
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
