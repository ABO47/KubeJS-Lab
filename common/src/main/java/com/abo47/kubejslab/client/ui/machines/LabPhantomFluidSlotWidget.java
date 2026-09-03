package com.abo47.kubejslab.client.ui.machines;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabPickTarget;

import mezz.jei.api.recipe.RecipeIngredientRole;


public final class LabPhantomFluidSlotWidget extends Widget implements LabPickTarget {
    private final LabSlotData data;
    private LabMachineLayoutWidget dragOwner;
    private RecipeIngredientRole role;

    public LabPhantomFluidSlotWidget(LabSlotData data, int x, int y) {
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
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (dragOwner != null && isMouseOverElement(mouseX, mouseY) && !data.fluid.isEmpty()) {
            dragOwner.adjustStackCount(data, (int) Math.signum(wheelDelta));
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        int x = getPositionX();
        int y = getPositionY();
        SlotWidget.ITEM_SLOT_TEXTURE.draw(graphics, mouseX, mouseY, x, y, 18, 18);
        if (role != null) {
            int color = role == RecipeIngredientRole.INPUT ? LabColors.FLUID_INPUT_TINT : LabColors.FLUID_OUTPUT_TINT;
            graphics.fill(x, y, x + 18, y + 18, color);
        }
        if (!data.fluid.isEmpty()) {
            long capacity = Math.max(data.fluid.getAmount(), 1000);
            DrawerHelper.drawFluidForGui(graphics, data.fluid, capacity, x + 1, y + 1, 16, 16);
        }
    }

    @Override
    public void updateScreen() {
        if (!data.fluid.isEmpty()) {
            setHoverTooltips(Component.literal(data.fluid.getDisplayName().getString()),
                    Component.literal(data.fluid.getAmount() + " mB"));
        }
    }
}
