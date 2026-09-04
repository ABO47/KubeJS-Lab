package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;

import com.google.gson.JsonObject;


public abstract class ImmersiveEngineeringMachine implements RecipeHandler {
    private final ResourceLocation jeiUid;
    private final String jsonType;
    private final List<RecipeField> fields;

    protected ImmersiveEngineeringMachine(String type, RecipeField... fields) {
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
    public List<RecipeField> fields() {
        return fields;
    }

    protected static JsonObject ingredientWithSize(RecipeIngredient ingredient) {
        if (ingredient instanceof RecipeIngredient.Item item && item.stack().getCount() > 1) {
            JsonObject sized = new JsonObject();
            sized.addProperty("count", item.stack().getCount());
            sized.add("base_ingredient", RecipeJson.itemJson(item.stack()));
            return sized;
        }
        return RecipeJson.ingredientJson(ingredient);
    }

    protected static JsonObject fluidTagInput(RecipeIngredient ingredient) {
        return fluidTagInput(ingredient, -1);
    }

    protected static JsonObject fluidTagInput(RecipeIngredient ingredient, int amountOverride) {
        JsonObject json = new JsonObject();
        if (ingredient instanceof RecipeIngredient.Fluid fluid) {
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

    protected static JsonObject outputWithAmount(RecipeOutput output, int amountOverride) {
        if (output instanceof RecipeOutput.Fluid fluid && amountOverride > 0) {
            return RecipeJson.fluidJson(fluid.fluid().copy(amountOverride));
        }
        return readOutput(output);
    }

    protected static JsonObject readOutput(RecipeOutput output) {
        if (output instanceof RecipeOutput.Item item) {
            return RecipeJson.itemWithCount(item.stack());
        }
        if (output instanceof RecipeOutput.Fluid fluid) {
            return RecipeJson.fluidJson(fluid.fluid());
        }
        return new JsonObject();
    }

    private static ResourceLocation firstFluidTag(Fluid fluid) {
        return fluid.builtInRegistryHolder().tags().map(TagKey::location).findFirst().orElse(null);
    }
}