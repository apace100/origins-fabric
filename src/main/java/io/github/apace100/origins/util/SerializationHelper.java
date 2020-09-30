package io.github.apace100.origins.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.power.factory.condition.block.BlockCondition;
import io.github.apace100.origins.power.factory.condition.damage.DamageCondition;
import io.github.apace100.origins.power.factory.condition.item.ItemCondition;
import io.github.apace100.origins.power.factory.condition.player.PlayerCondition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SerializationHelper {

    public static Tag<Fluid> getFluidTagFromId(Identifier id) {
        Optional<? extends Tag.Identified<Fluid>> tag = FluidTags.all().stream().filter(f -> f.getId().equals(id)).findAny();
        if(tag.isPresent()) {
            return tag.get();
        }
        return null;
    }

    public static Tag<Block> getBlockTagFromId(Identifier id) {
        return ServerTagManagerHolder.getTagManager().getBlocks().getTag(id);//BlockTags.getTagGroup().getTag(id);
    }

    public static EntityAttributeModifier readAttributeModifier(JsonObject json) {
        String name = JsonHelper.getString(json, "name", "Unnamed attribute modifier");
        String operation = JsonHelper.getString(json, "operation").toUpperCase();
        double value = JsonHelper.getFloat(json, "value");
        return new EntityAttributeModifier(name, value, EntityAttributeModifier.Operation.valueOf(operation));
    }

    @Environment(EnvType.CLIENT)
    public static EntityAttributeModifier readAttributeModifier(PacketByteBuf buf) {
        String modName = buf.readString();
        double modValue = buf.readDouble();
        int operation = buf.readInt();
        return new EntityAttributeModifier(modName, modValue, EntityAttributeModifier.Operation.fromId(operation));
    }

    public static void writeAttributeModifier(EntityAttributeModifier modifier, PacketByteBuf buf) {
        buf.writeString(modifier.getName());
        buf.writeDouble(modifier.getValue());
        buf.writeInt(modifier.getOperation().getId());
    }

    public static StatusEffectInstance readStatusEffect(JsonObject json) {
        String effect = JsonHelper.getString(json, "effect");
        Optional<StatusEffect> effectOptional = Registry.STATUS_EFFECT.getOrEmpty(Identifier.tryParse(effect));
        if(!effectOptional.isPresent()) {
            throw new JsonSyntaxException("Error reading status effect: could not find status effect with id: " + effect);
        }
        int duration = JsonHelper.getInt(json, "duration", 100);
        int amplifier = JsonHelper.getInt(json, "amplifier", 0);
        boolean ambient = JsonHelper.getBoolean(json, "is_ambient", false);
        boolean showParticles = JsonHelper.getBoolean(json, "show_particles", true);
        boolean showIcon = JsonHelper.getBoolean(json, "show_icon", true);
        return new StatusEffectInstance(effectOptional.get(), duration, amplifier, ambient, showParticles, showIcon);
    }

    @Environment(EnvType.CLIENT)
    public static StatusEffectInstance readStatusEffect(PacketByteBuf buf) {
        Identifier effect = buf.readIdentifier();
        int duration = buf.readInt();
        int amplifier = buf.readInt();
        boolean ambient = buf.readBoolean();
        boolean showParticles = buf.readBoolean();
        boolean showIcon = buf.readBoolean();
        return new StatusEffectInstance(Registry.STATUS_EFFECT.get(effect), duration, amplifier, ambient, showParticles, showIcon);
    }

    public static void writeStatusEffect(StatusEffectInstance modifier, PacketByteBuf buf) {
        buf.writeIdentifier(Registry.STATUS_EFFECT.getId(modifier.getEffectType()));
        buf.writeInt(modifier.getDuration());
        buf.writeInt(modifier.getAmplifier());
        buf.writeBoolean(modifier.isAmbient());
        buf.writeBoolean(modifier.shouldShowParticles());
        buf.writeBoolean(modifier.shouldShowIcon());
    }

    public static List<List<PlayerCondition>> readPlayerConditions(JsonElement element) {
        List<List<PlayerCondition>> list = new LinkedList<>();
        if(element.isJsonObject()) {
            PlayerCondition cond = PlayerCondition.read(element);
            LinkedList<PlayerCondition> innerList = new LinkedList<>();
            innerList.add(cond);
            list.add(innerList);
        } else if(element.isJsonArray()) {
            JsonArray condAndArray = element.getAsJsonArray();
            condAndArray.forEach(e0 -> {
                LinkedList<PlayerCondition> orList = new LinkedList<>();
                if(e0.isJsonArray()) {
                    JsonArray condOrArray = e0.getAsJsonArray();
                    condOrArray.forEach(e1 -> {
                        if(e1.isJsonObject()) {
                            orList.add(PlayerCondition.read(e1));
                        }
                    });
                }
                if(orList.size() > 0) {
                    list.add(orList);
                }
            });
        }
        return list;
    }

    public static void writePlayerConditions(List<List<PlayerCondition>> conditions, PacketByteBuf buf) {
        buf.writeInt(conditions.size());
        for(List<PlayerCondition> conditionListInner : conditions) {
            buf.writeInt(conditionListInner.size());
            for(PlayerCondition condition : conditionListInner) {
                PlayerCondition.write(condition, buf);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static List<List<PlayerCondition>> readPlayerConditions(PacketByteBuf buf) {
        List<List<PlayerCondition>> conditions = new LinkedList<>();
        int innerListCount = buf.readInt();
        for(int i = 0; i < innerListCount; i++) {
            int conditionCount = buf.readInt();
            List<PlayerCondition> innerList = new ArrayList<>(conditionCount);
            for(int j = 0; j < conditionCount; j++) {
                innerList.add(PlayerCondition.read(buf));
            }
            conditions.add(innerList);
        }
        return conditions;
    }

    public static Predicate<PlayerEntity> buildPlayerConditionPredicate(List<List<PlayerCondition>> conditions) {
        return player ->
            conditions.size() == 0
                || conditions.stream().allMatch(ors -> ors.size() == 0 || ors.stream().anyMatch(condition -> condition.test(player)));
    }

    public static List<List<ItemCondition>> readItemConditions(JsonElement element) {
        List<List<ItemCondition>> list = new LinkedList<>();
        if(element.isJsonObject()) {
            ItemCondition cond = ItemCondition.read(element);
            LinkedList<ItemCondition> innerList = new LinkedList<>();
            innerList.add(cond);
            list.add(innerList);
        } else if(element.isJsonArray()) {
            JsonArray condAndArray = element.getAsJsonArray();
            condAndArray.forEach(e0 -> {
                LinkedList<ItemCondition> orList = new LinkedList<>();
                if(e0.isJsonArray()) {
                    JsonArray condOrArray = e0.getAsJsonArray();
                    condOrArray.forEach(e1 -> {
                        if(e1.isJsonObject()) {
                            orList.add(ItemCondition.read(e1));
                        }
                    });
                }
                if(orList.size() > 0) {
                    list.add(orList);
                }
            });
        }
        return list;
    }

    public static void writeItemConditions(List<List<ItemCondition>> conditions, PacketByteBuf buf) {
        buf.writeInt(conditions.size());
        for(List<ItemCondition> conditionListInner : conditions) {
            buf.writeInt(conditionListInner.size());
            for(ItemCondition condition : conditionListInner) {
                ItemCondition.write(condition, buf);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static List<List<ItemCondition>> readItemConditions(PacketByteBuf buf) {
        List<List<ItemCondition>> conditions = new LinkedList<>();
        int innerListCount = buf.readInt();
        for(int i = 0; i < innerListCount; i++) {
            int conditionCount = buf.readInt();
            List<ItemCondition> innerList = new ArrayList<>(conditionCount);
            for(int j = 0; j < conditionCount; j++) {
                innerList.add(ItemCondition.read(buf));
            }
            conditions.add(innerList);
        }
        return conditions;
    }

    public static Predicate<ItemStack> buildItemConditionPredicate(List<List<ItemCondition>> conditions) {
        return stack ->
            conditions.size() == 0
                || conditions.stream().allMatch(ors -> ors.size() == 0 || ors.stream().anyMatch(condition -> condition.test(stack)));
    }

    public static List<List<BlockCondition>> readBlockConditions(JsonElement element) {
        List<List<BlockCondition>> list = new LinkedList<>();
        if(element.isJsonObject()) {
            BlockCondition cond = BlockCondition.read(element);
            LinkedList<BlockCondition> innerList = new LinkedList<>();
            innerList.add(cond);
            list.add(innerList);
        } else if(element.isJsonArray()) {
            JsonArray condAndArray = element.getAsJsonArray();
            condAndArray.forEach(e0 -> {
                LinkedList<BlockCondition> orList = new LinkedList<>();
                if(e0.isJsonArray()) {
                    JsonArray condOrArray = e0.getAsJsonArray();
                    condOrArray.forEach(e1 -> {
                        if(e1.isJsonObject()) {
                            orList.add(BlockCondition.read(e1));
                        }
                    });
                }
                if(orList.size() > 0) {
                    list.add(orList);
                }
            });
        }
        return list;
    }

    public static void writeBlockConditions(List<List<BlockCondition>> conditions, PacketByteBuf buf) {
        buf.writeInt(conditions.size());
        for(List<BlockCondition> conditionListInner : conditions) {
            buf.writeInt(conditionListInner.size());
            for(BlockCondition condition : conditionListInner) {
                BlockCondition.write(condition, buf);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static List<List<BlockCondition>> readBlockConditions(PacketByteBuf buf) {
        List<List<BlockCondition>> conditions = new LinkedList<>();
        int innerListCount = buf.readInt();
        for(int i = 0; i < innerListCount; i++) {
            int conditionCount = buf.readInt();
            List<BlockCondition> innerList = new ArrayList<>(conditionCount);
            for(int j = 0; j < conditionCount; j++) {
                innerList.add(BlockCondition.read(buf));
            }
            conditions.add(innerList);
        }
        return conditions;
    }

    public static Predicate<CachedBlockPosition> buildBlockConditionPredicate(List<List<BlockCondition>> conditions) {
        return block ->
            conditions.size() == 0
                || conditions.stream().allMatch(ors -> ors.size() == 0 || ors.stream().anyMatch(condition -> condition.test(block)));
    }

    public static List<List<DamageCondition>> readDamageConditions(JsonElement element) {
        List<List<DamageCondition>> list = new LinkedList<>();
        if(element.isJsonObject()) {
            DamageCondition cond = DamageCondition.read(element);
            LinkedList<DamageCondition> innerList = new LinkedList<>();
            innerList.add(cond);
            list.add(innerList);
        } else if(element.isJsonArray()) {
            JsonArray condAndArray = element.getAsJsonArray();
            condAndArray.forEach(e0 -> {
                LinkedList<DamageCondition> orList = new LinkedList<>();
                if(e0.isJsonArray()) {
                    JsonArray condOrArray = e0.getAsJsonArray();
                    condOrArray.forEach(e1 -> {
                        if(e1.isJsonObject()) {
                            orList.add(DamageCondition.read(e1));
                        }
                    });
                }
                if(orList.size() > 0) {
                    list.add(orList);
                }
            });
        }
        return list;
    }

    public static void writeDamageConditions(List<List<DamageCondition>> conditions, PacketByteBuf buf) {
        buf.writeInt(conditions.size());
        for(List<DamageCondition> conditionListInner : conditions) {
            buf.writeInt(conditionListInner.size());
            for(DamageCondition condition : conditionListInner) {
                DamageCondition.write(condition, buf);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static List<List<DamageCondition>> readDamageConditions(PacketByteBuf buf) {
        List<List<DamageCondition>> conditions = new LinkedList<>();
        int innerListCount = buf.readInt();
        for(int i = 0; i < innerListCount; i++) {
            int conditionCount = buf.readInt();
            List<DamageCondition> innerList = new ArrayList<>(conditionCount);
            for(int j = 0; j < conditionCount; j++) {
                innerList.add(DamageCondition.read(buf));
            }
            conditions.add(innerList);
        }
        return conditions;
    }

    public static Predicate<Pair<DamageSource, Float>> buildDamageConditionPredicate(List<List<DamageCondition>> conditions) {
        return damage ->
            conditions.size() == 0
                || conditions.stream().allMatch(ors -> ors.size() == 0 || ors.stream().anyMatch(condition -> condition.test(damage)));
    }

    public static Predicate<DamageSource> buildNoAmountDamageConditionPredicate(List<List<DamageCondition>> conditions) {
        return damage -> {
            Pair<DamageSource, Float> damagePair = new Pair(damage, null);
            return conditions.size() == 0
                || conditions.stream().allMatch(ors -> ors.size() == 0 || ors.stream().anyMatch(condition -> condition.test(damagePair)));
        };
    }
}
