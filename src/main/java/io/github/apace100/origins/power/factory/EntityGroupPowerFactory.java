package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.SetEntityGroupPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.HashMap;

public class EntityGroupPowerFactory extends PowerFactory<SetEntityGroupPower> {

    private static final HashMap<String, EntityGroup> GROUPS;

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "entity_group");
    private final String groupString;
    private final EntityGroup group;

    public EntityGroupPowerFactory(String groupString) {
        this.groupString = groupString;
        if(!GROUPS.containsKey(groupString)) {
            Origins.LOGGER.warn("EntityGroup power was defined with invalid group string: " + groupString);
            this.group = EntityGroup.DEFAULT;
        } else {
            this.group = GROUPS.get(groupString);
        }
    }

    @Override
    public SetEntityGroupPower apply(PowerType<SetEntityGroupPower> powerType, PlayerEntity playerEntity) {
        SetEntityGroupPower power = new SetEntityGroupPower(powerType, playerEntity, group);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<EntityGroupPowerFactory> {

        @Override
        public void write(EntityGroupPowerFactory factory, PacketByteBuf buf) {
            buf.writeString(factory.groupString);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public EntityGroupPowerFactory read(PacketByteBuf buf) {
            EntityGroupPowerFactory factory = new EntityGroupPowerFactory(buf.readString());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public EntityGroupPowerFactory read(JsonObject json) {
            EntityGroupPowerFactory factory = new EntityGroupPowerFactory(JsonHelper.getString(json, "group", "default"));
            super.readConditions(factory, json);
            return factory;
        }
    }

    static {
        GROUPS = new HashMap<>();
        GROUPS.put("default", EntityGroup.DEFAULT);
        GROUPS.put("undead", EntityGroup.UNDEAD);
        GROUPS.put("arthropod", EntityGroup.ARTHROPOD);
        GROUPS.put("illager", EntityGroup.ILLAGER);
        GROUPS.put("aquatic", EntityGroup.AQUATIC);
    }
}
