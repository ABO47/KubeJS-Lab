package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;
import com.abo47.kubejslab.platform.Services;
import com.abo47.kubejslab.recipe.model.LabRecipeStateEntry;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;


public final class LabRecipeIndex {
    private static final Map<ResourceLocation, Recipe<?>> RECIPE_CACHE = new HashMap<>();
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
        return LabSearchNormalizer.normalizeUserSearch(value);
    }

    public record RecipeCounts(int recipes, int disabled, int modified) {
    }

    public static RecipeCounts counts(boolean kubejs) {
        int recipes = 0;
        for (LabRecipeEntry entry : entries()) {
            if (entry.kubejs() == kubejs) {
                recipes++;
            }
        }
        int disabled = 0;
        int modified = 0;
        for (LabRecipeStateEntry state : LabRecipeStates.stateEntries()) {
            if ("kubejs".equals(state.id().getNamespace()) != kubejs) {
                continue;
            }
            LabRecipeStatus status = state.status();
            if (status == LabRecipeStatus.DISABLED) {
                disabled++;
            } else if (status == LabRecipeStatus.MODIFIED) {
                modified++;
            }
        }
        return new RecipeCounts(recipes, disabled, modified);
    }

    public static Recipe<?> recipeById(ResourceLocation id) {
        entries();
        if (cachedManager != null) {
            Recipe<?> recipe = cachedManager.byKey(id).orElse(null);
            if (recipe != null) {
                return recipe;
            }
        }
        return RECIPE_CACHE.get(id);
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
            try {
                entries = build(manager, connection.registryAccess());
                version++;
            } catch (RuntimeException e) {
                cachedManager = null;
                cachedCount = -1;
                KubeJSLab.LOGGER.warn("[Index] failed to build recipe index, will retry", e);
            }
        }
        return entries;
    }

    private static List<LabRecipeEntry> build(RecipeManager manager, RegistryAccess registryAccess) {
        List<LabRecipeEntry> built = new ArrayList<>();
        for (Recipe<?> recipe : manager.getRecipes()) {
            RECIPE_CACHE.put(recipe.getId(), recipe);
            ItemStack result = resultItem(recipe, registryAccess);
            FluidStack fluidOutput = Services.platform().fluidOutputStack(recipe);
            if (result.isEmpty() && fluidOutput.isEmpty()) {
                continue;
            }
            if (result.isEmpty()) {
                result = Services.platform().fluidOutputDisplay(recipe);
            }
            built.add(LabRecipeEntry.of(recipe.getId(), result,
                    fluidNameOrFallback(recipe, result, fluidOutput), fluidOutput));
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

    private static String fluidNameOrFallback(Recipe<?> recipe, ItemStack result, FluidStack fluidOutput) {
        if (fluidOutput != null && !fluidOutput.isEmpty()) {
            return fluidOutput.getDisplayName().getString();
        }
        if (!result.isEmpty()) {
            return result.getHoverName().getString();
        }
        String name = Services.platform().fluidOutputDisplayName(recipe);
        if (!name.isBlank()) {
            return name;
        }
        return recipe.getId().getPath();
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
            String normalizedName,
            FluidStack fluidOutput
    ) {
        private static final String KUBEJS_NAMESPACE = "kubejs";

        static LabRecipeEntry of(ResourceLocation id, ItemStack output, String name) {
            return of(id, output, name, null);
        }

        static LabRecipeEntry of(ResourceLocation id, ItemStack output, String name, FluidStack fluidOutput) {
            ItemStack copy = output.copy();
            copy.setCount(1);
            return new LabRecipeEntry(
                    id,
                    copy,
                    name,
                    KUBEJS_NAMESPACE.equals(id.getNamespace()),
                    normalize(id.toString()),
                    normalize(name),
                    fluidOutput
            );
        }

        public boolean matches(String query) {
            return normalizedId.contains(query) || normalizedName.contains(query);
        }
    }
}
