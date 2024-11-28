package io.github.apace100.origins.origin;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.TextUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.Validatable;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.data.OriginsDataTypes;
import io.github.apace100.origins.registry.ModComponents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Origin implements Validatable {

    public static final Origin EMPTY = Origin.special(Origins.identifier("empty"), ItemStack.EMPTY, Impact.NONE, Integer.MAX_VALUE);
    public static final SerializableDataType<Origin> DATA_TYPE = SerializableDataType.compound(
        new SerializableData()
            .add("id", SerializableDataTypes.IDENTIFIER)
            .add("icon", SerializableDataTypes.UNCOUNTED_ITEM_STACK, ItemStack.EMPTY)
            .add("powers", ApoliDataTypes.POWER_REFERENCE.list(), new ObjectArrayList<>())
            .add("upgrades", OriginsDataTypes.UPGRADES, new ObjectArrayList<>())
            .add("impact", OriginsDataTypes.IMPACT, Impact.NONE)
            .add("name", SerializableDataTypes.TEXT, null)
            .add("description", SerializableDataTypes.TEXT, null)
            .add("unchoosable", SerializableDataTypes.BOOLEAN, false)
            .add("order", SerializableDataTypes.INT, Integer.MAX_VALUE),
        data -> new Origin(
            data.get("id"),
            data.get("icon"),
            data.get("powers"),
            data.get("upgrades"),
            data.get("impact"),
            data.get("name"),
            data.get("description"),
            data.get("unchoosable"),
            data.get("order")
        ),
        (origin, serializableData) -> serializableData.instance()
            .set("id", origin.getId())
            .set("icon", origin.getDisplayItem())
            .set("powers", origin.getPowerReferences())
            .set("upgrades", origin.upgrades)
            .set("impact", origin.getImpact())
            .set("name", origin.getName())
            .set("description", origin.getDescription())
            .set("unchoosable", !origin.isChoosable())
            .set("special", origin.isSpecial())
            .set("order", origin.getOrder())
    );

    private final Identifier id;
    private final ItemStack displayItem;

    private final Set<PowerReference> powerReferences;
    private final Set<Power> powers;

    private final List<OriginUpgrade> upgrades;
    private final Impact impact;

    private final Text name;
    private final Text description;

    private final boolean choosable;
    private final boolean special;

    private final int order;

    protected Origin(Identifier id, ItemStack icon, List<PowerReference> powerReferences, List<OriginUpgrade> upgrades, Impact impact, @Nullable Text name, @Nullable Text description, boolean unchoosable, boolean special, int order) {

        this.id = id;
        String baseTranslationKey = Util.createTranslationKey("origin", id);

        this.displayItem = icon.copy();
        this.powerReferences = new ObjectLinkedOpenHashSet<>(powerReferences);
        this.powers = new ObjectLinkedOpenHashSet<>();
        this.upgrades = upgrades;
        this.impact = impact;
        this.name = TextUtil.forceTranslatable(baseTranslationKey + ".name", Optional.ofNullable(name));
        this.description = TextUtil.forceTranslatable(baseTranslationKey + ".description", Optional.ofNullable(description));
        this.choosable = !unchoosable;
        this.special = special;
        this.order = order;

    }

    public Origin(Identifier id, ItemStack icon, List<PowerReference> powerReferences, List<OriginUpgrade> upgrades, Impact impact, @Nullable Text name, @Nullable Text description, boolean unchoosable, int order) {
        this(id, icon, powerReferences, upgrades, impact, name, description, unchoosable, false, order);
    }

    public static Origin special(Identifier id, ItemStack icon, Impact impact, int order) {
        return new Origin(id, icon, new LinkedList<>(), new LinkedList<>(), impact, null, null, true, true, order);
    }

    public Identifier getId() {
        return id;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public ImmutableList<PowerReference> getPowerReferences() {
        return ImmutableList.copyOf(powerReferences);
    }

    public ImmutableList<Power> getPowers() {
        return ImmutableList.copyOf(powers);
    }

    @Deprecated(forRemoval = true)
    public Optional<OriginUpgrade> getUpgrade(AdvancementEntry advancement) {
        return upgrades.stream()
            .filter(ou -> ou.advancementCondition().equals(advancement.id()))
            .findFirst();
    }

    @Deprecated(forRemoval = true)
    public boolean hasUpgrade() {
        return !this.upgrades.isEmpty();
    }

    public Impact getImpact() {
        return impact;
    }

    public MutableText getName() {
        return name.copy();
    }

    public MutableText getDescription() {
        return description.copy();
    }

    public boolean isChoosable() {
        return this.choosable;
    }

    public boolean isSpecial() {
        return this.special;
    }

    public int getOrder() {
        return this.order;
    }

    @Override
    public void validate() {

        this.powers.clear();
        for (PowerReference powerReference : powerReferences) {

            try {
                powers.add(powerReference.getStrictReference());
            }

            catch (Exception e) {
                Origins.LOGGER.error("Origin \"{}\" contained unregistered power \"{}\"!", id, powerReference.id());
            }

        }

    }

    public boolean hasPower(Power targetPower) {
        return powers.contains(targetPower) || powers
            .stream()
            .filter(MultiplePower.class::isInstance)
            .map(MultiplePower.class::cast)
            .map(MultiplePower::getSubPowerIds)
            .flatMap(Collection::stream)
            .anyMatch(targetPower.getId()::equals);
    }

    @Override
    public String toString() {

        StringBuilder str = new StringBuilder("Origin[id = " + id.toString() + ", powers = {");
        String separator = "";

        for (Power power : powers) {
            str.append(separator).append(power.getId());
            separator = ", ";
        }

        str.append("}]");
        return str.toString();

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Origin other && this.id.equals(other.id));
    }

    public static void init() {

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

}
