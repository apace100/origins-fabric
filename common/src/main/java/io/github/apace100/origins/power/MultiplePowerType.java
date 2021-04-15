package io.github.apace100.origins.power;

import com.google.common.collect.ImmutableList;
import io.github.apace100.origins.power.factory.PowerFactory;
import net.minecraft.util.Identifier;

import java.util.List;

public class MultiplePowerType<T extends Power> extends PowerType<T> {

    private ImmutableList<Identifier> subPowers;

    public MultiplePowerType(Identifier id, PowerFactory<T>.Instance factory) {
        super(id, factory);
    }

    public void setSubPowers(List<Identifier> subPowers) {
        this.subPowers = ImmutableList.copyOf(subPowers);
    }

    public ImmutableList<Identifier> getSubPowers() {
        return subPowers;
    }
}
