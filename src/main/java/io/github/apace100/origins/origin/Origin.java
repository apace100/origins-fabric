package io.github.apace100.origins.origin;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
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
import io.github.apace100.origins.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class Origin implements Comparable<Origin>, Validatable {

    private static final Set<Origin> SPECIALS = new ObjectOpenHashSet<>();

    public static final Origin EMPTY = new Origin(Origins.identifier("empty"), () -> ItemStack.EMPTY, Impact.NONE, Integer.MIN_VALUE);
    public static final Origin RANDOM = new Origin(Origins.identifier("random"), () -> Suppliers.memoize(() -> ModItems.ORB_OF_ORIGIN).get().getDefaultStack(), Impact.NONE, Integer.MAX_VALUE);

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
        data -> Origin.of(
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
    private final Supplier<ItemStack> displayItem;

    private final Set<PowerReference> powerReferences;
    private final Set<Power> powers;

    //  TODO: Allow users to modify the window textures via data packs
    //        (delayed since it's already pre-release/release)  -eggohito
    private final GuiMetadata guiMetadata;
    private final List<OriginUpgrade> upgrades;
    private final Impact impact;

    private final Text name;
    private final Text description;

    private final boolean choosable;
    private final int order;

    protected Origin(Identifier id, Supplier<ItemStack> icon, List<PowerReference> powerReferences, GuiMetadata guiMetadata, List<OriginUpgrade> upgrades, Impact impact, @Nullable Text name, @Nullable Text description, boolean unchoosable, int order) {

        this.id = id;
        String baseTranslationKey = Util.createTranslationKey("origin", id);

        this.displayItem = Suppliers.compose(ItemStack::copy, icon::get);
        this.powerReferences = new ObjectLinkedOpenHashSet<>(powerReferences);
        this.powers = new ObjectLinkedOpenHashSet<>();
        this.guiMetadata = guiMetadata;
        this.upgrades = upgrades;
        this.impact = impact;
        this.name = TextUtil.forceTranslatable(baseTranslationKey + ".name", Optional.ofNullable(name));
        this.description = TextUtil.forceTranslatable(baseTranslationKey + ".description", Optional.ofNullable(description));
        this.choosable = !unchoosable;
        this.order = order;

    }

    protected Origin(Identifier id, Supplier<ItemStack> icon, Impact impact, int order) {
        this(id, icon, List.of(), GuiMetadata.DEFAULT, List.of(), impact, null, null, false, order);
        SPECIALS.add(this);
    }

    public static Origin of(Identifier id, ItemStack icon, List<PowerReference> powers, GuiMetadata windowTextures, List<OriginUpgrade> upgrades, Impact impact, @Nullable Text name, @Nullable Text description, boolean unchoosable, int order) {
        return new Origin(id, () -> icon, powers, windowTextures, upgrades, impact, name, description,  unchoosable, order);
    }

    public static Origin of(Identifier id, ItemStack icon, List<PowerReference> powers, List<OriginUpgrade> upgrades, Impact impact, @Nullable Text name, @Nullable Text description, boolean unchoosable, int order) {
        return new Origin(id, () -> icon, powers, GuiMetadata.DEFAULT, upgrades, impact, name, description,  unchoosable, order);
    }

    //  TODO: Add a config that determines how origins should be sorted -eggohito
    @Override
    public int compareTo(@NotNull Origin that) {
        int impactDelta = this.getImpact().compareTo(that.getImpact());
        return impactDelta != 0
            ? impactDelta
            : Integer.compare(this.getOrder(), that.getOrder());
    }

    public Identifier getId() {
        return id;
    }

    public ItemStack getDisplayItem() {
        return displayItem.get();
    }

    public ImmutableList<PowerReference> getPowerReferences() {
        return ImmutableList.copyOf(powerReferences);
    }

    public ImmutableList<Power> getPowers() {
        return ImmutableList.copyOf(powers);
    }

    public GuiMetadata getGuiMetadata() {
        return guiMetadata;
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
        return SPECIALS.contains(this);
    }

    public int getOrder() {
        return this.order;
    }

    @Override
    public void validate() {

        this.powers.clear();
        for (PowerReference powerReference : powerReferences) {
            if (Origins.config.isPowerDisabled(this.id, powerReference.id())) {
                continue;
            }

            try {
                powers.add(powerReference.getPower());
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

    public static Set<Origin> getSpecials() {
        return new ObjectOpenHashSet<>(SPECIALS);
    }

    public record GuiMetadata(WindowTextures choosing, WindowTextures viewing) {

        public static final GuiMetadata DEFAULT = new GuiMetadata(
            WindowTextures.DEFAULT,
            WindowTextures.DEFAULT
        );

        public static final TypedDataObjectFactory<GuiMetadata> DATA_FACTORY = TypedDataObjectFactory.simple(
            new SerializableData()
                .add("choosing", WindowTextures.DATA_FACTORY.getDataType())
                .add("viewing", WindowTextures.DATA_FACTORY.getDataType()),
            data -> new GuiMetadata(
                data.get("choosing"),
                data.get("viewing")
            ),
            (metadata, serializableData) -> serializableData.instance()
                .set("choosing", metadata.choosing())
                .set("viewing", metadata.viewing())
        );

    }

    public record WindowTextures(Identifier background, Identifier border, Identifier namePlate, ScrollerTextures scroller) {

        public static final WindowTextures DEFAULT = new WindowTextures(
            Origins.identifier("choose_origin/background"),
            Origins.identifier("choose_origin/border"),
            Origins.identifier("choose_origin/name_plate"),
            ScrollerTextures.DEFAULT
        );

        public static final TypedDataObjectFactory<WindowTextures> DATA_FACTORY = TypedDataObjectFactory.simple(
            new SerializableData()
                .add("background", SerializableDataTypes.IDENTIFIER)
                .add("border", SerializableDataTypes.IDENTIFIER)
                .add("name_plate", SerializableDataTypes.IDENTIFIER)
                .add("scroller", ScrollerTextures.DATA_FACTORY.getDataType()),
            data -> new WindowTextures(
                data.get("background"),
                data.get("border"),
                data.get("name_plate"),
                data.get("scroller")
            ),
            (metadata, serializableData) -> serializableData.instance()
                .set("background", metadata.background())
                .set("border", metadata.border())
                .set("name_plate", metadata.namePlate())
                .set("scroller", metadata.scroller())
        );

    }

    public record ScrollerTextures(Identifier unpressed, Identifier pressed, Identifier slot) {

        public static final ScrollerTextures DEFAULT = new ScrollerTextures(
            Origins.identifier("choose_origin/scroll_bar"),
            Origins.identifier("choose_origin/scroll_bar/pressed"),
            Origins.identifier("choose_origin/scroll_bar/slot")
        );

        public static final TypedDataObjectFactory<ScrollerTextures> DATA_FACTORY = TypedDataObjectFactory.simple(
            new SerializableData()
                .add("unpressed", SerializableDataTypes.IDENTIFIER)
                .add("pressed", SerializableDataTypes.IDENTIFIER)
                .add("slot", SerializableDataTypes.IDENTIFIER),
            data -> new ScrollerTextures(
                data.get("unpressed"),
                data.get("pressed"),
                data.get("slot")
            ),
            (metadata, serializableData) -> serializableData.instance()
                .set("unpressed", metadata.unpressed())
                .set("pressed", metadata.pressed())
                .set("slot", metadata.slot())
        );

    }
}
