package com.abo47.kubejslab.network.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class LabPacketCodecs {
    private LabPacketCodecs() {
    }

    public static void writeStack(FriendlyByteBuf buf, ItemStack stack) {
        buf.writeBoolean(!stack.isEmpty());
        if (stack.isEmpty()) {
            return;
        }
        buf.writeUtf(stack.getItem().builtInRegistryHolder().key().location().toString());
        buf.writeVarInt(stack.getCount());
        CompoundTag tag = stack.getTag();
        buf.writeNbt(tag);
    }

    public static ItemStack readStack(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(buf.readUtf())));
        stack.setCount(buf.readVarInt());
        stack.setTag(buf.readNbt());
        return stack;
    }
}
