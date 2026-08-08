package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;


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