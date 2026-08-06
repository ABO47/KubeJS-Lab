package com.abo47.kubejslab.client.ui.machines;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;

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

import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;

import com.abo47.kubejslab.client.jei.LabJeiPlugin;
import com.abo47.kubejslab.recipe.LabRecipeMachines;

public final class LabMachineCatalog {
    private static IJeiRuntime cachedRuntime;
    private static List<LabMachine> machines = List.of();
    private static final Map<LabMachine, Set<ResourceLocation>> RECIPE_IDS = new HashMap<>();
    private static long lastIndexVersion = -1;

    private LabMachineCatalog() {
    }

    public static List<LabMachine> machines() {
        IJeiRuntime runtime = LabJeiPlugin.runtime();
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

    public static Set<ResourceLocation> recipeIds(LabMachine machine) {
        machines();
        long indexVersion = LabRecipeIndex.version();
        if (indexVersion != lastIndexVersion) {
            lastIndexVersion = indexVersion;
            RECIPE_IDS.clear();
        }
        Set<ResourceLocation> ids = RECIPE_IDS.get(machine);
        if (ids == null) {
            ids = computeRecipeIds(machine);
            RECIPE_IDS.put(machine, ids);
        }
        return ids;
    }

    public static LabMachine machineFor(ResourceLocation recipeId) {
        for (LabMachine machine : machines()) {
            if (recipeIds(machine).contains(recipeId)) {
                return machine;
            }
        }
        return null;
    }

    private static List<LabMachine> build(IJeiRuntime runtime) {
        IRecipeManager manager = runtime.getRecipeManager();
        List<LabMachine> built = new ArrayList<>();
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
                boolean supported = LabRecipeMachines.supports(uid);
                built.add(new LabMachine(category, icon, name, supported));
            } catch (RuntimeException | LinkageError ignored) {
            }
        }
        built.sort(Comparator.comparing(LabMachine::supported, Comparator.reverseOrder())
                .thenComparing(LabMachine::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(machine -> machine.recipeTypeUid().toString()));
        return List.copyOf(built);
    }

    private static Set<ResourceLocation> computeRecipeIds(LabMachine machine) {
        IJeiRuntime runtime = LabJeiPlugin.runtime();
        if (runtime == null) {
            return Set.of();
        }
        return computeIds(runtime.getRecipeManager(), machine.category());
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