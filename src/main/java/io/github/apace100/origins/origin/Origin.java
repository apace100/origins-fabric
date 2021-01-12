package io.github.apace100.origins.origin;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Origin {

    public static final SerializableData DATA = new SerializableData()
        .add("powers", SerializableDataType.IDENTIFIERS)
        .add("icon", SerializableDataType.ITEM, Items.AIR)
        .add("unchoosable", SerializableDataType.BOOLEAN, false)
        .add("order", SerializableDataType.INT, Integer.MAX_VALUE)
        .add("impact", SerializableDataType.IMPACT, Impact.NONE)
        .add("loading_priority", SerializableDataType.INT, 0)
        .add("upgrades", SerializableDataType.UPGRADES, null)
        .add("name", SerializableDataType.STRING, "")
        .add("description", SerializableDataType.STRING, "");

    public static final Origin EMPTY;

    static {
        EMPTY = register(new Origin(new Identifier(Origins.MODID, "empty"), Items.AIR, Impact.NONE, -1, Integer.MAX_VALUE).setUnchoosable().setSpecial());
    }

    public static void init() {

    }

    private static Origin register(Origin origin) {
        return OriginRegistry.register(origin);
    }

    public static HashMap<OriginLayer, Origin> get(Entity entity) {
        if(entity instanceof PlayerEntity) {
            return get((PlayerEntity)entity);
        }
        return new HashMap<>();
    }

    public static HashMap<OriginLayer, Origin> get(PlayerEntity player) {
        return ModComponents.ORIGIN.get(player).getOrigins();
    }

    private Identifier identifier;
    private List<PowerType<?>> powerTypes = new LinkedList<>();
    private final ItemStack displayItem;
    private final Impact impact;
    private boolean isChoosable;
    private final int order;
    private final int loadingPriority;
    private List<OriginUpgrade> upgrades = new LinkedList<>();

    private boolean isSpecial;

    private String nameTranslationKey;
    private String descriptionTranslationKey;

    protected Origin(Identifier id, ItemConvertible item, Impact impact, int order, int loadingPriority) {
        this.identifier = id;
        this.displayItem = new ItemStack(item);
        this.impact = impact;
        this.isChoosable = true;
        this.order = order;
        this.loadingPriority = loadingPriority;
    }

    private Origin addUpgrade(OriginUpgrade upgrade) {
        this.upgrades.add(upgrade);
        return this;
    }

    public boolean hasUpgrade() {
        return this.upgrades.size() > 0;
    }

    public Optional<OriginUpgrade> getUpgrade(Advancement advancement) {
        for(OriginUpgrade upgrade : upgrades) {
            if(upgrade.getAdvancementCondition().equals(advancement.getId())) {
                return Optional.of(upgrade);
            }
        }
        return Optional.empty();
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

    public Origin setSpecial() {
        this.isSpecial = true;
        return this;
    }

    private Origin setName(String name) {
        this.nameTranslationKey = name;
        return this;
    }

    private Origin setDescription(String description) {
        this.descriptionTranslationKey = description;
        return this;
    }

    public boolean hasPowerType(PowerType<?> powerType) {
        if(powerType.getIdentifier() == null) {
            return false;
        }
        return this.powerTypes.contains(powerType);
    }

    public int getLoadingPriority() {
        return this.loadingPriority;
    }

    public boolean isSpecial() {
        return this.isSpecial;
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

    public String getOrCreateNameTranslationKey() {
        if(nameTranslationKey == null || nameTranslationKey.isEmpty()) {
            nameTranslationKey =
                "origin." + identifier.getNamespace() + "." + identifier.getPath() + ".name";
        }
        return nameTranslationKey;
    }

    public TranslatableText getName() {
        return new TranslatableText(getOrCreateNameTranslationKey());
    }

    public String getOrCreateDescriptionTranslationKey() {
        if(descriptionTranslationKey == null || descriptionTranslationKey.isEmpty()) {
            descriptionTranslationKey =
                "origin." + identifier.getNamespace() + "." + identifier.getPath() + ".description";
        }
        return descriptionTranslationKey;
    }

    public TranslatableText getDescription() {
        return new TranslatableText(getOrCreateDescriptionTranslationKey());
    }

    public int getOrder() {
        return this.order;
    }

    public void write(PacketByteBuf buffer) {
        SerializableData.Instance data = DATA.new Instance();
        data.set("icon", displayItem.getItem());
        data.set("impact", impact);
        data.set("order", order);
        data.set("loading_priority", loadingPriority);
        data.set("unchoosable", !this.isChoosable);
        data.set("powers", powerTypes.stream().map(PowerType::getIdentifier).collect(Collectors.toList()));
        data.set("name", getOrCreateNameTranslationKey());
        data.set("description", getOrCreateDescriptionTranslationKey());
        data.set("upgrades", upgrades);
        DATA.write(buffer, data);
    }

    @SuppressWarnings("unchecked")
    public static Origin createFromData(Identifier id, SerializableData.Instance data) {
        Origin origin = new Origin(id,
            (Item)data.get("icon"),
            (Impact)data.get("impact"),
            data.getInt("order"),
            data.getInt("loading_priority"));

        if(data.getBoolean("unchoosable")) {
            origin.setUnchoosable();
        }

        ((List<Identifier>)data.get("powers")).forEach(powerId -> {
            try {
                origin.add(PowerTypeRegistry.get(powerId));
            } catch(IllegalArgumentException e) {
                Origins.LOGGER.error("Origin \"" + id + "\" contained unregistered power: \"" + powerId + "\"");
            }
        });

        if(data.isPresent("upgrades")) {
            ((List<OriginUpgrade>)data.get("upgrades")).forEach(origin::addUpgrade);
        }

        origin.setName(data.getString("name"));
        origin.setDescription(data.getString("description"));

        return origin;
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
                    powers[i] = PowerTypeRegistry.get(id);
                } else {
                    Origins.LOGGER.error("Received invalid power type from server in origin: '" + s + "'.");
                }
            } catch(IllegalArgumentException e) {
                Origins.LOGGER.error("Failed to get power with id " + s + " which was received from the server.");
            }
        }
        origin.add(powers);

        int upgradeCount = buffer.readInt();
        for(int i = 0; i < upgradeCount; i++) {
            origin.addUpgrade(OriginUpgrade.read(buffer));
        }

        origin.setName(buffer.readString());
        origin.setDescription(buffer.readString());

        return origin;
    }

    public static Origin fromJson(Identifier id, JsonObject json) {
        if(!json.has("powers") || !json.get("powers").isJsonArray()) {
            throw new JsonParseException("Origin json requires array with key \"powers\".");
        }
        JsonArray powerArray = json.getAsJsonArray("powers");
        PowerType<?>[] powers = new PowerType<?>[powerArray.size()];
        for (int i = 0; i < powers.length; i++) {
            Identifier powerId = Identifier.tryParse(powerArray.get(i).getAsString());
            if(powerId == null) {
                throw new JsonParseException("Invalid power ID in Origin json: " + powerArray.get(i).getAsString());
            }
            powers[i] = PowerTypeRegistry.get(powerId);
            if (powers[i] == null) {
                throw new JsonParseException("Unregistered power ID in Origin json: " + powerId.toString());
            }
        }
        Identifier iconItemIdentifier = Identifier.tryParse(json.get("icon").getAsString());
        Item icon = Items.AIR;
        if (iconItemIdentifier != null) {
            icon = Registry.ITEM.get(iconItemIdentifier);
        }
        boolean isUnchoosable = JsonHelper.getBoolean(json, "unchoosable", false);
        int orderNum = JsonHelper.getInt(json, "order", Integer.MAX_VALUE);

        JsonElement impactJson = json.get("impact");
        int impactNum = 0;
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
        if(json.has("upgrades") && json.get("upgrades").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("upgrades");
            array.forEach(jsonElement -> origin.addUpgrade(OriginUpgrade.fromJson(jsonElement)));
        }

        if(json.has("name")) {
            origin.setName(JsonHelper.getString(json, "name", ""));
        }

        if(json.has("description")) {
            origin.setDescription(JsonHelper.getString(json, "description", ""));
        }
        return origin;
    }

    @Override
    public String toString() {
        String str = "Origin(" + identifier.toString() + ")[";
        for(PowerType<?> pt : powerTypes) {
            str += PowerTypeRegistry.getId(pt);
            str += ",";
        }
        str = str.substring(0, str.length() - 1) + "]";
        return str;
    }
}
