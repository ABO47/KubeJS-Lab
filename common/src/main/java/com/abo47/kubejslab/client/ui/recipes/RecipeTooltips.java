package com.abo47.kubejslab.client.ui.recipes;

import java.util.List;

import net.minecraft.network.chat.Component;

import com.abo47.kubejslab.recipe.model.RecipeField;


public final class RecipeTooltips {
    private RecipeTooltips() {
    }

    public static List<Component> forField(RecipeField field) {
        return switch (field) {
            case EXPERIENCE -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_UNIT_XP),
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_EXPERIENCE));
            case COOKING_TIME, PROCESSING_TIME -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_UNIT_TICKS),
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_TICKS));
            case COUNT -> List.of(Component.translatable(RecipeKeys.RECIPE_UNIT_COUNT));
            case ENERGY -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_UNIT_FE),
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_ENERGY));
            case CREOSOTE_AMOUNT -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_UNIT_MB),
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_CREOSOTE));
            case MOLD -> List.of(Component.translatable(RecipeKeys.RECIPE_TOOLTIP_MOLD));
            case BLUEPRINT_CATEGORY -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_BLUEPRINT_CATEGORY));
            case CLOCHE_RENDER_TYPE -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_RENDER_TYPE));
            case CLOCHE_RENDER_BLOCK -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_RENDER_BLOCK));
            case FLUID_INPUT_AMOUNT, FLUID_OUTPUT_AMOUNT -> List.of(
                    Component.translatable(RecipeKeys.RECIPE_UNIT_MB),
                    Component.translatable(RecipeKeys.RECIPE_TOOLTIP_FLUID_AMOUNT));
            case SHAPELESS, HEAT_REQUIREMENT, KEEP_HELD_ITEM, ACCEPT_MIRRORED,
                    GRID_WIDTH, GRID_HEIGHT -> List.of();
        };
    }
}
