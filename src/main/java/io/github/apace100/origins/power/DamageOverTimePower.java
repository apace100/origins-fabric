package io.github.apace100.origins.power;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Difficulty;

import java.util.Map;

public class DamageOverTimePower extends VariableIntPower {

    private final int damageTickInterval;
    private final int beginDamageIn;
    private final float damageAmountEasy;
    private final float damageAmount;
    private final DamageSource damageSource;
    private final Enchantment protectingEnchantment;
    private final float protectionEffectiveness;

    private int outOfDamageTicks;

    public DamageOverTimePower(PowerType<?> type, PlayerEntity player, int beginDamageIn, int damageInterval, float damageAmountEasy, float damageAmount, DamageSource damageSource, Enchantment protectingEnchantment, float protectionEffectiveness) {
        super(type, player, beginDamageIn, 0, Math.max(damageInterval, beginDamageIn));
        this.damageSource = damageSource;
        this.beginDamageIn = beginDamageIn;
        this.damageAmount = damageAmount;
        this.damageAmountEasy = damageAmountEasy;
        this.protectingEnchantment = protectingEnchantment;
        this.protectionEffectiveness = protectionEffectiveness;
        this.damageTickInterval = damageInterval;
        this.setTicking(true);
    }

    @Override
    public int getMax() {
        return Math.max(super.getMax(), getDamageBegin());
    }

    public int getDamageBegin() {
        int prot = getProtection();
        if(prot >= 64) {
            return 20 * 60 * 20;
        }
        prot = (int)(prot * 2 * 20 * protectionEffectiveness);
        return beginDamageIn + prot;
    }

    @Override
    public void tick() {
        if(this.isActive()) {
            doDamage();
        } else {
            resetDamage();
        }
    }

    public void doDamage() {
        outOfDamageTicks = 0;
        if(getValue() <= 0) {
            setValue(damageTickInterval);
            player.damage(damageSource, player.world.getDifficulty() == Difficulty.EASY ? damageAmountEasy : damageAmount);
        } else {
            decrement();
        }
    }

    public void resetDamage() {
        if(outOfDamageTicks >= 20) {
            this.setValue(getDamageBegin());
        } else {
            outOfDamageTicks++;
        }
    }

    private int getProtection() {
        if(protectingEnchantment == null) {
            return 0;
        } else {
            Map<EquipmentSlot, ItemStack> enchantedItems = protectingEnchantment.getEquipment(player);
            Iterable<ItemStack> iterable = enchantedItems.values();
            int i = 0;
            for (ItemStack itemStack : iterable) {
                i += EnchantmentHelper.getLevel(protectingEnchantment, itemStack);
            }
            return i * enchantedItems.size();
        }
    }
}
