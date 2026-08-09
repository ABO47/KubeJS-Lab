package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabRecipeStateEntry;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;


public final class LabRecipeStates {
    private static final Map<ResourceLocation, LabRecipeStateEntry> STATES = new LinkedHashMap<>();

    private LabRecipeStates() {
    }

    public static void apply(Map<ResourceLocation, LabRecipeStateEntry> states) {
        STATES.clear();
        STATES.putAll(states);
    }

    public static LabRecipeStatus statusOf(ResourceLocation id) {
        LabRecipeStateEntry entry = STATES.get(id);
        return entry == null ? LabRecipeStatus.NORMAL : entry.status();
    }

    public static ResourceLocation machineUidOf(ResourceLocation id) {
        LabRecipeStateEntry entry = STATES.get(id);
        return entry == null ? null : entry.machineUid();
    }

    public static List<LabRecipeIndex.LabRecipeEntry> disabledEntries(ResourceLocation machineUid) {
        List<LabRecipeIndex.LabRecipeEntry> result = new ArrayList<>();
        for (LabRecipeStateEntry entry : STATES.values()) {
            if (entry.status() == LabRecipeStatus.DISABLED
                    && machineUid != null && machineUid.equals(entry.machineUid())) {
                result.add(LabRecipeIndex.LabRecipeEntry.of(entry.id(), entry.output(), entry.name()));
            }
        }
        return result;
    }
}
