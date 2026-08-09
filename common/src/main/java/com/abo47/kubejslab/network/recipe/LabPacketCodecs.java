package com.abo47.kubejslab.network.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;


public final class LabPacketCodecs {
    private LabPacketCodecs() {
    }

    public static void writeStack(FriendlyByteBuf buf, ItemStack stack) {
        buf.writeBoolean(!stack.isEmpty());
        if (stack.isEmpty()) {
            return;
        }
        buf.writeUtf(stack.getItem().builtInRegistryHolder().key().location().toString(), 32767);
        buf.writeVarInt(stack.getCount());
        buf.writeNbt(stack.getTag());
    }

    public static ItemStack readStack(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(buf.readUtf())));
        stack.setCount(buf.readVarInt());
        stack.setTag(buf.readNbt());
        return stack;
    }

    public static void writeFluid(FriendlyByteBuf buf, FluidStack fluid) {
        buf.writeBoolean(!fluid.isEmpty());
        if (fluid.isEmpty()) {
            return;
        }
        buf.writeUtf(fluid.getFluid().builtInRegistryHolder().key().location().toString(), 32767);
        buf.writeVarLong(fluid.getAmount());
        buf.writeNbt(fluid.getTag());
    }

    public static FluidStack readFluid(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return FluidStack.empty();
        }
        FluidStack fluid = FluidStack.create(
                BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(buf.readUtf())), buf.readVarLong());
        CompoundTag tag = buf.readNbt();
        if (tag != null) {
            fluid.setTag(tag);
        }
        return fluid;
    }

    public static void writeIngredient(FriendlyByteBuf buf, LabIngredient ingredient) {
        if (ingredient instanceof LabIngredient.Tag tag) {
            buf.writeVarInt(0);
            buf.writeUtf(tag.tag().toString(), 32767);
        } else if (ingredient instanceof LabIngredient.Fluid fluid) {
            buf.writeVarInt(1);
            writeFluid(buf, fluid.fluid());
        } else if (ingredient instanceof LabIngredient.Item item) {
            buf.writeVarInt(2);
            writeStack(buf, item.stack());
        } else {
            buf.writeVarInt(2);
            writeStack(buf, ItemStack.EMPTY);
        }
    }

    public static LabIngredient readIngredient(FriendlyByteBuf buf) {
        int kind = buf.readVarInt();
        if (kind == 0) {
            return new LabIngredient.Tag(ResourceLocation.tryParse(buf.readUtf()));
        }
        if (kind == 1) {
            return new LabIngredient.Fluid(readFluid(buf));
        }
        return new LabIngredient.Item(readStack(buf));
    }

    public static void writeOutput(FriendlyByteBuf buf, LabRecipeOutput output) {
        if (output instanceof LabRecipeOutput.Fluid fluid) {
            buf.writeVarInt(0);
            writeFluid(buf, fluid.fluid());
        } else if (output instanceof LabRecipeOutput.Item item) {
            buf.writeVarInt(1);
            writeStack(buf, item.stack());
            buf.writeFloat(item.chance());
        } else {
            buf.writeVarInt(1);
            writeStack(buf, ItemStack.EMPTY);
            buf.writeFloat(1f);
        }
    }

    public static LabRecipeOutput readOutput(FriendlyByteBuf buf) {
        int kind = buf.readVarInt();
        if (kind == 0) {
            return new LabRecipeOutput.Fluid(readFluid(buf));
        }
        ItemStack stack = readStack(buf);
        float chance = Math.max(0f, Math.min(1f, buf.readFloat()));
        return new LabRecipeOutput.Item(stack, chance);
    }
}
