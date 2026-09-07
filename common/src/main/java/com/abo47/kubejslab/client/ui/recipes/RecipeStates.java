package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.RecipeStateEntry;
import com.abo47.kubejslab.recipe.model.RecipeStatus;


public final class RecipeStates {
    private static final Map<ResourceLocation, RecipeStateEntry> STATES = new LinkedHashMap<>();

    private RecipeStates() {
    }

    public static void apply(Map<ResourceLocation, RecipeStateEntry> states) {
        STATES.clear();
        STATES.putAll(states);
    }

    public static RecipeStatus statusOf(ResourceLocation id) {
        RecipeStateEntry entry = STATES.get(id);
        return entry == null ? RecipeStatus.NORMAL : entry.status();
    }

    public static java.util.Collection<RecipeStateEntry> stateEntries() {
        return STATES.values();
    }

    public static ResourceLocation machineUidOf(ResourceLocation id) {
        RecipeStateEntry entry = STATES.get(id);
        return entry == null ? null : entry.machineUid();
    }

    public static List<RecipeIndex.RecipeEntry> disabledEntries(ResourceLocation machineUid) {
        List<RecipeIndex.RecipeEntry> result = new ArrayList<>();
        for (RecipeStateEntry entry : STATES.values()) {
            if (entry.status() == RecipeStatus.DISABLED
                    && machineUid != null && machineUid.equals(entry.machineUid())) {
                result.add(RecipeIndex.RecipeEntry.of(entry.id(), entry.output(), entry.name()));
            }
        }
        return result;
    }

    public static List<RecipeIndex.RecipeEntry> createdEntries(ResourceLocation machineUid) {
        List<RecipeIndex.RecipeEntry> result = new ArrayList<>();
        for (RecipeStateEntry entry : STATES.values()) {
            if (entry.status() != RecipeStatus.CREATED) {
                continue;
            }
            if (machineUid != null && entry.machineUid() != null && !machineUid.equals(entry.machineUid())) {
                continue;
            }
            result.add(RecipeIndex.RecipeEntry.of(entry.id(), entry.output(), entry.name()));
        }
        return result;
    }

    public static RecipeIndex.RecipeEntry createdEntryFor(String outputItemId, ResourceLocation machineUid,
            String name) {
        if (outputItemId == null || outputItemId.isBlank()) {
            return null;
        }
        for (RecipeStateEntry entry : STATES.values()) {
            if (entry.status() != RecipeStatus.CREATED) {
                continue;
            }
            if (machineUid != null && entry.machineUid() != null && !machineUid.equals(entry.machineUid())) {
                continue;
            }
            String entryItem = entry.output().isEmpty() ? ""
                    : BuiltInRegistries.ITEM.getKey(entry.output().getItem()).toString();
            if (!outputItemId.equals(entryItem)) {
                continue;
            }
            if (name != null && !name.isBlank() && !name.equals(entry.name())) {
                continue;
            }
            return RecipeIndex.RecipeEntry.of(entry.id(), entry.output(), entry.name());
        }
        return null;
    }
}
