package com.abo47.kubejslab.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

public final class LabRecipeIndex {
    private static RecipeManager cachedManager;
    private static int cachedCount = -1;
    private static List<LabRecipeEntry> entries = List.of();
    private static long version;

    private LabRecipeIndex() {
    }

    public static long version() {
        entries();
        return version;
    }

    public static List<LabRecipeEntry> search(String query, boolean kubejsOnly, Set<ResourceLocation> machineRecipeIds) {
        List<LabRecipeEntry> source = entries();
        String normalizedQuery = normalize(query);
        List<LabRecipeEntry> matches = new ArrayList<>();
        for (LabRecipeEntry entry : source) {
            if (entry.kubejs() != kubejsOnly) {
                continue;
            }
            if (machineRecipeIds != null && !machineRecipeIds.contains(entry.id())) {
                continue;
            }
            if (normalizedQuery.isBlank() || entry.matches(normalizedQuery)) {
                matches.add(entry);
            }
        }
        return matches;
    }

    public static String normalizeUserSearch(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\n', ' ').replace('\r', ' ').toLowerCase(Locale.ROOT);
        while (normalized.endsWith("_")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static List<LabRecipeEntry> entries() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        RecipeManager manager = connection == null ? null : connection.getRecipeManager();
        if (manager == null) {
            cachedManager = null;
            cachedCount = -1;
            entries = List.of();
            return entries;
        }
        if (manager != cachedManager || manager.getRecipes().size() != cachedCount) {
            cachedManager = manager;
            cachedCount = manager.getRecipes().size();
            entries = build(manager, connection.registryAccess());
            version++;
        }
        return entries;
    }

    private static List<LabRecipeEntry> build(RecipeManager manager, RegistryAccess registryAccess) {
        List<LabRecipeEntry> built = new ArrayList<>();
        for (Recipe<?> recipe : manager.getRecipes()) {
            ItemStack result = resultItem(recipe, registryAccess);
            if (result.isEmpty()) {
                continue;
            }
            built.add(LabRecipeEntry.of(recipe.getId(), result, displayName(result, recipe.getId())));
        }
        built.sort(Comparator.comparing(LabRecipeEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabRecipeEntry::id));
        return List.copyOf(built);
    }

    private static ItemStack resultItem(Recipe<?> recipe, RegistryAccess registryAccess) {
        try {
            return recipe.getResultItem(registryAccess);
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static String displayName(ItemStack stack, ResourceLocation id) {
        String name = stack.getHoverName().getString();
        if (name.isBlank()) {
            name = id.getPath();
        }
        return name;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record LabRecipeEntry(
            ResourceLocation id,
            ItemStack output,
            String name,
            boolean kubejs,
            String normalizedId,
            String normalizedName
    ) {
        private static final String KUBEJS_NAMESPACE = "kubejs";

        static LabRecipeEntry of(ResourceLocation id, ItemStack output, String name) {
            ItemStack copy = output.copy();
            copy.setCount(1);
            return new LabRecipeEntry(
                    id,
                    copy,
                    name,
                    KUBEJS_NAMESPACE.equals(id.getNamespace()),
                    normalize(id.toString()),
                    normalize(name)
            );
        }

        public boolean matches(String query) {
            return normalizedId.contains(query) || normalizedName.contains(query);
        }
    }
}
