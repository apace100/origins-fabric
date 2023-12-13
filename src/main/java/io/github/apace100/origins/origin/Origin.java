package io.github.apace100.origins.origin;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.data.CompatibilityDataTypes;
import io.github.apace100.origins.data.OriginsDataTypes;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class Origin {

    public static final SerializableData DATA = new SerializableData()
        .add("powers", SerializableDataTypes.IDENTIFIERS, Lists.newArrayList())
        .add("icon", CompatibilityDataTypes.ITEM_OR_ITEM_STACK, new ItemStack(Items.AIR))
        .add("unchoosable", SerializableDataTypes.BOOLEAN, false)
        .add("order", SerializableDataTypes.INT, Integer.MAX_VALUE)
        .add("impact", OriginsDataTypes.IMPACT, Impact.NONE)
        .add("loading_priority", SerializableDataTypes.INT, 0)
        .add("upgrades", OriginsDataTypes.UPGRADES, null)
        .add("name", SerializableDataTypes.TEXT, null)
        .add("description", SerializableDataTypes.TEXT, null);

    public static final Origin EMPTY;

    static {
        EMPTY = register(new Origin(new Identifier(Origins.MODID, "empty"), new ItemStack(Items.AIR), Impact.NONE, -1, Integer.MAX_VALUE).setUnchoosable().setSpecial());
    }

    public static void init() {

    }

    private static Origin register(Origin origin) {
        return OriginRegistry.register(origin);
    }

    public static Map<OriginLayer, Origin> get(Entity entity) {
        if(entity instanceof PlayerEntity) {
            return get((PlayerEntity)entity);
        }
        return new HashMap<>();
    }

    public static Map<OriginLayer, Origin> get(PlayerEntity player) {
        return ModComponents.ORIGIN.get(player).getOrigins();
    }

    private final List<OriginUpgrade> upgrades = new LinkedList<>();
    private final List<PowerType<?>> powerTypes = new LinkedList<>();
    private final Identifier identifier;
    private final ItemStack displayItem;
    private final Impact impact;

    private String nameTranslationKey;
    private String descriptionTranslationKey;

    private Text name;
    private Text description;

    private final int loadingPriority;
    private final int order;

    private boolean isChoosable;
    private boolean isSpecial;


    public Origin(Identifier id, ItemStack icon, Impact impact, int order, int loadingPriority) {
        this.identifier = id;
        this.displayItem = icon.copy();
        this.impact = impact;
        this.isChoosable = true;
        this.order = order;
        this.loadingPriority = loadingPriority;
    }

    public Origin addUpgrade(OriginUpgrade upgrade) {
        this.upgrades.add(upgrade);
        return this;
    }

    public boolean hasUpgrade() {
        return !this.upgrades.isEmpty();
    }

    public Optional<OriginUpgrade> getUpgrade(AdvancementEntry advancement) {
        return upgrades.stream()
            .filter(ou -> ou.advancementCondition().equals(advancement.id()))
            .findFirst();
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Origin add(PowerType<?>... powerTypes) {
        this.powerTypes.addAll(Lists.newArrayList(powerTypes));
        return this;
    }

    public void removePowerType(PowerType<?> powerType) {
        this.powerTypes.remove(powerType);
    }

    protected Origin setUnchoosable() {
        this.isChoosable = false;
        return this;
    }

    public Origin setSpecial() {
        this.isSpecial = true;
        return this;
    }

    public Origin setName(String name) {
        this.nameTranslationKey = name;
        return this;
    }

    public Origin setNameText(Text name) {
        this.name = name;
        return this;
    }

    public Origin setDescription(String description) {
        this.descriptionTranslationKey = description;
        return this;
    }

    public Origin setDescriptionText(Text description) {
        this.description = description;
        return this;
    }

    public boolean hasPowerType(PowerType<?> powerType) {
        if(powerType.getIdentifier() == null) {
            return false;
        }
        if(this.powerTypes.contains(powerType)) {
            return true;
        }
        for (PowerType<?> pt : this.powerTypes) {
            if (pt instanceof MultiplePowerType) {
                if(((MultiplePowerType<?>)pt).getSubPowers().contains(powerType.getIdentifier())) {
                    return true;
                }
            }
        }
        return false;
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

    public List<PowerType<?>> getPowerTypes() {
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
            nameTranslationKey = "origin." + identifier.getNamespace() + "." + identifier.getPath() + ".name";
        }
        return nameTranslationKey;
    }

    public MutableText getName() {
        return name != null ? name.copy() : Text.translatable(getOrCreateNameTranslationKey());
    }

    public String getOrCreateDescriptionTranslationKey() {
        if(descriptionTranslationKey == null || descriptionTranslationKey.isEmpty()) {
            descriptionTranslationKey = "origin." + identifier.getNamespace() + "." + identifier.getPath() + ".description";
        }
        return descriptionTranslationKey;
    }

    public MutableText getDescription() {
        return description != null ? description.copy() : Text.translatable(getOrCreateDescriptionTranslationKey());
    }

    public int getOrder() {
        return this.order;
    }

    public SerializableData.Instance toData() {

        SerializableData.Instance data = DATA.new Instance();

        data.set("powers", powerTypes.stream().map(PowerType::getIdentifier).toList());
        data.set("icon", displayItem);
        data.set("unchoosable", !isChoosable);
        data.set("order", order);
        data.set("impact", impact);
        data.set("loading_priority", loadingPriority);
        data.set("upgrades", upgrades);
        data.set("name", getName());
        data.set("description", getDescription());

        return data;

    }

    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(identifier);
        DATA.write(buffer, toData());
    }

    @SuppressWarnings("unchecked")
    public static Origin createFromData(Identifier id, SerializableData.Instance data) {

        Origin origin = new Origin(
            id,
            data.get("icon"),
            data.get("impact"),
            data.getInt("order"),
            data.getInt("loading_priority")
        );

        if(data.getBoolean("unchoosable")) {
            origin.setUnchoosable();
        }

        ((List<Identifier>)data.get("powers")).forEach(powerId -> {
            try {
                PowerType<?> powerType = PowerTypeRegistry.get(powerId);
                origin.add(powerType);
            } catch(IllegalArgumentException e) {
                Origins.LOGGER.error("Origin \"" + id + "\" contained unregistered power: \"" + powerId + "\"");
            }
        });

        if(data.isPresent("upgrades")) {
            ((List<OriginUpgrade>)data.get("upgrades")).forEach(origin::addUpgrade);
        }

        origin.setNameText(data.get("name"));
        origin.setDescriptionText(data.get("description"));

        return origin;
    }

    public static Origin read(PacketByteBuf buffer) {
        Identifier id = new Identifier(buffer.readString());
        return createFromData(id, DATA.read(buffer));
    }

    public static Origin fromJson(Identifier id, JsonObject json) {
        return createFromData(id, DATA.read(json));
    }

    public JsonObject toJson() {
        return DATA.write(toData());
    }

    @Override
    public String toString() {

        StringBuilder str = new StringBuilder("Origin[id = " + identifier.toString() + ", powers = {");
        String separator = "";

        for (PowerType<?> powerType : powerTypes) {
            str.append(separator).append(powerType.getIdentifier());
            separator = ", ";
        }

        str.append("}]");
        return str.toString();

    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Origin other && this.identifier.equals(other.identifier));
    }

}
