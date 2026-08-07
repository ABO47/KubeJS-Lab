package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import com.google.gson.JsonObject;


public abstract class ImmersiveEngineeringMachine implements LabRecipeMachine {
    private final ResourceLocation jeiUid;
    private final String jsonType;
    private final List<LabRecipeField> fields;

    protected ImmersiveEngineeringMachine(String type, LabRecipeField... fields) {
        this.jeiUid = new ResourceLocation("immersiveengineering", type);
        this.jsonType = "immersiveengineering:" + type;
        this.fields = List.of(fields);
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

    protected static JsonObject ingredientWithSize(LabIngredient ingredient) {
        if (ingredient instanceof LabIngredient.Item item && item.stack().getCount() > 1) {
            JsonObject sized = new JsonObject();
            sized.addProperty("count", item.stack().getCount());
            sized.add("base_ingredient", LabRecipeJson.itemJson(item.stack()));
            return sized;
        }
        return LabRecipeJson.ingredientJson(ingredient);
    }

    protected static JsonObject fluidTagInput(LabIngredient ingredient) {
        return fluidTagInput(ingredient, -1);
    }

    protected static JsonObject fluidTagInput(LabIngredient ingredient, int amountOverride) {
        JsonObject json = new JsonObject();
        if (ingredient instanceof LabIngredient.Fluid fluid) {
            FluidStack stack = fluid.fluid();
            ResourceLocation tag = firstFluidTag(stack.getFluid());
            if (tag == null) {
                tag = BuiltInRegistries.FLUID.getKey(stack.getFluid());
            }
            if (tag != null) {
                json.addProperty("tag", tag.toString());
            }
            json.addProperty("amount", amountOverride > 0 ? amountOverride : stack.getAmount());
            if (stack.getTag() != null) {
                json.addProperty("nbt", stack.getTag().toString());
            }
        }
        return json;
    }

    protected static JsonObject outputWithAmount(LabRecipeOutput output, int amountOverride) {
        if (output instanceof LabRecipeOutput.Fluid fluid && amountOverride > 0) {
            return LabRecipeJson.fluidJson(fluid.fluid().copy(amountOverride));
        }
        return readOutput(output);
    }

    protected static JsonObject readOutput(LabRecipeOutput output) {
        if (output instanceof LabRecipeOutput.Item item) {
            return LabRecipeJson.itemWithCount(item.stack());
        }
        if (output instanceof LabRecipeOutput.Fluid fluid) {
            return LabRecipeJson.fluidJson(fluid.fluid());
        }
        return new JsonObject();
    }

    private static ResourceLocation firstFluidTag(Fluid fluid) {
        return fluid.builtInRegistryHolder().tags().map(TagKey::location).findFirst().orElse(null);
    }
}