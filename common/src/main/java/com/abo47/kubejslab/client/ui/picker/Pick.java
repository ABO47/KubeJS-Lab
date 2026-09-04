package com.abo47.kubejslab.client.ui.picker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;


public sealed interface Pick permits Pick.Item, Pick.Tag, Pick.Fluid {

    default ItemStack carried() {
        return ItemStack.EMPTY;
    }

    record Item(ItemStack stack) implements Pick {
        @Override
        public ItemStack carried() {
            return stack.copy();
        }
    }

    record Tag(ResourceLocation tag) implements Pick {
        @Override
        public ItemStack carried() {
            return PickerEntries.tagPreview(tag);
        }
    }

    record Fluid(FluidStack fluid) implements Pick {
        @Override
        public ItemStack carried() {
            return new ItemStack(fluid.getFluid().getBucket());
        }
    }
}
