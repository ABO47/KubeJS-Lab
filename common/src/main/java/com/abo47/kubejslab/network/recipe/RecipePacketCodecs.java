package com.abo47.kubejslab.network.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;


public final class RecipePacketCodecs {
    private RecipePacketCodecs() {
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

    public static void writeIngredient(FriendlyByteBuf buf, RecipeIngredient ingredient) {
        if (ingredient instanceof RecipeIngredient.Tag tag) {
            buf.writeVarInt(0);
            buf.writeUtf(tag.tag().toString(), 32767);
        } else if (ingredient instanceof RecipeIngredient.Fluid fluid) {
            buf.writeVarInt(1);
            writeFluid(buf, fluid.fluid());
        } else if (ingredient instanceof RecipeIngredient.Item item) {
            buf.writeVarInt(2);
            writeStack(buf, item.stack());
        } else {
            buf.writeVarInt(2);
            writeStack(buf, ItemStack.EMPTY);
        }
    }

    public static RecipeIngredient readIngredient(FriendlyByteBuf buf) {
        int kind = buf.readVarInt();
        if (kind == 0) {
            return new RecipeIngredient.Tag(ResourceLocation.tryParse(buf.readUtf()));
        }
        if (kind == 1) {
            return new RecipeIngredient.Fluid(readFluid(buf));
        }
        return new RecipeIngredient.Item(readStack(buf));
    }

    public static void writeOutput(FriendlyByteBuf buf, RecipeOutput output) {
        if (output instanceof RecipeOutput.Fluid fluid) {
            buf.writeVarInt(0);
            writeFluid(buf, fluid.fluid());
        } else if (output instanceof RecipeOutput.Item item) {
            buf.writeVarInt(1);
            writeStack(buf, item.stack());
            buf.writeFloat(item.chance());
        } else {
            buf.writeVarInt(1);
            writeStack(buf, ItemStack.EMPTY);
            buf.writeFloat(1f);
        }
    }

    public static RecipeOutput readOutput(FriendlyByteBuf buf) {
        int kind = buf.readVarInt();
        if (kind == 0) {
            return new RecipeOutput.Fluid(readFluid(buf));
        }
        ItemStack stack = readStack(buf);
        float chance = Math.max(0f, Math.min(1f, buf.readFloat()));
        return new RecipeOutput.Item(stack, chance);
    }
}
