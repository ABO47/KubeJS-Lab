package com.abo47.kubejslab.client.ui.machines;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


final class LabSlotData {
    LabSlotKind kind = LabSlotKind.ITEM;
    ItemStack stack = ItemStack.EMPTY;
    ResourceLocation tag;
    FluidStack fluid = FluidStack.empty();
    float chance = 1f;

    void setChance(float chance) {
        this.chance = Math.max(0f, Math.min(1f, chance));
    }

    void setItemValue(ItemStack stack) {
        this.kind = LabSlotKind.ITEM;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.tag = null;
    }

    void setTagValue(ResourceLocation tag) {
        this.kind = LabSlotKind.TAG;
        this.tag = tag;
        if (this.stack.isEmpty()) {
            this.stack = firstTagMember(tag);
        }
    }

    boolean setFluidValue(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return false;
        }
        this.kind = LabSlotKind.FLUID;
        this.fluid = fluid.copy();
        this.stack = ItemStack.EMPTY;
        this.tag = null;
        return true;
    }

    boolean isEmpty() {
        return switch (kind) {
            case FLUID -> fluid.isEmpty();
            case TAG -> tag == null;
            case ITEM -> stack.isEmpty();
        };
    }

    void clear() {
        kind = LabSlotKind.ITEM;
        stack = ItemStack.EMPTY;
        tag = null;
        fluid = FluidStack.empty();
        chance = 1f;
    }

    LabIngredient toIngredient() {
        return switch (kind) {
            case FLUID -> new LabIngredient.Fluid(fluid);
            case TAG -> tag == null ? new LabIngredient.Item(stack) : new LabIngredient.Tag(tag);
            case ITEM -> new LabIngredient.Item(stack);
        };
    }

    LabRecipeOutput toOutput() {
        return switch (kind) {
            case FLUID -> fluid.isEmpty() ? null : new LabRecipeOutput.Fluid(fluid);
            case TAG -> stack.isEmpty() ? null : new LabRecipeOutput.Item(stack, chance);
            case ITEM -> stack.isEmpty() ? null : new LabRecipeOutput.Item(stack, chance);
        };
    }

    private static ItemStack firstTagMember(ResourceLocation tag) {
        TagKey<Item> key = TagKey.create(Registries.ITEM, tag);
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
            return new ItemStack(holder);
        }
        return ItemStack.EMPTY;
    }
}