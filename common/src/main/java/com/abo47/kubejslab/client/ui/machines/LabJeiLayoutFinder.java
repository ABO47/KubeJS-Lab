package com.abo47.kubejslab.client.ui.machines;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.client.jei.LabJeiPlugin;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.LabRecipeMachines;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;


final class LabJeiLayoutFinder {
    static IRecipeLayoutDrawable findSampleLayout(LabMachine machine) {
        if (LabJeiPlugin.runtime() == null) {
            return null;
        }
        return findSample(LabJeiPlugin.runtime(), resolveLayoutCategory(machine));
    }

    static IRecipeLayoutDrawable findJeiLayout(LabMachine machine, LabRecipeIndex.LabRecipeEntry entry) {
        if (LabJeiPlugin.runtime() == null || machine == null) {
            return null;
        }
        return findEntry(LabJeiPlugin.runtime(), resolveLayoutCategory(machine), entry.id());
    }

    private static IRecipeCategory<?> resolveLayoutCategory(LabMachine machine) {
        LabRecipeMachine support = machine == null ? null : LabRecipeMachines.get(machine.recipeTypeUid());
        if (support == null || support.recipeIdSourceUid() == null) {
            return machine.category();
        }
        IRecipeManager manager = LabJeiPlugin.runtime().getRecipeManager();
        ResourceLocation source = support.recipeIdSourceUid();
        try (Stream<IRecipeCategory<?>> stream = manager.createRecipeCategoryLookup().get()) {
            return stream
                    .filter(c -> source.equals(c.getRecipeType().getUid()))
                    .findFirst()
                    .orElse(machine.category());
        } catch (RuntimeException | LinkageError ignored) {
            return machine.category();
        }
    }

    private static <R> IRecipeLayoutDrawable<?> findSample(IJeiRuntime runtime, IRecipeCategory<R> category) {
        IRecipeManager manager = runtime.getRecipeManager();
        try (Stream<R> stream = manager.createRecipeLookup(category.getRecipeType()).includeHidden().get()) {
            return stream
                    .findFirst()
                    .flatMap(candidate -> manager.createRecipeLayoutDrawable(category, candidate, emptyFocus(runtime)))
                    .orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static <R> IRecipeLayoutDrawable<?> findEntry(
            IJeiRuntime runtime, IRecipeCategory<R> category, ResourceLocation entryId) {
        IRecipeManager manager = runtime.getRecipeManager();
        try (Stream<R> stream = manager.createRecipeLookup(category.getRecipeType()).includeHidden().get()) {
            Optional<R> match = stream
                    .filter(candidate -> entryId.equals(category.getRegistryName(candidate)))
                    .findFirst();
            if (match.isPresent()) {
                Optional<IRecipeLayoutDrawable<R>> drawable =
                        manager.createRecipeLayoutDrawable(category, match.get(), emptyFocus(runtime));
                if (drawable.isPresent()) {
                    return drawable.get();
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
            // fall through to the cache path
        }
        return buildDrawableFromCache(runtime, category, entryId);
    }

    private static <R> IRecipeLayoutDrawable<?> buildDrawableFromCache(
            IJeiRuntime runtime, IRecipeCategory<R> category, ResourceLocation entryId) {
        Recipe<?> cached = LabRecipeIndex.recipeById(entryId);
        if (cached == null) {
            return null;
        }
        IRecipeManager manager = runtime.getRecipeManager();
        @SuppressWarnings("unchecked")
        R candidate = (R) cached;
        try {
            return manager.createRecipeLayoutDrawable(category, candidate, emptyFocus(runtime)).orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static IFocusGroup emptyFocus(IJeiRuntime runtime) {
        return runtime.getJeiHelpers().getFocusFactory().createFocusGroup(Collections.emptyList());
    }

    private LabJeiLayoutFinder() {
    }
}