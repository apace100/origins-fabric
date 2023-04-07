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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Origin {

    public static final SerializableData DATA = new SerializableData()
        .add("powers", SerializableDataTypes.IDENTIFIERS, Lists.newArrayList())
        .add("icon", CompatibilityDataTypes.ITEM_OR_ITEM_STACK, new ItemStack(Items.AIR))
        .add("unchoosable", SerializableDataTypes.BOOLEAN, false)
        .add("order", SerializableDataTypes.INT, Integer.MAX_VALUE)
        .add("impact", OriginsDataTypes.IMPACT, Impact.NONE)
        .add("loading_priority", SerializableDataTypes.INT, 0)
        .add("upgrades", OriginsDataTypes.UPGRADES, null)
        .add("name", SerializableDataTypes.STRING, "")
        .add("description", SerializableDataTypes.STRING, "");

    public static final Origin EMPTY;

    static {
        EMPTY = register(new Origin(new Identifier(Origins.MODID, "empty"), new ItemStack(Items.AIR), Impact.NONE, -1, Integer.MAX_VALUE).setUnchoosable().setSpecial());
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

    public Origin setDescription(String description) {
        this.descriptionTranslationKey = description;
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

    public MutableText getName() {
        return Text.translatable(getOrCreateNameTranslationKey());
    }

    public String getOrCreateDescriptionTranslationKey() {
        if(descriptionTranslationKey == null || descriptionTranslationKey.isEmpty()) {
            descriptionTranslationKey =
                "origin." + identifier.getNamespace() + "." + identifier.getPath() + ".description";
        }
        return descriptionTranslationKey;
    }

    public MutableText getDescription() {
        return Text.translatable(getOrCreateDescriptionTranslationKey());
    }

    public int getOrder() {
        return this.order;
    }

    public void write(PacketByteBuf buffer) {
        SerializableData.Instance data = DATA.new Instance();
        data.set("icon", displayItem);
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
            (ItemStack)data.get("icon"),
            (Impact)data.get("impact"),
            data.getInt("order"),
            data.getInt("loading_priority"));

        if(data.getBoolean("unchoosable")) {
            origin.setUnchoosable();
        }

        ((List<Identifier>)data.get("powers")).forEach(powerId -> {
            try {
                PowerType powerType = PowerTypeRegistry.get(powerId);
                origin.add(powerType);
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
        return createFromData(identifier, DATA.read(buffer));
    }

    public static Origin fromJson(Identifier id, JsonObject json) {
        return createFromData(id, DATA.read(json));
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

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Origin) {
            return ((Origin)obj).identifier.equals(identifier);
        }
        return false;
    }
}
