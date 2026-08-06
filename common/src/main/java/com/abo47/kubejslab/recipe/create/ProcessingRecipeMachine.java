package com.abo47.kubejslab.recipe.create;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;

public final class ProcessingRecipeMachine implements LabRecipeMachine {
    private final ResourceLocation jeiUid;
    private final boolean supportsDuration;
    private final boolean supportsHeat;
    private final boolean supportsKeepHeldItem;
    private final List<LabRecipeField> fields;

    public ProcessingRecipeMachine(ResourceLocation jeiUid, boolean supportsDuration, boolean supportsHeat,
            boolean supportsKeepHeldItem) {
        this.jeiUid = jeiUid;
        this.supportsDuration = supportsDuration;
        this.supportsHeat = supportsHeat;
        this.supportsKeepHeldItem = supportsKeepHeldItem;
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
    public ResourceLocation jeiUid() {
        return jeiUid;
    }

    @Override
    public String jsonType() {
        return jeiUid.toString();
    }

    @Override
    public List<LabRecipeField> fields() {
        return fields;
    }

    @Override
    public JsonObject buildJson(String jsonType, List<ItemStack> inputs, ItemStack output, LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        JsonArray ingredients = new JsonArray();
        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                ingredients.add(LabRecipeJson.itemJson(stack));
            }
        }
        json.add("ingredients", ingredients);
        JsonArray results = new JsonArray();
        JsonObject result = new JsonObject();
        result.addProperty("item", output.getItem().builtInRegistryHolder().key().location().toString());
        if (output.getCount() != 1) {
            result.addProperty("count", output.getCount());
        }
        if (output.hasTag()) {
            result.add("nbt", JsonParser.parseString(output.getTag().toString()));
        }
        results.add(result);
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
                    current.count(), processing.getProcessingDuration(), heat, current.keepHeldItem());
        }
        if (original instanceof ItemApplicationRecipe application) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(),
                    application.shouldKeepHeldItem());
        }
        return current;
    }
}
