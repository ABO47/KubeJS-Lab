package com.abo47.kubejslab.client.ui.shell;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.runtime.LootScriptWriter;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;

final class BulkSaver {
    private static final String RECIPE_TAB = "recipe";
    private static final String ITEM_TAB = "item";
    private static final String BLOCK_TAB = "block";
    private static final String LOOT_TAB = "loot";

    private final WorkspacePanel panel;
    private final Map<String, String> cleanSnapshots = new HashMap<>();

    BulkSaver(WorkspacePanel panel) {
        this.panel = panel;
    }

    void markRecipeClean() {
        markClean(RECIPE_TAB);
    }

    void markItemClean() {
        markClean(ITEM_TAB);
    }

    void markBlockClean() {
        markClean(BLOCK_TAB);
    }

    void markLootClean() {
        markClean(LOOT_TAB);
    }

    private void markClean(String tab) {
        cleanSnapshots.put(tab, fingerprint(tab));
    }

    int saveAll() {
        int saved = 0;
        if (canSaveRecipe() && isDirty(RECIPE_TAB) && panel.saver.saveRecipe()) {
            saved++;
            markClean(RECIPE_TAB);
        }
        if (canSaveItem() && isDirty(ITEM_TAB) && panel.itemSaver.saveItem()) {
            saved++;
            markClean(ITEM_TAB);
        }
        if (canSaveBlock() && isDirty(BLOCK_TAB) && panel.blockSaver.saveBlock()) {
            saved++;
            markClean(BLOCK_TAB);
        }
        if (canSaveLoot() && isDirty(LOOT_TAB) && panel.lootSaver.saveLoot()) {
            saved++;
            markClean(LOOT_TAB);
        }
        if (saved == 0) {
            saveCurrentWithFeedback();
        }
        return saved;
    }

    private boolean isDirty(String tab) {
        String clean = cleanSnapshots.get(tab);
        return clean == null || !clean.equals(fingerprint(tab));
    }

    private String fingerprint(String tab) {
        return switch (tab) {
            case RECIPE_TAB -> recipeFingerprint();
            case ITEM_TAB -> itemFingerprint();
            case BLOCK_TAB -> blockFingerprint();
            case LOOT_TAB -> lootFingerprint();
            default -> "";
        };
    }

    private String recipeFingerprint() {
        if (panel.machineLayout == null || panel.machineDropdown == null || panel.settingsWidget == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (panel.machineDropdown.getSelectedMachine() != null) {
            sb.append(panel.machineDropdown.getSelectedMachine().recipeTypeUid());
        }
        sb.append('|');
        for (RecipeIngredient input : panel.machineLayout.getInputs()) {
            sb.append(ingredientKey(input)).append(';');
        }
        sb.append('|');
        for (RecipeOutput output : panel.machineLayout.getOutputs()) {
            sb.append(outputKey(output)).append(';');
        }
        sb.append('|').append(panel.settingsWidget.getValues());
        return sb.toString();
    }

    private String itemFingerprint() {
        if (panel.itemSettings == null) {
            return "";
        }
        return panel.itemSettings.getType() + "|" + panel.itemSettings.getValues() + "|"
                + panel.itemSettings.getTags() + "|" + panel.itemSettings.getActions();
    }

    private String blockFingerprint() {
        if (panel.blockSettings == null) {
            return "";
        }
        return panel.blockSettings.getType() + "|" + panel.blockSettings.getValues() + "|"
                + panel.blockSettings.getTags() + "|" + panel.blockSettings.getActions();
    }

    private String lootFingerprint() {
        if (panel.lootSettings == null) {
            return "";
        }
        return panel.lootSettings.getLootType() + "|" + panel.lootSettings.getValues();
    }

    private static String ingredientKey(RecipeIngredient ingredient) {
        if (ingredient instanceof RecipeIngredient.Item item) {
            return stackKey(item.stack());
        }
        if (ingredient instanceof RecipeIngredient.Tag tag) {
            return "t:" + tag.tag();
        }
        if (ingredient instanceof RecipeIngredient.Fluid fluid) {
            return fluidKey(fluid.fluid());
        }
        return "empty";
    }

    private static String outputKey(RecipeOutput output) {
        if (output instanceof RecipeOutput.Item item) {
            return stackKey(item.stack()) + "@" + item.chance();
        }
        if (output instanceof RecipeOutput.Fluid fluid) {
            return fluidKey(fluid.fluid());
        }
        return "empty";
    }

    private static String stackKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return "i:" + id + "x" + stack.getCount() + (stack.hasTag() ? "#" + stack.getTag() : "");
    }

    private static String fluidKey(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty() || fluid.getFluid() == null) {
            return "empty";
        }
        return "f:" + BuiltInRegistries.FLUID.getKey(fluid.getFluid()) + "x" + fluid.getAmount();
    }

    private void saveCurrentWithFeedback() {
        int selected = panel.getSelectedTabIndex();
        String tab = switch (selected) {
            case 1 -> ITEM_TAB;
            case 2 -> BLOCK_TAB;
            case 3 -> LOOT_TAB;
            default -> RECIPE_TAB;
        };
        if (cleanSnapshots.containsKey(tab) && !isDirty(tab)) {
            return;
        }
        switch (selected) {
            case 1 -> panel.itemSaver.saveItem();
            case 2 -> panel.blockSaver.saveBlock();
            case 3 -> panel.lootSaver.saveLoot();
            default -> panel.saver.saveRecipe();
        }
    }

    private boolean canSaveRecipe() {
        if (panel.machineLayout == null) {
            return false;
        }
        for (RecipeIngredient input : panel.machineLayout.getInputs()) {
            if (input != null && !input.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean canSaveItem() {
        if (panel.itemSettings == null) {
            return false;
        }
        if (panel.items != null && panel.items.itemMode == WorkspacePanel.EditMode.MODIFY
                && panel.items.itemModifyTarget != null) {
            return true;
        }
        String type = panel.itemSettings.getType();
        String name = panel.itemSettings.getValues().displayName();
        return type != null && !type.isBlank() && name != null && !name.isBlank();
    }

    private boolean canSaveBlock() {
        if (panel.blockSettings == null) {
            return false;
        }
        if (panel.blocks != null && panel.blocks.blockMode == WorkspacePanel.EditMode.MODIFY
                && panel.blocks.blockModifyTarget != null) {
            return true;
        }
        String type = panel.blockSettings.getType();
        String name = panel.blockSettings.getValues().displayName();
        return type != null && !type.isBlank() && name != null && !name.isBlank();
    }

    private boolean canSaveLoot() {
        if (panel.lootSettings == null) {
            return false;
        }
        if (panel.loot != null && panel.loot.lootMode == WorkspacePanel.EditMode.MODIFY
                && panel.loot.lootModifyTarget != null) {
            return true;
        }
        String target = panel.lootSettings.getValues().targetId();
        if (target == null || target.isBlank()) {
            return false;
        }
        for (LootPoolValues pool : panel.lootSettings.getValues().pools()) {
            if (pool != null && LootScriptWriter.writesPool(pool)) {
                return true;
            }
        }
        return false;
    }
}
