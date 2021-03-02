package io.github.apace100.origins.power;

import io.github.apace100.origins.power.factory.PowerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class PowerTypeReference<T extends Power> extends PowerType<T> {

    private PowerType<T> referencedPowerType;
    private int cooldown = 0;

    public PowerTypeReference(Identifier id) {
        super(id, null);
    }

    @Override
    public PowerFactory<T>.Instance getFactory() {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return null;
        }
        return referencedPowerType.getFactory();
    }

    @Override
    public boolean isActive(Entity entity) {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return false;
        }
        return referencedPowerType.isActive(entity);
    }

    @Override
    public T get(Entity entity) {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return null;
        }
        return referencedPowerType.get(entity);
    }

    public PowerType<T> getReferencedPowerType() {
        if(isReferenceInvalid()) {
            if(cooldown > 0) {
                cooldown--;
                return null;
            }
            try {
                referencedPowerType = null;
                referencedPowerType = PowerTypeRegistry.get(getIdentifier());
            } catch(IllegalArgumentException e) {
                cooldown = 600;
                //Origins.LOGGER.warn("Invalid PowerTypeReference: no power type exists with id \"" + getIdentifier() + "\"");
            }
        }
        return referencedPowerType;
    }

    private boolean isReferenceInvalid() {
        if(referencedPowerType != null) {
            if(PowerTypeRegistry.contains(referencedPowerType.getIdentifier())) {
                PowerType<T> type = PowerTypeRegistry.get(referencedPowerType.getIdentifier());
                return type != referencedPowerType;
            }
        }
        return true;
    }
}
