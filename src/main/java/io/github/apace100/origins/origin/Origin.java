package io.github.apace100.origins.origin;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.LinkedList;
import java.util.List;

public class Origin {

    public static final Origin EMPTY;
    public static final Origin HUMAN;

    static {
        EMPTY = register(new Origin(new Identifier(Origins.MODID, "empty"), Items.AIR, Impact.NONE, -1, Integer.MAX_VALUE).setUnchoosable());
        HUMAN = register(new Origin(new Identifier(Origins.MODID, "human"), Items.PLAYER_HEAD, Impact.NONE, 0, Integer.MAX_VALUE));
    }

    public static void init() {

    }

    private static Origin register(Origin origin) {
        return OriginRegistry.register(origin);
    }

    public static Origin get(Entity entity) {
        if(entity instanceof PlayerEntity) {
            return get((PlayerEntity)entity);
        }
        return Origin.EMPTY;
    }

    public static Origin get(PlayerEntity player) {
        return ModComponents.ORIGIN.get(player).getOrigin();
    }

    private Identifier identifier;
    private List<PowerType<?>> powerTypes = new LinkedList<>();
    private final ItemStack displayItem;
    private final Impact impact;
    private boolean isChoosable;
    private final int order;
    private final int loadingPriority;

    protected Origin(Identifier id, ItemConvertible item, Impact impact, int order, int loadingPriority) {
        this.identifier = id;
        this.displayItem = new ItemStack(item);
        this.impact = impact;
        this.isChoosable = true;
        this.order = order;
        this.loadingPriority = loadingPriority;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    protected Origin add(PowerType<?>... powerTypes) {
        this.powerTypes.addAll(Lists.newArrayList(powerTypes));
        return this;
    }

    protected Origin setUnchoosable() {
        this.isChoosable = false;
        return this;
    }

    public boolean hasPowerType(PowerType<?> powerType) {
        return this.powerTypes.contains(powerType);
    }

    public int getLoadingPriority() {
        return this.loadingPriority;
    }

    public boolean isChoosable() {
        return this.isChoosable;
    }

    public Iterable<PowerType<?>> getPowerTypes() {
        return powerTypes;
    }

    public Impact getImpact() {
        return impact;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public TranslatableText getName() {
        return new TranslatableText("origin." + identifier.getNamespace() + "." + identifier.getPath() + ".name");
    }

    public TranslatableText getDescription() {
        return new TranslatableText("origin." + identifier.getNamespace() + "." + identifier.getPath() + ".description");
    }

    public int getOrder() {
        return this.order;
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeString(identifier.toString());
        buffer.writeString(Registry.ITEM.getId(displayItem.getItem()).toString());
        buffer.writeInt(impact.getImpactValue());
        buffer.writeInt(order);
        buffer.writeInt(loadingPriority);
        buffer.writeBoolean(this.isChoosable);
        buffer.writeInt(this.powerTypes.size());
        for (PowerType<?> powerType : this.powerTypes) {
            buffer.writeString(ModRegistries.POWER_TYPE.getId(powerType).toString());
        }
    }

    @Environment(EnvType.CLIENT)
    public static Origin read(PacketByteBuf buffer) {
        Identifier identifier = Identifier.tryParse(buffer.readString(32767));
        Identifier iconItemId = Identifier.tryParse(buffer.readString(32767));
        Item icon = Items.AIR;
        if(iconItemId != null) {
            icon = Registry.ITEM.get(iconItemId);
        }
        Impact impact = Impact.getByValue(buffer.readInt());
        int order = buffer.readInt();
        int loadingPriority = buffer.readInt();
        Origin origin = new Origin(identifier, icon, impact, order, loadingPriority);
        if(!buffer.readBoolean()) {
            origin.setUnchoosable();
        }
        int powerCount = buffer.readInt();
        PowerType<?>[] powers = new PowerType<?>[powerCount];
        for(int i = 0; i < powerCount; i++) {
            String s = buffer.readString();
            try {
                Identifier id = Identifier.tryParse(s);
                if(id != null) {
                    powers[i] = ModRegistries.POWER_TYPE.get(id);
                } else {
                    Origins.LOGGER.error("Received invalid power type from server in origin: '" + s + "'.");
                }
            } catch(IllegalArgumentException e) {
                System.err.println("Failed to get power with id " + s + " which was received from the server.");
                return null;
            }
        }
        origin.add(powers);

        return origin;
    }

    public static Origin fromJson(Identifier id, JsonObject json) {
        JsonArray powerArray = json.getAsJsonArray("powers");
        PowerType<?>[] powers = new PowerType<?>[powerArray.size()];
        for (int i = 0; i < powers.length; i++) {
            Identifier powerId = Identifier.tryParse(powerArray.get(i).getAsString());
            if (powerId != null) {
                powers[i] = ModRegistries.POWER_TYPE.get(powerId);
            } else {
                Origins.LOGGER.warn("Unknown power ID in json file: " + powerId.toString());
            }
        }
        Identifier iconItemIdentifier = Identifier.tryParse(json.get("icon").getAsString());
        Item icon = Items.AIR;
        if (iconItemIdentifier != null) {
            icon = Registry.ITEM.get(iconItemIdentifier);
        }
        JsonElement unchoosable = json.get("unchoosable");
        boolean isUnchoosable = false;
        if (unchoosable != null) {
            isUnchoosable = unchoosable.getAsBoolean();
        }
        JsonElement order = json.get("order");
        int orderNum = 10000;
        if (order != null) {
            orderNum = order.getAsInt();
        }
        JsonElement impactJson = json.get("impact");
        int impactNum = 2;
        if (impactJson != null) {
            impactNum = impactJson.getAsInt();
            if (impactNum < 0) {
                impactNum = 0;
            }
            if (impactNum > 3) {
                impactNum = 3;
            }
        }
        Impact impact = Impact.getByValue(impactNum);
        int loadingPriority = JsonHelper.getInt(json, "loading_priority", 0);
        Origin origin = new Origin(id, icon, impact, orderNum, loadingPriority);
        if (isUnchoosable) {
            origin.setUnchoosable();
        }
        origin.add(powers);
        return origin;
    }

    @Override
    public String toString() {
        String str = "Origin(" + identifier.toString() + ")[";
        for(PowerType<?> pt : powerTypes) {
            str += ModRegistries.POWER_TYPE.getId(pt);
            str += ",";
        }
        str = str.substring(0, str.length() - 1) + "]";
        return str;
    }
}
