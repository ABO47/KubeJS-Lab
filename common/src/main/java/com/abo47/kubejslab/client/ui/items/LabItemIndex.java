package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;

import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;
import com.abo47.kubejslab.item.model.LabItemFieldValues;

import com.mojang.datafixers.util.Pair;


public final class LabItemIndex {
    private static final Map<ResourceLocation, LabItemEntry> ENTRIES = new HashMap<>();

    static {
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if (id.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null) {
                continue;
            }
            ENTRIES.put(id, LabItemEntry.of(id, item));
        }
    }

    private LabItemIndex() {
    }

    public static List<LabItemEntry> entries() {
        List<LabItemEntry> sorted = new ArrayList<>(ENTRIES.values());
        sorted.sort(Comparator.comparing(LabItemEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabItemEntry::id));
        return sorted;
    }

    public static List<LabItemEntry> search(String query, boolean kubejsOnly) {
        String normalizedQuery = LabSearchNormalizer.normalizeQuery(query);
        List<LabItemEntry> matches = new ArrayList<>();
        for (LabItemEntry entry : ENTRIES.values()) {
            if (entry.kubejs() != kubejsOnly) {
                continue;
            }
            if (normalizedQuery.isBlank() || entry.matches(normalizedQuery)) {
                matches.add(entry);
            }
        }
        matches.sort(Comparator.comparing(LabItemEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabItemEntry::id));
        return matches;
    }

    public static LabItemEntry entryById(ResourceLocation id) {
        return ENTRIES.get(id);
    }

    public static String typeOf(ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null) {
            return "basic";
        }
        if (item instanceof SwordItem) {
            return "sword";
        }
        if (item instanceof PickaxeItem) {
            return "pickaxe";
        }
        if (item instanceof AxeItem) {
            return "axe";
        }
        if (item instanceof ShovelItem) {
            return "shovel";
        }
        if (item instanceof HoeItem) {
            return "hoe";
        }
        if (item instanceof ShearsItem) {
            return "shears";
        }
        if (item instanceof ArmorItem armorItem) {
            return switch (armorItem.getType()) {
                case HELMET -> "helmet";
                case CHESTPLATE -> "chestplate";
                case LEGGINGS -> "leggings";
                case BOOTS -> "boots";
            };
        }
        if (item instanceof RecordItem) {
            return "music_disc";
        }
        return "basic";
    }

    public static LabItemFieldValues prefillValues(ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null) {
            return LabItemFieldValues.defaults();
        }
        ItemStack stack = new ItemStack(item);
        FoodProperties food = item.getFoodProperties();
        String foodEffect = "";
        int foodEffectDuration = 0;
        int foodEffectAmplifier = 0;
        float foodEffectChance = 0f;
        if (food != null && !food.getEffects().isEmpty()) {
            Pair<MobEffectInstance, Float> pair = food.getEffects().get(0);
            MobEffectInstance effect = pair.getFirst();
            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect());
            foodEffect = effectId == null ? "" : effectId.toString();
            foodEffectDuration = effect.getDuration();
            foodEffectAmplifier = effect.getAmplifier();
            foodEffectChance = pair.getSecond();
        }
        String toolTier = "";
        String armorTier = "";
        int armorProtection = 0;
        float armorToughness = 0f;
        float armorKnockback = 0f;
        if (item instanceof TieredItem tiered && tiered.getTier() instanceof Tiers tier) {
            toolTier = tier.name().toLowerCase(Locale.ROOT);
        }
        if (item instanceof ArmorItem armorItem) {
            String material = armorItem.getMaterial().getName();
            armorTier = material.startsWith("minecraft:") ? material.substring("minecraft:".length()) : material;
            EquipmentSlot slot = armorItem.getType().getSlot();
            armorProtection = (int) modifierAmount(stack, slot, Attributes.ARMOR);
            armorToughness = modifierAmount(stack, slot, Attributes.ARMOR_TOUGHNESS);
            armorKnockback = modifierAmount(stack, slot, Attributes.KNOCKBACK_RESISTANCE);
        }
        Item remainder = item.hasCraftingRemainingItem() ? item.getCraftingRemainingItem() : null;
        return new LabItemFieldValues("", "", item.getRarity(stack).name().toLowerCase(Locale.ROOT),
                item.getMaxStackSize(), item.getMaxDamage(), 0, false, item.isFireResistant(),
                remainder == null ? "" : BuiltInRegistries.ITEM.getKey(remainder).toString(), "", "",
                food == null ? 0 : food.getNutrition(),
                food == null ? 0f : food.getSaturationModifier(),
                food != null && food.isMeat(), food != null && food.isFastFood(), food != null && food.canAlwaysEat(),
                foodEffect, foodEffectDuration, foodEffectAmplifier, foodEffectChance, toolTier,
                modifierAmount(stack, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE),
                modifierAmount(stack, EquipmentSlot.MAINHAND, Attributes.ATTACK_SPEED), 0f, armorTier,
                armorProtection, armorToughness, armorKnockback, 0, 0f, 0f, 0, 0, "", 0f, "", "", 0f, 0f, "",
                "", 0f, "", "", 0);
    }

    private static float modifierAmount(ItemStack stack, EquipmentSlot slot, Attribute attribute) {
        double total = 0;
        for (AttributeModifier modifier : stack.getAttributeModifiers(slot).get(attribute)) {
            total += modifier.getAmount();
        }
        return (float) total;
    }

    public record LabItemEntry(ResourceLocation id, ItemStack stack, String name, boolean kubejs,
            String normalizedId, String normalizedName) {
        public static LabItemEntry of(ResourceLocation id, Item item) {
            String name = item.getDescription().getString();
            return new LabItemEntry(id, new ItemStack(item), name, id.getNamespace().equals("kubejs"),
                    LabSearchNormalizer.normalizeUserSearch(id.toString()),
                    LabSearchNormalizer.normalizeUserSearch(name));
        }

        public boolean matches(String normalizedQuery) {
            return normalizedId.contains(normalizedQuery) || normalizedName.contains(normalizedQuery);
        }
    }
}