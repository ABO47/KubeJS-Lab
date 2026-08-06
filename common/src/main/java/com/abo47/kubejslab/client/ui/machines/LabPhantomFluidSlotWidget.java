package com.abo47.kubejslab.client.ui.machines;

import com.abo47.kubejslab.client.ui.base.LabColors;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;

public final class LabPhantomFluidSlotWidget extends Widget {
    private final LabMachineLayoutWidget.SlotData data;
    private LabMachineLayoutWidget dragOwner;
    private RecipeIngredientRole role;

    public LabPhantomFluidSlotWidget(LabMachineLayoutWidget.SlotData data, int x, int y) {
        super(x, y, 18, 18);
        this.data = data;
    }

    void setDragOwner(LabMachineLayoutWidget dragOwner) {
        this.dragOwner = dragOwner;
    }

    void setRole(RecipeIngredientRole role) {
        this.role = role;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (dragOwner == null || gui == null || !isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        ItemStack carried = gui.getModularUIContainer().getCarried();
        if (button == LabColors.MOUSE_BUTTON_RIGHT && carried.isEmpty()) {
            dragOwner.beginPaint(button);
            dragOwner.paintSlot(button, data);
            return true;
        }
        if (carried.isEmpty()) {
            return false;
        }
        dragOwner.beginPaint(button);
        dragOwner.paintSlot(button, data);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragOwner != null && dragOwner.isPainting(button) && isMouseOverElement(mouseX, mouseY)) {
            dragOwner.paintSlot(button, data);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragOwner != null && dragOwner.isPainting(button)) {
            dragOwner.endPaint();
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX();
        int y = getPositionY();
        if (role != null) {
            int color = role == RecipeIngredientRole.INPUT ? 0x402E7CF6 : 0x40FF8C42;
            graphics.fill(x, y, x + 18, y + 18, color);
        }
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        SlotWidget.ITEM_SLOT_TEXTURE.draw(graphics, mouseX, mouseY, x, y, 18, 18);
        if (!data.fluid.isEmpty()) {
            long capacity = Math.max(data.fluid.getAmount(), 1000);
            DrawerHelper.drawFluidForGui(graphics, data.fluid, capacity, x + 1, y + 1, 16, 16);
        }
    }
}
