package io.github.apace100.origins.origin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.data.OriginsDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class OriginLayer implements Comparable<OriginLayer> {

    public static final SerializableData DATA = new SerializableData()
        .add("order", SerializableDataTypes.INT, OriginLayers.size())
        .add("loading_priority", SerializableDataTypes.INT, 0)
        .add("origins", OriginsDataTypes.ORIGINS_OR_CONDITIONED_ORIGINS)
        .add("replace", SerializableDataTypes.BOOLEAN, false)
        .add("enabled", SerializableDataTypes.BOOLEAN, true)
        .add("name", SerializableDataTypes.TEXT, null)
        .add("gui_title", OriginsDataTypes.GUI_TITLE, null)
        .add("missing_name", SerializableDataTypes.TEXT, null)
        .add("missing_description", SerializableDataTypes.TEXT, null)
        .add("allow_random", SerializableDataTypes.BOOLEAN, false)
        .add("allow_random_unchoosable", SerializableDataTypes.BOOLEAN, false)
        .add("exclude_random", SerializableDataTypes.IDENTIFIERS, new LinkedList<>())
        .add("replace_exclude_random", SerializableDataTypes.BOOLEAN, false)
        .add("default_origin", SerializableDataTypes.IDENTIFIER, null)
        .add("auto_choose", SerializableDataTypes.BOOLEAN, false)
        .add("hidden", SerializableDataTypes.BOOLEAN, false);

    private int order;
    private int loadingPriority;

    private boolean enabled = false;

    protected Identifier id;

    private List<ConditionedOrigin> conditionedOrigins;
    private boolean replaceConditionedOrigins;

    private String nameTranslationKey;
    private String titleViewOriginTranslationKey;
    private String titleChooseOriginTranslationKey;
    private String missingOriginNameTranslationKey;
    private String missingOriginDescriptionTranslationKey;

    private Text name;
    private Text missingName;
    private Text missingDescription;

    private GuiTitle guiTitle;

    private boolean isRandomAllowed = false;
    private boolean doesRandomAllowUnchoosable = false;
    private List<Identifier> originsExcludedFromRandom = null;
    private boolean replaceOriginsExcludedFromRandom;

    private Identifier defaultOrigin = null;
    private boolean autoChooseIfNoChoice = false;

    private boolean hidden = false;

    public int getOrder() {
        return order;
    }

    public int getLoadingPriority() {
        return loadingPriority;
    }

    protected List<ConditionedOrigin> getConditionedOrigins() {
        return conditionedOrigins;
    }

    public String getOrCreateTranslationKey() {

        if(nameTranslationKey == null || nameTranslationKey.isEmpty()) {
            this.nameTranslationKey = "layer." + id.getNamespace() + "." + id.getPath() + ".name";
        }

        return nameTranslationKey;

    }

    public String getTranslationKey() {
        return getOrCreateTranslationKey();
    }

    public Text getName() {
        return name != null ? name : Text.translatable(getTranslationKey());
    }

    @Nullable
    public Text getMissingName() {
        return missingName != null ? missingName : null;
    }

    public String getMissingOriginNameTranslationKey() {

        if (missingOriginNameTranslationKey == null || missingOriginNameTranslationKey.isEmpty()) {
            this.missingOriginNameTranslationKey = "layer." + id.getNamespace() + "." + id.getPath() + ".missing_origin.name";
        }

        return missingOriginNameTranslationKey;

    }

    @Nullable
    public Text getMissingDescription() {
        return missingDescription != null ? missingDescription : null;
    }

    public String getMissingOriginDescriptionTranslationKey() {

        if (missingOriginDescriptionTranslationKey == null || missingOriginDescriptionTranslationKey.isEmpty()) {
            this.missingOriginDescriptionTranslationKey = "layer." + id.getNamespace() + "." + id.getPath() + ".missing_origin.description";
        }

        return missingOriginDescriptionTranslationKey;

    }

    public Text getViewOriginTitle() {
        return guiTitle != null && guiTitle.viewOrigin != null ? guiTitle.viewOrigin : Text.translatable(getTitleViewOriginTranslationKey());
    }

    public String getTitleViewOriginTranslationKey() {

        if (titleViewOriginTranslationKey == null || titleViewOriginTranslationKey.isEmpty()) {
            this.titleViewOriginTranslationKey = "layer." + id.getNamespace() + "." + id.getPath() + ".view_origin.name";
        }

        return titleViewOriginTranslationKey;

    }

    public boolean shouldOverrideViewOriginTitle() {
        return guiTitle != null;
    }

    public Text getChooseOriginTitle() {
        return guiTitle != null && guiTitle.chooseOrigin != null ? guiTitle.chooseOrigin : Text.translatable(getTitleChooseOriginTranslationKey());
    }

    public String getTitleChooseOriginTranslationKey() {

        if (titleChooseOriginTranslationKey == null || titleChooseOriginTranslationKey.isEmpty()) {
            this.titleChooseOriginTranslationKey = "layer." + id.getNamespace() + "." + id.getPath() + ".choose_origin.name";
        }

        return titleChooseOriginTranslationKey;

    }

    public boolean shouldOverrideChooseOriginTitle() {
        return guiTitle != null;
    }

    public boolean shouldReplaceConditionedOrigins() {
        return replaceConditionedOrigins;
    }

    public boolean shouldReplaceExcludedOriginsFromRandom() {
        return replaceOriginsExcludedFromRandom;
    }

    public Identifier getIdentifier() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasDefaultOrigin() {
        return defaultOrigin != null;
    }

    public Identifier getDefaultOrigin() {
        return defaultOrigin;
    }

    public boolean shouldAutoChoose() {
        return autoChooseIfNoChoice;
    }

    public List<Identifier> getOrigins() {
        return getOrigins(null);
    }

    public List<Identifier> getOrigins(@Nullable PlayerEntity playerEntity) {
        return conditionedOrigins
            .stream()
            .filter(co -> playerEntity == null || co.isConditionFulfilled(playerEntity))
            .flatMap(co -> co.origins.stream())
            .filter(OriginRegistry::contains)
            .collect(Collectors.toList());
    }

    public int getOriginOptionCount(PlayerEntity playerEntity) {

        int choosableOrigins = (int) getOrigins(playerEntity)
            .stream()
            .map(OriginRegistry::get)
            .filter(Origin::isChoosable)
            .count();

        if (choosableOrigins > 1 && (isRandomAllowed && !getRandomOrigins(playerEntity).isEmpty())) {
            choosableOrigins++;
        }

        return choosableOrigins;

    }

    public boolean contains(Identifier originId) {
        return conditionedOrigins
            .stream()
            .flatMap(co -> co.origins().stream())
            .anyMatch(originId::equals);
    }

    public boolean contains(Origin origin) {
        return contains(origin.getIdentifier());
    }

    public boolean contains(Identifier originId, PlayerEntity playerEntity) {
        return conditionedOrigins
            .stream()
            .filter(co -> co.isConditionFulfilled(playerEntity))
            .flatMap(co -> co.origins().stream())
            .anyMatch(originId::equals);
    }

    public boolean contains(Origin origin, PlayerEntity playerEntity) {
        return contains(origin.getIdentifier(), playerEntity);
    }

    public boolean isRandomAllowed() {
        return isRandomAllowed;
    }

    public boolean isHidden() {
        return hidden;
    }

    public List<Identifier> getRandomOrigins(PlayerEntity playerEntity) {
        return conditionedOrigins
            .stream()
            .filter(co -> co.isConditionFulfilled(playerEntity))
            .flatMap(co -> co.origins.stream())
            .filter(OriginRegistry::contains)
            .filter(oId -> !originsExcludedFromRandom.contains(oId))
            .filter(oid -> doesRandomAllowUnchoosable || OriginRegistry.get(oid).isChoosable())
            .collect(Collectors.toList());
    }

    public SerializableData.Instance toData() {

        SerializableData.Instance data = DATA.new Instance();

        data.set("order", order);
        data.set("loading_priority", loadingPriority);
        data.set("origins", conditionedOrigins);
        data.set("replace", replaceConditionedOrigins);
        data.set("enabled", enabled);
        data.set("name", name);
        data.set("gui_title", guiTitle);
        data.set("missing_name", missingName);
        data.set("missing_description", missingDescription);
        data.set("allow_random", isRandomAllowed);
        data.set("allow_random_unchoosable", doesRandomAllowUnchoosable);
        data.set("exclude_random", originsExcludedFromRandom);
        data.set("replace_exclude_random", replaceOriginsExcludedFromRandom);
        data.set("default_origin", defaultOrigin);
        data.set("auto_choose", autoChooseIfNoChoice);
        data.set("hidden", hidden);

        return data;

    }

    public static OriginLayer fromData(SerializableData.Instance data) {

        OriginLayer layer = new OriginLayer();

        layer.order = data.get("order");
        layer.loadingPriority = data.get("loading_priority");
        layer.conditionedOrigins = data.get("origins");
        layer.replaceConditionedOrigins = data.get("replace");
        layer.enabled = data.get("enabled");
        layer.name = data.get("name");
        layer.guiTitle = data.get("gui_title");
        layer.missingName = data.get("missing_name");
        layer.missingDescription = data.get("missing_description");
        layer.isRandomAllowed = data.get("allow_random");
        layer.doesRandomAllowUnchoosable = data.get("allow_random_unchoosable");
        layer.originsExcludedFromRandom = data.get("exclude_random");
        layer.replaceOriginsExcludedFromRandom = data.get("replace_exclude_random");
        layer.defaultOrigin = data.get("default_origin");
        layer.autoChooseIfNoChoice = data.get("auto_choose");
        layer.hidden = data.get("hidden");

        return layer;

    }

    public void merge(JsonObject json) {
        merge(fromData(DATA.read(json)));
    }

    public void merge(OriginLayer otherLayer) {

        this.order = otherLayer.order;
        this.enabled = otherLayer.enabled;

        if (otherLayer.shouldReplaceConditionedOrigins()) {
            this.conditionedOrigins.clear();
        }

        otherLayer.conditionedOrigins
            .stream()
            .filter(Predicate.not(this.conditionedOrigins::contains))
            .forEach(this.conditionedOrigins::add);

        this.name = otherLayer.name;
        this.guiTitle = otherLayer.guiTitle;
        this.missingName = otherLayer.missingName;
        this.missingDescription = otherLayer.missingDescription;
        this.isRandomAllowed = otherLayer.isRandomAllowed;
        this.doesRandomAllowUnchoosable = otherLayer.doesRandomAllowUnchoosable;

        if (otherLayer.shouldReplaceExcludedOriginsFromRandom()) {
            this.originsExcludedFromRandom.clear();
        }

        otherLayer.originsExcludedFromRandom
            .stream()
            .filter(Predicate.not(this.originsExcludedFromRandom::contains))
            .forEach(this.originsExcludedFromRandom::add);

        this.defaultOrigin = otherLayer.defaultOrigin;
        this.autoChooseIfNoChoice = otherLayer.autoChooseIfNoChoice;
        this.hidden = otherLayer.hidden;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OriginLayer other && Objects.deepEquals(this.id, other.id));
    }

    @Override
    public int compareTo(OriginLayer o) {
        return Integer.compare(order, o.order);
    }

    public void write(PacketByteBuf buffer) {
        DATA.write(buffer, toData());
    }

    public static OriginLayer read(PacketByteBuf buffer) {
        return fromData(DATA.read(buffer));
    }

    public static OriginLayer fromJson(Identifier id, JsonObject json) {

        OriginLayer layer = fromData(DATA.read(json));
        layer.id = id;

        return layer;

    }

    public record GuiTitle(@Nullable Text viewOrigin, @Nullable Text chooseOrigin) {

        public static final SerializableData DATA = new SerializableData()
            .add("view_origin", SerializableDataTypes.TEXT, null)
            .add("choose_origin", SerializableDataTypes.TEXT, null);

        public SerializableData.Instance toData() {

            SerializableData.Instance data = DATA.new Instance();

            data.set("view_origin", viewOrigin);
            data.set("choose_origin", chooseOrigin);

            return data;

        }

        public static GuiTitle fromData(SerializableData.Instance data) {
            return new GuiTitle(data.get("view_origin"), data.get("choose_origin"));
        }

    }

    public record ConditionedOrigin(ConditionFactory<Entity>.Instance condition, List<Identifier> origins) {

        public static final SerializableData DATA = new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
            .add("origins", SerializableDataTypes.IDENTIFIERS);

        public boolean isConditionFulfilled(PlayerEntity playerEntity) {
            return condition == null || condition.test(playerEntity);
        }

        public SerializableData.Instance toData() {

            SerializableData.Instance data = DATA.new Instance();

            data.set("condition", condition);
            data.set("origins", origins);

            return data;

        }

        public static ConditionedOrigin fromData(SerializableData.Instance data) {
            return new ConditionedOrigin(data.get("condition"), data.get("origins"));
        }

        @Deprecated
        public ConditionFactory<Entity>.Instance getCondition() {
            return condition;
        }

        @Deprecated
        public List<Identifier> getOrigins() {
            return origins;
        }

        public void write(PacketByteBuf buffer) {
            OriginsDataTypes.CONDITIONED_ORIGIN.send(buffer, this);
        }

        public static ConditionedOrigin read(PacketByteBuf buffer) {
            return OriginsDataTypes.ORIGIN_OR_CONDITIONED_ORIGIN.receive(buffer);
        }

        public static ConditionedOrigin read(JsonElement element) {
            return OriginsDataTypes.ORIGIN_OR_CONDITIONED_ORIGIN.read(element);
        }

    }

}
