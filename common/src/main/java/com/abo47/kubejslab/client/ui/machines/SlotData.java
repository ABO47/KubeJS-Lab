package com.abo47.kubejslab.client.ui.machines;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotKind;


final class SlotData {
    private final SlotKind initialKind;
    SlotKind kind;
    ItemStack stack = ItemStack.EMPTY;
    ResourceLocation tag;
    FluidStack fluid = FluidStack.empty();
    float chance = 1f;

    SlotData() {
        this(false);
    }

    SlotData(boolean fluidSlot) {
        this.initialKind = fluidSlot ? SlotKind.FLUID : SlotKind.ITEM;
        this.kind = initialKind;
    }

    void setChance(float chance) {
        this.chance = Math.max(0f, Math.min(1f, chance));
    }

    void setItemValue(ItemStack stack) {
        this.kind = SlotKind.ITEM;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.tag = null;
    }

    void setTagValue(ResourceLocation tag) {
        this.kind = SlotKind.TAG;
        this.tag = tag;
        if (this.stack.isEmpty()) {
            this.stack = firstTagMember(tag);
        }
    }

    boolean setFluidValue(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return false;
        }
        this.kind = SlotKind.FLUID;
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
        kind = initialKind;
        stack = ItemStack.EMPTY;
        tag = null;
        fluid = FluidStack.empty();
        chance = 1f;
    }

    RecipeIngredient toIngredient() {
        return switch (kind) {
            case FLUID -> new RecipeIngredient.Fluid(fluid);
            case TAG -> tag == null ? new RecipeIngredient.Item(stack) : new RecipeIngredient.Tag(tag);
            case ITEM -> new RecipeIngredient.Item(stack);
        };
    }

    RecipeOutput toOutput() {
        return switch (kind) {
            case FLUID -> fluid.isEmpty() ? null : new RecipeOutput.Fluid(fluid);
            case TAG -> stack.isEmpty() ? null : new RecipeOutput.Item(stack, chance);
            case ITEM -> stack.isEmpty() ? null : new RecipeOutput.Item(stack, chance);
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