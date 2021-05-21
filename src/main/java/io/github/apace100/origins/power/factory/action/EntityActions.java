package io.github.apace100.origins.power.factory.action;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.CooldownPower;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.VariableIntPower;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.*;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class EntityActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("actions", SerializableDataType.ENTITY_ACTIONS),
            (data, entity) -> ((List<ActionFactory<Entity>.Instance>)data.get("actions")).forEach((e) -> e.accept(entity))));
        register(new ActionFactory<>(Origins.identifier("chance"), new SerializableData()
            .add("action", SerializableDataType.ENTITY_ACTION)
            .add("chance", SerializableDataType.FLOAT),
            (data, entity) -> {
                if(new Random().nextFloat() < data.getFloat("chance")) {
                    ((ActionFactory<Entity>.Instance)data.get("action")).accept(entity);
                }
            }));
        register(new ActionFactory<>(Origins.identifier("if_else"), new SerializableData()
            .add("condition", SerializableDataType.ENTITY_CONDITION)
            .add("if_action", SerializableDataType.ENTITY_ACTION)
            .add("else_action", SerializableDataType.ENTITY_ACTION, null),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    if(((ConditionFactory<LivingEntity>.Instance)data.get("condition")).test((LivingEntity)entity)) {
                        ((ActionFactory<Entity>.Instance)data.get("if_action")).accept(entity);
                    } else {
                        if(data.isPresent("else_action")) {
                            ((ActionFactory<Entity>.Instance)data.get("else_action")).accept(entity);
                        }
                    }
                }
            }));
        register(new ActionFactory<>(Origins.identifier("choice"), new SerializableData()
            .add("actions", SerializableDataType.weightedList(SerializableDataType.ENTITY_ACTION)),
            (data, entity) -> {
                FilterableWeightedList<ActionFactory<Entity>.Instance> actionList = (FilterableWeightedList<ActionFactory<Entity>.Instance>)data.get("actions");
                ActionFactory<Entity>.Instance action = actionList.pickRandom(new Random());
                action.accept(entity);
            }));
        register(new ActionFactory<>(Origins.identifier("if_else_list"), new SerializableData()
            .add("actions", SerializableDataType.list(SerializableDataType.compound(ClassUtil.castClass(Pair.class), new SerializableData()
                .add("action", SerializableDataType.ENTITY_ACTION)
                .add("condition", SerializableDataType.ENTITY_CONDITION),
                inst -> new Pair<>((ConditionFactory<LivingEntity>.Instance)inst.get("condition"), (ActionFactory<Entity>.Instance)inst.get("action")),
                (data, pair) -> {
                    SerializableData.Instance inst = data.new Instance();
                    inst.set("condition", pair.getLeft());
                    inst.set("action", pair.getRight());
                    return inst;
                }))),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    List<Pair<ConditionFactory<Entity>.Instance, ActionFactory<Entity>.Instance>> actions =
                        (List<Pair<ConditionFactory<Entity>.Instance, ActionFactory<Entity>.Instance>>)data.get("actions");
                    for (Pair<ConditionFactory<Entity>.Instance, ActionFactory<Entity>.Instance> action: actions) {
                        if(action.getLeft().test(entity)) {
                            action.getRight().accept(entity);
                            break;
                        }
                    }
                }
            }));
        register(new ActionFactory<>(Origins.identifier("damage"), new SerializableData()
            .add("amount", SerializableDataType.FLOAT)
            .add("source", SerializableDataType.DAMAGE_SOURCE),
            (data, entity) -> entity.damage((DamageSource)data.get("source"), data.getFloat("amount"))));
        register(new ActionFactory<>(Origins.identifier("heal"), new SerializableData()
            .add("amount", SerializableDataType.FLOAT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    ((LivingEntity)entity).heal(data.getFloat("amount"));
                }
            }));
        register(new ActionFactory<>(Origins.identifier("play_sound"), new SerializableData()
                .add("sound", SerializableDataType.SOUND_EVENT)
                .add("volume", SerializableDataType.FLOAT, 1F)
                .add("pitch", SerializableDataType.FLOAT, 1F),
                (data, entity) -> {
                    if(entity instanceof PlayerEntity) {
                        entity.world.playSound((PlayerEntity) null, (entity).getX(), (entity).getY(), (entity).getZ(), (SoundEvent)data.get("sound"),
                        SoundCategory.PLAYERS, data.getFloat("volume"), data.getFloat("pitch"));
                    }
                }));
        register(new ActionFactory<>(Origins.identifier("exhaust"), new SerializableData()
            .add("amount", SerializableDataType.FLOAT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity)
                    ((PlayerEntity)entity).getHungerManager().addExhaustion(data.getFloat("amount"));
            }));
        register(new ActionFactory<>(Origins.identifier("apply_effect"), new SerializableData()
            .add("effect", SerializableDataType.STATUS_EFFECT_INSTANCE, null)
            .add("effects", SerializableDataType.STATUS_EFFECT_INSTANCES, null),
            (data, entity) -> {
                if(entity instanceof LivingEntity && !entity.world.isClient) {
                    LivingEntity le = (LivingEntity) entity;
                    if(data.isPresent("effect")) {
                        StatusEffectInstance effect = (StatusEffectInstance)data.get("effect");
                        le.addStatusEffect(new StatusEffectInstance(effect));
                    }
                    if(data.isPresent("effects")) {
                        ((List<StatusEffectInstance>)data.get("effects")).forEach(e -> le.addStatusEffect(new StatusEffectInstance(e)));
                    }
                }
            }));
        register(new ActionFactory<>(Origins.identifier("clear_effect"), new SerializableData()
            .add("effect", SerializableDataType.STATUS_EFFECT, null),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) entity;
                    if(data.isPresent("effect")) {
                        le.removeStatusEffect((StatusEffect)data.get("effect"));
                    } else {
                        le.clearStatusEffects();
                    }
                }
            }));
        register(new ActionFactory<>(Origins.identifier("set_on_fire"), new SerializableData()
            .add("duration", SerializableDataType.INT),
            (data, entity) -> {
                entity.setOnFireFor(data.getInt("duration"));
            }));
        register(new ActionFactory<>(Origins.identifier("add_velocity"), new SerializableData()
            .add("x", SerializableDataType.FLOAT, 0F)
            .add("y", SerializableDataType.FLOAT, 0F)
            .add("z", SerializableDataType.FLOAT, 0F)
            .add("space", SerializableDataType.SPACE, Space.WORLD)
            .add("set", SerializableDataType.BOOLEAN, false),
            (data, entity) -> {
                Space space = (Space)data.get("space");
                Vector3f vec = new Vector3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                Vec3d vel;
                Vec3d velH;
                TriConsumer<Float, Float, Float> method = entity::addVelocity;
                if(data.getBoolean("set")) {
                    method = entity::setVelocity;
                }
                switch(space) {
                    case WORLD:
                        method.accept(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                        break;
                    case LOCAL:
                        Space.rotateVectorToBase(entity.getRotationVector(), vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case LOCAL_HORIZONTAL:
                        vel = entity.getRotationVector();
                        velH = new Vec3d(vel.x, 0, vel.z);
                        if(velH.lengthSquared() > 0.00005) {
                            velH = velH.normalize();
                            Space.rotateVectorToBase(velH, vec);
                            method.accept(vec.getX(), vec.getY(), vec.getZ());
                        }
                        break;
                    case VELOCITY:
                        Space.rotateVectorToBase(entity.getVelocity(), vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case VELOCITY_NORMALIZED:
                        Space.rotateVectorToBase(entity.getVelocity().normalize(), vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case VELOCITY_HORIZONTAL:
                        vel = entity.getVelocity();
                        velH = new Vec3d(vel.x, 0, vel.z);
                        Space.rotateVectorToBase(velH, vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case VELOCITY_HORIZONTAL_NORMALIZED:
                        vel = entity.getVelocity();
                        velH = new Vec3d(vel.x, 0, vel.z);
                        if(velH.lengthSquared() > 0.00005) {
                            velH = velH.normalize();
                            Space.rotateVectorToBase(velH, vec);
                            method.accept(vec.getX(), vec.getY(), vec.getZ());
                        }
                        break;
                }
                entity.velocityModified = true;
            }));
        register(new ActionFactory<>(Origins.identifier("spawn_entity"), new SerializableData()
            .add("entity_type", SerializableDataType.ENTITY_TYPE)
            .add("tag", SerializableDataType.NBT, null)
            .add("entity_action", SerializableDataType.ENTITY_ACTION, null),
            (data, entity) -> {
                Entity e = ((EntityType<?>)data.get("entity_type")).create(entity.world);
                if(e != null) {
                    e.refreshPositionAndAngles(entity.getPos().x, entity.getPos().y, entity.getPos().z, entity.yaw, entity.pitch);
                    if(data.isPresent("tag")) {
                        CompoundTag mergedTag = e.toTag(new CompoundTag());
                        mergedTag.copyFrom((CompoundTag)data.get("tag"));
                        e.fromTag(mergedTag);
                    }

                    entity.world.spawnEntity(e);
                    if(data.isPresent("entity_action")) {
                        ((ActionFactory<Entity>.Instance)data.get("entity_action")).accept(e);
                    }
                }
            }));
        register(new ActionFactory<>(Origins.identifier("gain_air"), new SerializableData()
            .add("value", SerializableDataType.INT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) entity;
                    le.setAir(Math.min(le.getAir() + data.getInt("value"), le.getMaxAir()));
                }
            }));
        register(new ActionFactory<>(Origins.identifier("block_action_at"), new SerializableData()
            .add("block_action", SerializableDataType.BLOCK_ACTION),
            (data, entity) -> {
                    ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("block_action")).accept(
                        Triple.of(entity.world, entity.getBlockPos(), Direction.UP));
            }));
        register(new ActionFactory<>(Origins.identifier("spawn_effect_cloud"), new SerializableData()
            .add("radius", SerializableDataType.FLOAT, 3.0F)
            .add("radius_on_use", SerializableDataType.FLOAT, -0.5F)
            .add("wait_time", SerializableDataType.INT, 10)
            .add("effect", SerializableDataType.STATUS_EFFECT_INSTANCE, null)
            .add("effects", SerializableDataType.STATUS_EFFECT_INSTANCES, null),
            (data, entity) -> {
                AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(entity.world, entity.getX(), entity.getY(), entity.getZ());
                if (entity instanceof LivingEntity) {
                    areaEffectCloudEntity.setOwner((LivingEntity)entity);
                }
                areaEffectCloudEntity.setRadius(data.getFloat("radius"));
                areaEffectCloudEntity.setRadiusOnUse(data.getFloat("radius_on_use"));
                areaEffectCloudEntity.setWaitTime(data.getInt("wait_time"));
                areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float)areaEffectCloudEntity.getDuration());
                List<StatusEffectInstance> effects = new LinkedList<>();
                if(data.isPresent("effect")) {
                    effects.add((StatusEffectInstance)data.get("effect"));
                }
                if(data.isPresent("effects")) {
                    effects.addAll((List<StatusEffectInstance>)data.get("effects"));
                }
                areaEffectCloudEntity.setColor(PotionUtil.getColor(effects));
                effects.forEach(areaEffectCloudEntity::addEffect);

                entity.world.spawnEntity(areaEffectCloudEntity);
            }));
        register(new ActionFactory<>(Origins.identifier("extinguish"), new SerializableData(),
            (data, entity) -> entity.extinguish()));
        register(new ActionFactory<>(Origins.identifier("execute_command"), new SerializableData()
            .add("command", SerializableDataType.STRING)
            .add("permission_level", SerializableDataType.INT, 4),
            (data, entity) -> {
                MinecraftServer server = entity.world.getServer();
                if(server != null) {
                    ServerCommandSource source = new ServerCommandSource(
                        CommandOutput.DUMMY,
                        entity.getPos(),
                        entity.getRotationClient(),
                        entity.world instanceof ServerWorld ? (ServerWorld)entity.world : null,
                        data.getInt("permission_level"),
                        entity.getName().getString(),
                        entity.getDisplayName(),
                        entity.world.getServer(),
                        entity);
                    server.getCommandManager().execute(source, data.getString("command"));
                }
            }));
        register(new ActionFactory<>(Origins.identifier("change_resource"), new SerializableData()
            .add("resource", SerializableDataType.POWER_TYPE)
            .add("change", SerializableDataType.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    OriginComponent component = ModComponents.ORIGIN.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("resource"));
                    if(p instanceof VariableIntPower) {
                        VariableIntPower vip = (VariableIntPower)p;
                        int newValue = vip.getValue() + data.getInt("change");
                        vip.setValue(newValue);
                        OriginComponent.sync((PlayerEntity)entity);
                    } else if(p instanceof CooldownPower) {
                        CooldownPower cp = (CooldownPower)p;
                        cp.modify(data.getInt("change"));
                        OriginComponent.sync((PlayerEntity)entity);
                    }
                }
            }));
        register(new ActionFactory<>(Origins.identifier("feed"), new SerializableData()
            .add("food", SerializableDataType.INT)
            .add("saturation", SerializableDataType.FLOAT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    ((PlayerEntity)entity).getHungerManager().add(data.getInt("food"), data.getFloat("saturation"));
                }
            }));
        register(new ActionFactory<>(Origins.identifier("add_xp"), new SerializableData()
            .add("points", SerializableDataType.INT, 0)
            .add("levels", SerializableDataType.INT, 0),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    int points = data.getInt("points");
                    int levels = data.getInt("levels");
                    if(points > 0) {
                        ((PlayerEntity)entity).addExperience(points);
                    }
                    ((PlayerEntity)entity).addExperienceLevels(levels);
                }
            }));

        Scheduler scheduler = new Scheduler();
        register(new ActionFactory<>(Origins.identifier("delay"), new SerializableData()
            .add("ticks", SerializableDataType.INT)
            .add("action", SerializableDataType.ENTITY_ACTION),
            (data, entity) -> {
                ActionFactory<Entity>.Instance action = (ActionFactory<Entity>.Instance)data.get("action");
                scheduler.queue(s -> action.accept(entity), data.getInt("ticks"));
            }));
        register(new ActionFactory<>(Origins.identifier("set_fall_distance"), new SerializableData()
            .add("fall_distance", SerializableDataType.FLOAT),
            (data, entity) -> {
                entity.fallDistance = data.getFloat("fall_distance");
            }));
        register(new ActionFactory<>(Origins.identifier("give"), new SerializableData()
            .add("stack", SerializableDataType.ITEM_STACK),
            (data, entity) -> {
                if(!entity.world.isClient()) {
                    ItemStack stack = (ItemStack)data.get("stack");
                    stack = stack.copy();
                    if(entity instanceof PlayerEntity) {
                        ((PlayerEntity)entity).inventory.offerOrDrop(entity.world, stack);
                    } else {
                        entity.world.spawnEntity(new ItemEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), stack));
                    }
                }
            }));
        register(new ActionFactory<>(Origins.identifier("equipped_item_action"), new SerializableData()
            .add("equipment_slot", SerializableDataType.EQUIPMENT_SLOT)
            .add("action", SerializableDataType.ITEM_ACTION),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    ItemStack stack = ((LivingEntity)entity).getEquippedStack((EquipmentSlot)data.get("equipment_slot"));
                    ActionFactory<ItemStack>.Instance action = (ActionFactory<ItemStack>.Instance)data.get("action");
                    action.accept(stack);
                }
            }));
        register(new ActionFactory<>(Origins.identifier("trigger_cooldown"), new SerializableData()
            .add("power", SerializableDataType.POWER_TYPE),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    OriginComponent component = ModComponents.ORIGIN.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("power"));
                    if(p instanceof CooldownPower) {
                        CooldownPower cp = (CooldownPower)p;
                        cp.use();
                    }
                }
            }));
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ModRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
