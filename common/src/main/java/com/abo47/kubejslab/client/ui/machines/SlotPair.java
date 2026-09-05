package com.abo47.kubejslab.client.ui.machines;

import com.abo47.kubejslab.recipe.model.SlotTint;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;


record SlotPair(SlotData data, IRecipeSlotView view, RecipeIngredientRole role, int gx, int gy,
        SlotTint tint) {
}