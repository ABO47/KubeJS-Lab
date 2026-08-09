package com.abo47.kubejslab.recipe.model;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;


public sealed interface LabRecipeOutput permits LabRecipeOutput.Item, LabRecipeOutput.Fluid {

    boolean isEmpty();

    record Item(ItemStack stack, float chance) implements LabRecipeOutput {
        public Item {
            stack = stack == null ? ItemStack.EMPTY : stack;
            chance = Math.max(0f, Math.min(1f, chance));
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }

    record Fluid(FluidStack fluid) implements LabRecipeOutput {
        @Override
        public boolean isEmpty() {
            return fluid == null || fluid.isEmpty();
        }
    }

    static ItemStack firstItem(List<LabRecipeOutput> outputs) {
        for (LabRecipeOutput output : outputs) {
            if (output instanceof Item item && !item.isEmpty()) {
                return item.stack();
            }
        }
        return ItemStack.EMPTY;
    }

    static ItemStack displayStack(List<LabRecipeOutput> outputs) {
        ItemStack firstItem = firstItem(outputs);
        if (!firstItem.isEmpty()) {
            return firstItem;
        }
        for (LabRecipeOutput output : outputs) {
            if (output instanceof Fluid fluid && !fluid.isEmpty()) {
                return new ItemStack(fluid.fluid().getFluid().getBucket());
            }
        }
        return ItemStack.EMPTY;
    }
}
