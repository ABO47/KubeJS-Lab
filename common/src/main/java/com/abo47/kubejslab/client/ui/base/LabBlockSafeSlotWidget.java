package com.abo47.kubejslab.client.ui.base;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;


public class LabBlockSafeSlotWidget extends Widget {
    private ItemStack stack = ItemStack.EMPTY;
    private Runnable onChange;

    public LabBlockSafeSlotWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
        setClientSideWidget();
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public void setBlockId(String id) {
        if (id == null || id.isBlank()) {
            stack = ItemStack.EMPTY;
        } else {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null) {
                Item item = BuiltInRegistries.ITEM.get(key);
                if (item != null) {
                    stack = new ItemStack(item);
                }
            }
        }
    }

    public String getBlockId() {
        if (stack.getItem() instanceof BlockItem blockItem) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return key.toString();
        }
        return "";
    }

    @Nullable
    public ItemStack getStack() {
        return stack.isEmpty() ? null : stack;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY) || gui == null) {
            return false;
        }
        ItemStack carried = gui.getModularUIContainer().getCarried();
        if (carried.isEmpty()) {
            stack = ItemStack.EMPTY;
            if (onChange != null) {
                onChange.run();
            }
            return true;
        }
        if (carried.getItem() instanceof BlockItem) {
            stack = carried.copyWithCount(1);
            if (onChange != null) {
                onChange.run();
            }
        }
        return true;
    }

@Override
    public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        int cx = getPositionX() + (getSizeWidth() - 18) / 2;
        int cy = getPositionY() + (getSizeHeight() - 18) / 2;
        SlotWidget.ITEM_SLOT_TEXTURE.draw(graphics, mouseX, mouseY, cx, cy, 18, 18);
        if (!stack.isEmpty()) {
            com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack(graphics, stack, cx + 1, cy + 1, -1, null);
        }
    }
}