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

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.platform.Services;
import com.abo47.kubejslab.recipe.model.RecipeStateEntry;
import com.abo47.kubejslab.recipe.model.RecipeStatus;


public final class RecipeIndex {
    private static final Map<ResourceLocation, Recipe<?>> RECIPE_CACHE = new HashMap<>();
    private static RecipeManager cachedManager;
    private static int cachedCount = -1;
    private static List<RecipeEntry> entries = List.of();
    private static long version;

    private RecipeIndex() {
    }

    public static long version() {
        entries();
        return version;
    }

    public static List<RecipeEntry> search(String query, boolean kubejsOnly, Set<ResourceLocation> machineRecipeIds) {
        List<RecipeEntry> source = entries();
        String normalizedQuery = normalize(query);
        List<RecipeEntry> matches = new ArrayList<>();
        for (RecipeEntry entry : source) {
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
        return SearchNormalizer.normalizeUserSearch(value);
    }

    public record RecipeCounts(int recipes, int disabled, int modified) {
    }

    public static RecipeCounts counts(boolean kubejs) {
        int recipes = 0;
        for (RecipeEntry entry : entries()) {
            if (entry.kubejs() == kubejs) {
                recipes++;
            }
        }
        int disabled = 0;
        int modified = 0;
        for (RecipeStateEntry state : RecipeStates.stateEntries()) {
            if ("kubejs".equals(state.id().getNamespace()) != kubejs) {
                continue;
            }
            RecipeStatus status = state.status();
            if (status == RecipeStatus.DISABLED) {
                disabled++;
            } else if (status == RecipeStatus.MODIFIED) {
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

    private static List<RecipeEntry> entries() {
        RecipeManager manager = resolveManager();
        if (manager == null) {
            KubeJSLab.LOGGER.warn("[RecipeIndex] no recipe manager available (connection={})",
                    Minecraft.getInstance().getConnection() == null ? "null" : "present");
            cachedManager = null;
            cachedCount = -1;
            entries = List.of();
            return entries;
        }
        int count = manager.getRecipes().size();
        if (manager != cachedManager || count != cachedCount || (entries.isEmpty() && count > 0)) {
            int prevSize = entries.size();
            cachedManager = manager;
            cachedCount = count;
            KubeJSLab.LOGGER.info("[RecipeIndex] rebuilding: managerRecipes={} prevEntries={} cachedCount={}", count, prevSize, cachedCount);
            try {
                entries = build(manager, safeRegistryAccess());
                version++;
                KubeJSLab.LOGGER.info("[RecipeIndex] built {} entries (filtered from {} manager recipes) version={}", entries.size(), count, version);
            } catch (Throwable e) {
                KubeJSLab.LOGGER.warn("[RecipeIndex] failed to build recipe index, will retry", e);
                entries = List.of();
                cachedCount = -1;
            }
        }
        return entries;
    }

    private static RecipeManager resolveManager() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection != null) {
            RecipeManager manager = connection.getRecipeManager();
            if (manager != null && !manager.getRecipes().isEmpty()) {
                return manager;
            }
        }
        if (minecraft.getSingleplayerServer() != null) {
            RecipeManager serverManager = minecraft.getSingleplayerServer().getRecipeManager();
            if (serverManager != null && !serverManager.getRecipes().isEmpty()) {
                return serverManager;
            }
        }
        return connection == null ? null : connection.getRecipeManager();
    }

    private static RegistryAccess safeRegistryAccess() {
        try {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            return connection == null ? null : connection.registryAccess();
        } catch (Throwable e) {
            KubeJSLab.LOGGER.warn("[RecipeIndex] registryAccess unavailable: {}", e.getMessage());
            return null;
        }
    }

    private static List<RecipeEntry> build(RecipeManager manager, RegistryAccess registryAccess) {
        List<RecipeEntry> built = new ArrayList<>();
        for (Recipe<?> recipe : manager.getRecipes()) {
            try {
                RECIPE_CACHE.put(recipe.getId(), recipe);
                ItemStack result = resultItem(recipe, registryAccess);
                FluidStack fluidOutput = Services.platform().fluidOutputStack(recipe);
                if (result.isEmpty() && fluidOutput.isEmpty()) {
                    continue;
                }
                if (result.isEmpty()) {
                    result = Services.platform().fluidOutputDisplay(recipe);
                }
                built.add(RecipeEntry.of(recipe.getId(), result,
                        fluidNameOrFallback(recipe, result, fluidOutput), fluidOutput));
            } catch (Throwable e) {
                KubeJSLab.LOGGER.warn("[RecipeIndex] skipped recipe {} while building index: {}",
                        recipe.getId(), e.getMessage());
            }
        }
        built.sort(Comparator.comparing(RecipeEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RecipeEntry::id));
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

    public record RecipeEntry(
            ResourceLocation id,
            ItemStack output,
            String name,
            boolean kubejs,
            String normalizedId,
            String normalizedName,
            FluidStack fluidOutput
    ) {
        private static final String KUBEJS_NAMESPACE = "kubejs";

        static RecipeEntry of(ResourceLocation id, ItemStack output, String name) {
            return of(id, output, name, null);
        }

        static RecipeEntry of(ResourceLocation id, ItemStack output, String name, FluidStack fluidOutput) {
            ItemStack copy = output.copy();
            copy.setCount(1);
            return new RecipeEntry(
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
