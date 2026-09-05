package com.abo47.kubejslab.client.ui.machines;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;


final class PhantomHandler implements IItemTransfer {
    private final SlotData data;

    PhantomHandler(SlotData data) {
        this.data = data;
    }

    SlotData data() {
        return data;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return data.stack;
    }

    @Override
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate, boolean notifyChanges) {
        if (simulate) {
            return ItemStack.EMPTY;
        }
        data.setItemValue(stack);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate, boolean notifyChanges) {
        if (simulate) {
            return data.stack.copy();
        }
        ItemStack extracted = data.stack;
        data.setItemValue(ItemStack.EMPTY);
        return extracted;
    }

    @Override
    public int getSlotLimit(int index) {
        return 64;
    }

    @Override
    public boolean isItemValid(int index, ItemStack stack) {
        return true;
    }

    @Override
    public Object createSnapshot() {
        return new Object[] {data.stack};
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {
        data.setItemValue((ItemStack) ((Object[]) snapshot)[0]);
    }
}