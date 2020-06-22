package io.github.apace100.origins.origin;

import com.google.common.collect.Lists;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedList;
import java.util.List;

public class Origin {

    public static final Origin EMPTY;
    public static final Origin HUMAN;
    public static final Origin MERLING;
    public static final Origin ARACHNID;
    public static final Origin BLAZEBORN;
    public static final Origin AVIAN;
    public static final Origin PHANTOM;

    static {
        EMPTY = register("empty", new Origin(Items.AIR, Impact.NONE, -1).setUnchoosable().add(PowerTypes.INVULNERABILITY));
        HUMAN = register("human", new Origin(Items.PLAYER_HEAD, Impact.NONE, 0));
        MERLING = register("merling", new Origin(Items.COD, Impact.HIGH, 0).add(
            PowerTypes.WATER_BREATHING,
            PowerTypes.AQUA_AFFINITY,
            PowerTypes.WATER_VISION,
            PowerTypes.SWIM_SPEED,
            PowerTypes.LIKE_WATER
        ));
        ARACHNID = register("arachnid", new Origin(Items.COBWEB, Impact.LOW, 1).add(
            PowerTypes.CLIMBING,
            PowerTypes.WEBBING,
            PowerTypes.FRAGILE,
            PowerTypes.NO_COBWEB_SLOWDOWN,
            PowerTypes.CARNIVORE,
            PowerTypes.ARTHROPOD
        ));
        BLAZEBORN = register("blazeborn", new Origin(Items.BLAZE_POWDER, Impact.HIGH, 1).add(
            PowerTypes.FIRE_IMMUNITY,
            PowerTypes.NETHER_SPAWN,
            PowerTypes.BURNING_WRATH,
            PowerTypes.WATER_VULNERABILITY,
            PowerTypes.FLAME_PARTICLES
        ));
        AVIAN = register("avian", new Origin(Items.FEATHER, Impact.LOW, 0).add(
            PowerTypes.SLOW_FALLING,
            PowerTypes.TAILWIND,
            PowerTypes.FRESH_AIR,
            PowerTypes.VEGETARIAN
        ));
        PHANTOM = register("phantom", new Origin(Items.PHANTOM_MEMBRANE, Impact.HIGH, 2).add(
            PowerTypes.PHASING,
            PowerTypes.INVISIBILITY,
            PowerTypes.HUNGER_OVER_TIME,
            PowerTypes.BURN_IN_DAYLIGHT
        ));
    }

    public static void init() {

    }

    private static Origin register(String path, Origin origin) {
        return Registry.register(ModRegistries.ORIGIN, new Identifier(Origins.MODID, path), origin);
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

    private List<PowerType<?>> powerTypes = new LinkedList<>();
    private final ItemStack displayItem;
    private final Impact impact;
    private boolean isChoosable;
    private final int order;

    protected Origin(ItemConvertible item, Impact impact, int order) {
        this.displayItem = new ItemStack(item);
        this.impact = impact;
        this.isChoosable = true;
        this.order = order;
    }

    protected Origin add(PowerType<?>... powerTypes) {
        this.powerTypes.addAll(Lists.newArrayList(powerTypes));
        return this;
    }

    protected Origin setUnchoosable() {
        this.isChoosable = false;
        return this;
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
        Identifier id = ModRegistries.ORIGIN.getId(this);
        return new TranslatableText("origin." + id.getNamespace() + "." + id.getPath() + ".name");
    }

    public TranslatableText getDescription() {
        Identifier id = ModRegistries.ORIGIN.getId(this);
        return new TranslatableText("origin." + id.getNamespace() + "." + id.getPath() + ".description");
    }

    public int getOrder() {
        return this.order;
    }
}
