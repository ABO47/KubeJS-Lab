package com.abo47.kubejslab.recipe.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;


public sealed interface LabIngredient permits LabIngredient.Item, LabIngredient.Tag, LabIngredient.Fluid {

    boolean isEmpty();

    record Item(ItemStack stack) implements LabIngredient {
        @Override
        public boolean isEmpty() {
            return stack == null || stack.isEmpty();
        }
    }

    record Tag(ResourceLocation tag) implements LabIngredient {
        @Override
        public boolean isEmpty() {
            return tag == null;
        }
    }

    record Fluid(FluidStack fluid) implements LabIngredient {
        @Override
        public boolean isEmpty() {
            return fluid == null || fluid.isEmpty();
        }
    }
}
