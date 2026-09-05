package com.abo47.kubejslab.client.ui.machines;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.jei.JeiBridgePlugin;
import com.abo47.kubejslab.client.ui.recipes.RecipeIndex;
import com.abo47.kubejslab.recipe.MachineRegistry;
import com.abo47.kubejslab.recipe.RecipeHandler;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;


public final class MachineCatalog {
    private static IJeiRuntime cachedRuntime;
    private static List<MachineView> machines = List.of();
    private static final Map<MachineView, Set<ResourceLocation>> RECIPE_IDS = new HashMap<>();
    private static long lastIndexVersion = -1;

    private MachineCatalog() {
    }

    public static List<MachineView> machines() {
        IJeiRuntime runtime = JeiBridgePlugin.runtime();
        if (runtime == null) {
            return List.of();
        }
        if (cachedRuntime != runtime) {
            cachedRuntime = runtime;
            RECIPE_IDS.clear();
            machines = build(runtime);
        }
        return machines;
    }

    public static Set<ResourceLocation> recipeIds(MachineView machine) {
        machines();
        long indexVersion = RecipeIndex.version();
        if (indexVersion != lastIndexVersion) {
            lastIndexVersion = indexVersion;
            RECIPE_IDS.clear();
        }
        Set<ResourceLocation> ids = RECIPE_IDS.get(machine);
        if (ids == null) {
            ids = computeRecipeIds(machine);
            if (!ids.isEmpty()) {
                RECIPE_IDS.put(machine, ids);
            }
        } else if (ids.isEmpty()) {
            Set<ResourceLocation> recomputed = computeRecipeIds(machine);
            if (!recomputed.isEmpty()) {
                RECIPE_IDS.put(machine, recomputed);
                return recomputed;
            }
        }
        return ids;
    }

    public static MachineView machineFor(ResourceLocation recipeId) {
        for (MachineView machine : machines()) {
            if (recipeIds(machine).contains(recipeId)) {
                return machine;
            }
        }
        return null;
    }

    private static List<MachineView> build(IJeiRuntime runtime) {
        IRecipeManager manager = runtime.getRecipeManager();
        List<MachineView> built = new ArrayList<>();
        Set<ResourceLocation> seen = new HashSet<>();
        for (IRecipeCategory<?> category : manager.createRecipeCategoryLookup().get().toList()) {
            try {
                ResourceLocation uid = category.getRecipeType().getUid();
                if ("minecraft".equals(uid.getNamespace()) && "fuel".equals(uid.getPath())) {
                    continue;
                }
                if (!seen.add(uid)) {
                    continue;
                }
                Optional<ItemStack> first = manager.createRecipeCatalystLookup(category.getRecipeType())
                        .getItemStack()
                        .findFirst();
                if (first.isEmpty()) {
                    continue;
                }
                ItemStack icon = first.get();
                String name = icon.getHoverName().getString();
                if (name.isBlank()) {
                    name = category.getTitle().getString();
                }
                boolean supported = MachineRegistry.supports(uid);
                built.add(new MachineView(category, icon, name, supported));
            } catch (RuntimeException | LinkageError ignored) {
            }
        }
        Map<String, List<MachineView>> byName = new HashMap<>();
        for (MachineView machine : built) {
            byName.computeIfAbsent(machine.name(), key -> new ArrayList<>()).add(machine);
        }
        List<MachineView> disambiguated = new ArrayList<>();
        for (List<MachineView> group : byName.values()) {
            if (group.size() == 1) {
                disambiguated.add(group.get(0));
                continue;
            }
            for (MachineView machine : group) {
                ResourceLocation uid = machine.recipeTypeUid();
                RecipeHandler support = MachineRegistry.get(uid);
                String label = support != null && support.displayLabel() != null
                        ? support.displayLabel()
                        : (support != null ? labelFor(support.jsonType()) : uid.getPath());
                disambiguated.add(new MachineView(machine.category(), machine.icon(),
                        machine.name() + " (" + label + ")", machine.supported()));
            }
        }
        built = disambiguated;
        built.sort(Comparator.comparing(MachineView::supported, Comparator.reverseOrder())
                .thenComparing(MachineView::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(machine -> machine.recipeTypeUid().toString()));
        return List.copyOf(built);
    }

    private static String labelFor(String jsonType) {
        int colon = jsonType.indexOf(':');
        return colon >= 0 ? jsonType.substring(colon + 1) : jsonType;
    }

    private static Set<ResourceLocation> computeRecipeIds(MachineView machine) {
        IJeiRuntime runtime = JeiBridgePlugin.runtime();
        if (runtime == null) {
            return Set.of();
        }
        IRecipeManager manager = runtime.getRecipeManager();
        RecipeHandler support = MachineRegistry.get(machine.recipeTypeUid());
        IRecipeCategory<?> category = machine.category();
        if (support != null && support.recipeIdSourceUid() != null) {
            ResourceLocation source = support.recipeIdSourceUid();
            try (Stream<IRecipeCategory<?>> stream = manager.createRecipeCategoryLookup().get()) {
                category = stream
                        .filter(c -> source.equals(c.getRecipeType().getUid()))
                        .findFirst()
                        .orElse(category);
            }
        }
        return computeIds(manager, category);
    }

    private static <R> Set<ResourceLocation> computeIds(IRecipeManager manager, IRecipeCategory<R> category) {
        Set<ResourceLocation> ids = new HashSet<>();
        try (Stream<R> stream = manager.createRecipeLookup(category.getRecipeType()).includeHidden().get()) {
            for (R recipe : (Iterable<R>) stream::iterator) {
                try {
                    ResourceLocation id = category.getRegistryName(recipe);
                    if (id != null) {
                        ids.add(id);
                    }
                } catch (RuntimeException | LinkageError ignored) {
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return Set.copyOf(ids);
    }
}