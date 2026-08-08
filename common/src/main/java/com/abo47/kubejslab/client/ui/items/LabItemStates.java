package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;
import com.abo47.kubejslab.item.model.LabCustomTier;
import com.abo47.kubejslab.item.model.LabItemState;
import com.abo47.kubejslab.item.model.LabItemStatus;


public final class LabItemStates {
    private static final Map<ResourceLocation, LabItemState> STATE = new HashMap<>();

    private LabItemStates() {
    }

    public static void apply(Map<ResourceLocation, LabItemState> states) {
        STATE.clear();
        STATE.putAll(states);
    }

    public static LabItemStatus statusOf(ResourceLocation id) {
        LabItemState entry = STATE.get(id);
        return entry == null ? LabItemStatus.NORMAL : entry.status();
    }

    public static LabItemState stateOf(ResourceLocation id) {
        return STATE.get(id);
    }

    public static boolean pendingRestartOf(ResourceLocation id) {
        LabItemState entry = STATE.get(id);
        return entry != null && entry.pendingRestart();
    }

    public static String nameOf(ResourceLocation id) {
        LabItemState entry = STATE.get(id);
        return entry == null ? "" : entry.name();
    }

    public static List<String> customTierIds(boolean armor) {
        List<String> ids = new ArrayList<>();
        for (LabItemState entry : STATE.values()) {
            LabCustomTier tier = entry.customTier();
            if (tier != null && tier.armor() == armor) {
                ids.add(tier.id());
            }
        }
        return ids;
    }

    public static List<LabItemIndex.LabItemEntry> stateEntries() {
        List<LabItemIndex.LabItemEntry> result = new ArrayList<>();
        for (LabItemState entry : STATE.values()) {
            if (!entry.id().getNamespace().equals("kubejs")) {
                continue;
            }
            result.add(new LabItemIndex.LabItemEntry(entry.id(), ItemStack.EMPTY,
                    entry.name().isBlank() ? entry.id().getPath() : entry.name(), true,
                    LabSearchNormalizer.normalizeUserSearch(entry.id().toString()),
                    LabSearchNormalizer.normalizeUserSearch(entry.name().isBlank() ? entry.id().getPath() : entry.name())));
        }
        return result;
    }
}