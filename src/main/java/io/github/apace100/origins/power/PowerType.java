package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.BiFunction;

public class PowerType<T extends Power> {

    private BiFunction<PowerType, PlayerEntity, T> factory;
    private boolean isHidden = false;

    protected PowerType(BiFunction<PowerType, PlayerEntity, T> factory) {
        this.factory = factory;
    }

    public PowerType setHidden() {
        this.isHidden = true;
        return this;
    }

    public T create(PlayerEntity player) {
        return factory.apply(this, player);
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    public boolean isActive(Entity entity) {
        if(entity instanceof PlayerEntity) {
            OriginComponent component = ModComponents.ORIGIN.get(entity);
            return component.hasPower(this);
        }
        return false;
    }

    public T get(Entity entity) {
        if(entity instanceof PlayerEntity) {
            OriginComponent component = ModComponents.ORIGIN.get(entity);
            return component.getPower(this);
        }
        return null;
    }

    public TranslatableText getName() {
        Identifier id = ModRegistries.POWER_TYPE.getId(this);
        return new TranslatableText("power." + id.getNamespace() + "." + id.getPath() + ".name");
    }

    public TranslatableText getDescription() {
        Identifier id = ModRegistries.POWER_TYPE.getId(this);
        return new TranslatableText("power." + id.getNamespace() + "." + id.getPath() + ".description");
    }
}
