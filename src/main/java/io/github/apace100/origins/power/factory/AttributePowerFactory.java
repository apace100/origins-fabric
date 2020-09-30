package io.github.apace100.origins.power.factory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.AttributePower;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AttributePowerFactory extends PowerFactory<AttributePower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "attribute");
    private final List<Mod> modifiers;

    public AttributePowerFactory(List<Mod> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public AttributePower apply(PowerType<AttributePower> powerType, PlayerEntity playerEntity) {
        AttributePower attrPower = new AttributePower(powerType, playerEntity);
        modifiers.forEach(mod -> attrPower.addModifier(mod.attribute, mod.modifier));
        return attrPower;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<AttributePowerFactory> {

        @Override
        public void write(AttributePowerFactory factory, PacketByteBuf buf) {
            buf.writeInt(factory.modifiers.size());
            factory.modifiers.forEach(mod -> {
                buf.writeString(Registry.ATTRIBUTE.getId(mod.attribute).toString());
                SerializationHelper.writeAttributeModifier(mod.modifier, buf);
            });
        }

        @Environment(EnvType.CLIENT)
        @Override
        public AttributePowerFactory read(PacketByteBuf buf) {
            int modCount = buf.readInt();
            List<Mod> modList = new ArrayList<>(modCount);
            for(int i = 0; i < modCount; i++) {
                Identifier attrId = Identifier.tryParse(buf.readString());
                EntityAttribute attribute = null;
                try {
                    attribute = Registry.ATTRIBUTE.get(attrId);
                } catch (Exception e) {
                    Origins.LOGGER.error("Failed to receive AttributePowerFactory from server, missing attribute: " + attrId.toString());
                }
                EntityAttributeModifier modifier = SerializationHelper.readAttributeModifier(buf);
                if(attribute != null) {
                    Mod mod = new Mod();
                    mod.attribute = attribute;
                    mod.modifier = modifier;
                    modList.add(mod);
                }
            }
            AttributePowerFactory factory = new AttributePowerFactory(modList);
            return factory;
        }

        @Override
        public AttributePowerFactory read(JsonObject json) {
            List<Mod> mods = new LinkedList<>();
            if(json.has("modifier")) {
                mods.add(readMod(json.get("modifier")));
            } else if(json.has("modifiers") && json.get("modifiers").isJsonArray()) {
                JsonArray modArray = json.getAsJsonArray("modifiers");
                modArray.forEach(je -> {
                    mods.add(readMod(je));
                });
            }
            return new AttributePowerFactory(mods);
        }

        private Mod readMod(JsonElement element) {
            if(!element.isJsonObject()) {
                throw new JsonParseException("AttributePower modifier needs to be a JSON object.");
            }
            JsonObject json = element.getAsJsonObject();
            if(!json.has("attribute")) {
                throw new JsonParseException("AttributePower modifier requires `attribute` key which specifies the ID of the attribute.");
            }
            Identifier attrId = Identifier.tryParse(json.get("attribute").getAsString());
            EntityAttribute attribute = null;
            try {
                attribute = Registry.ATTRIBUTE.get(attrId);
            } catch (Exception e) {
                throw new JsonParseException("AttributePower modifier defined unregistered attribute ID: " + attrId.toString());
            }
            EntityAttributeModifier attrMod = SerializationHelper.readAttributeModifier(json);
            Mod mod = new Mod();
            mod.attribute = attribute;
            mod.modifier = attrMod;
            return mod;
        }
    }

    private static class Mod {
        private EntityAttribute attribute;
        private EntityAttributeModifier modifier;
    }
}
