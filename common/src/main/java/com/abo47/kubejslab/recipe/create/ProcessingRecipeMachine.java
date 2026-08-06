package com.abo47.kubejslab.recipe.create;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

public final class ProcessingRecipeMachine implements LabRecipeMachine {
    private final ResourceLocation jeiUid;
    private final String jsonType;
    private final boolean supportsDuration;
    private final boolean supportsHeat;
    private final boolean supportsKeepHeldItem;
    private final boolean supportsChance;
    private final List<LabRecipeField> fields;

    public ProcessingRecipeMachine(ResourceLocation jeiUid, String jsonType, boolean supportsDuration,
            boolean supportsHeat, boolean supportsKeepHeldItem) {
        this(jeiUid, jsonType, supportsDuration, supportsHeat, supportsKeepHeldItem, false);
    }

    public ProcessingRecipeMachine(ResourceLocation jeiUid, String jsonType, boolean supportsDuration,
            boolean supportsHeat, boolean supportsKeepHeldItem, boolean supportsChance) {
        this.jeiUid = jeiUid;
        this.jsonType = jsonType;
        this.supportsDuration = supportsDuration;
        this.supportsHeat = supportsHeat;
        this.supportsKeepHeldItem = supportsKeepHeldItem;
        this.supportsChance = supportsChance;
        this.fields = new ArrayList<>();
        if (supportsDuration) {
            this.fields.add(LabRecipeField.PROCESSING_TIME);
        }
        if (supportsHeat) {
            this.fields.add(LabRecipeField.HEAT_REQUIREMENT);
        }
        if (supportsKeepHeldItem) {
            this.fields.add(LabRecipeField.KEEP_HELD_ITEM);
        }
    }

    @Override
    public boolean supportsChance() {
        return supportsChance;
    }

    @Override
    public ResourceLocation jeiUid() {
        return jeiUid;
    }

    @Override
    public String jsonType() {
        return jsonType;
    }

    @Override
    public List<LabRecipeField> fields() {
        return fields;
    }

    @Override
    public JsonObject buildJson(String jsonType, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        JsonArray ingredients = new JsonArray();
        for (LabIngredient ingredient : inputs) {
            if (!ingredient.isEmpty()) {
                ingredients.add(LabRecipeJson.ingredientJson(ingredient));
            }
        }
        json.add("ingredients", ingredients);
        JsonArray results = new JsonArray();
        for (LabRecipeOutput output : outputs) {
            if (!output.isEmpty()) {
                results.add(LabRecipeJson.outputJson(output));
            }
        }
        json.add("results", results);
        if (supportsDuration && values.processingTime() > 0) {
            json.addProperty("processingTime", values.processingTime());
        }
        if (supportsHeat && values.heatRequirement() != HeatRequirement.NONE) {
            json.addProperty("heatRequirement", values.heatRequirement().name().toLowerCase());
        }
        if (supportsKeepHeldItem && values.keepHeldItem()) {
            json.addProperty("keepHeldItem", true);
        }
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof ProcessingRecipe<?> processing) {
            HeatRequirement heat = switch (processing.getRequiredHeat()) {
                case HEATED -> HeatRequirement.HEATED;
                case SUPERHEATED -> HeatRequirement.SUPERHEATED;
                default -> HeatRequirement.NONE;
            };
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), processing.getProcessingDuration(), heat, current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight());
        }
        if (original instanceof ItemApplicationRecipe application) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(),
                    application.shouldKeepHeldItem(), current.acceptMirrored(), current.gridWidth(),
                    current.gridHeight());
        }
        return current;
    }
}
