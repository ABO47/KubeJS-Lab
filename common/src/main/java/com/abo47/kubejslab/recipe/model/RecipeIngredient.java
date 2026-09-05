package com.abo47.kubejslab.recipe.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;


public sealed interface RecipeIngredient permits RecipeIngredient.Item, RecipeIngredient.Tag, RecipeIngredient.Fluid {

    boolean isEmpty();

    record Item(ItemStack stack) implements RecipeIngredient {
        @Override
        public boolean isEmpty() {
            return stack == null || stack.isEmpty();
        }
    }

    record Tag(ResourceLocation tag) implements RecipeIngredient {
        @Override
        public boolean isEmpty() {
            return tag == null;
        }
    }

    record Fluid(FluidStack fluid) implements RecipeIngredient {
        @Override
        public boolean isEmpty() {
            return fluid == null || fluid.isEmpty();
        }
    }
}
