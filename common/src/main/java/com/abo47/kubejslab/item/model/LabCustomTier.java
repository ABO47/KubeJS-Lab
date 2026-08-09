package com.abo47.kubejslab.item.model;

import java.util.Arrays;


public record LabCustomTier(String id, boolean armor, int uses, float speed, float attackDamageBonus, int level,
        int enchantValue, String repairIngredient, float durabilityMultiplier, int[] protections, String equipSound,
        float toughness, float knockbackResistance) {

    public LabCustomTier {
        id = id == null ? "" : id;
        uses = Math.max(0, uses);
        speed = Math.max(0f, speed);
        attackDamageBonus = Math.max(0f, attackDamageBonus);
        level = Math.max(0, level);
        enchantValue = Math.max(0, enchantValue);
        repairIngredient = repairIngredient == null ? "" : repairIngredient;
        durabilityMultiplier = Math.max(0f, durabilityMultiplier);
        protections = protections == null ? new int[4] : protections;
        equipSound = equipSound == null ? "" : equipSound;
        toughness = Math.max(0f, toughness);
        knockbackResistance = Math.max(0f, knockbackResistance);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LabCustomTier that)) {
            return false;
        }
        return id.equals(that.id) && armor == that.armor && uses == that.uses
                && Float.compare(speed, that.speed) == 0 && Float.compare(attackDamageBonus, that.attackDamageBonus) == 0
                && level == that.level && enchantValue == that.enchantValue
                && repairIngredient.equals(that.repairIngredient)
                && Float.compare(durabilityMultiplier, that.durabilityMultiplier) == 0
                && Arrays.equals(protections, that.protections) && equipSound.equals(that.equipSound)
                && Float.compare(toughness, that.toughness) == 0
                && Float.compare(knockbackResistance, that.knockbackResistance) == 0;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (armor ? 1 : 0);
        result = 31 * result + uses;
        result = 31 * result + Float.floatToIntBits(speed);
        result = 31 * result + Float.floatToIntBits(attackDamageBonus);
        result = 31 * result + level;
        result = 31 * result + enchantValue;
        result = 31 * result + repairIngredient.hashCode();
        result = 31 * result + Float.floatToIntBits(durabilityMultiplier);
        result = 31 * result + Arrays.hashCode(protections);
        result = 31 * result + equipSound.hashCode();
        result = 31 * result + Float.floatToIntBits(toughness);
        result = 31 * result + Float.floatToIntBits(knockbackResistance);
        return result;
    }
}