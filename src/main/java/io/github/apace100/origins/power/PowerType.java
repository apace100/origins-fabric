package io.github.apace100.origins.power;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class PowerType<T extends Power> {

    private Identifier identifier;
    private PowerFactory<T>.Instance factory;
    private boolean isHidden = false;

    private String nameTranslationKey;
    private String descriptionTranslationKey;

    public PowerType(Identifier id, PowerFactory<T>.Instance factory) {
        this.identifier = id;
        this.factory = factory;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public PowerFactory<T>.Instance getFactory() {
        return factory;
    }

    public PowerType setHidden() {
        this.isHidden = true;
        return this;
    }

    public void setTranslationKeys(String name, String description) {
        this.nameTranslationKey = name;
        this.descriptionTranslationKey = description;
    }

    public T create(PlayerEntity player) {
        return factory.apply(this, player);
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    public boolean isActive(Entity entity) {
        if(entity instanceof PlayerEntity && identifier != null) {
            OriginComponent component = ModComponents.ORIGIN.get(entity);
            if(component.hasPower(this)) {
                return component.getPower(this).isActive();
            }
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

    public String getOrCreateNameTranslationKey() {
        if(nameTranslationKey == null || nameTranslationKey.isEmpty()) {
            nameTranslationKey =
                "power." + identifier.getNamespace() + "." + identifier.getPath() + ".name";
        }
        return nameTranslationKey;
    }

    public TranslatableText getName() {
        return new TranslatableText(getOrCreateNameTranslationKey());
    }

    public String getOrCreateDescriptionTranslationKey() {
        if(descriptionTranslationKey == null || descriptionTranslationKey.isEmpty()) {
            descriptionTranslationKey =
                "power." + identifier.getNamespace() + "." + identifier.getPath() + ".description";
        }
        return descriptionTranslationKey;
    }

    public TranslatableText getDescription() {
        return new TranslatableText(getOrCreateDescriptionTranslationKey());
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof PowerType)) {
            return false;
        }
        Identifier id = ((PowerType)obj).getIdentifier();
        return identifier.equals(id);
    }
}
