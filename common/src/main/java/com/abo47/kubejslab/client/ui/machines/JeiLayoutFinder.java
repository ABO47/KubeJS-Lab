package com.abo47.kubejslab.client.ui.machines;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.client.jei.JeiBridgePlugin;
import com.abo47.kubejslab.client.ui.recipes.RecipeIndex;
import com.abo47.kubejslab.recipe.MachineRegistry;
import com.abo47.kubejslab.recipe.RecipeHandler;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;


final class JeiLayoutFinder {
    static IRecipeLayoutDrawable findSampleLayout(MachineView machine) {
        if (JeiBridgePlugin.runtime() == null) {
            return null;
        }
        return findSample(JeiBridgePlugin.runtime(), resolveLayoutCategory(machine));
    }

    static IRecipeLayoutDrawable findJeiLayout(MachineView machine, RecipeIndex.RecipeEntry entry) {
        if (JeiBridgePlugin.runtime() == null || machine == null) {
            return null;
        }
        return findEntry(JeiBridgePlugin.runtime(), resolveLayoutCategory(machine), entry.id());
    }

    private static IRecipeCategory<?> resolveLayoutCategory(MachineView machine) {
        RecipeHandler support = machine == null ? null : MachineRegistry.get(machine.recipeTypeUid());
        if (support == null || support.recipeIdSourceUid() == null) {
            return machine.category();
        }
        IRecipeManager manager = JeiBridgePlugin.runtime().getRecipeManager();
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
        }
        return buildDrawableFromCache(runtime, category, entryId);
    }

    private static <R> IRecipeLayoutDrawable<?> buildDrawableFromCache(
            IJeiRuntime runtime, IRecipeCategory<R> category, ResourceLocation entryId) {
        Recipe<?> cached = RecipeIndex.recipeById(entryId);
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

    private JeiLayoutFinder() {
    }
}