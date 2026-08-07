package com.abo47.kubejslab.client.ui.picker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;


public sealed interface LabPick permits LabPick.Item, LabPick.Tag, LabPick.Fluid {

    default ItemStack carried() {
        return ItemStack.EMPTY;
    }

    record Item(ItemStack stack) implements LabPick {
        @Override
        public ItemStack carried() {
            return stack.copy();
        }
    }

    record Tag(ResourceLocation tag) implements LabPick {
        @Override
        public ItemStack carried() {
            return LabPickerEntries.tagPreview(tag);
        }
    }

    record Fluid(FluidStack fluid) implements LabPick {
        @Override
        public ItemStack carried() {
            return new ItemStack(fluid.getFluid().getBucket());
        }
    }
}
