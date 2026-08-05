package com.abo47.kubejslab.client.ui;

import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.world.item.ItemStack;

public final class LabPhantomSlotWidget extends PhantomSlotWidget {
    private final LabMachineLayoutWidget.PhantomHandler handler;
    private LabMachineLayoutWidget dragOwner;

    public LabPhantomSlotWidget(IItemTransfer itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
        this.handler = (LabMachineLayoutWidget.PhantomHandler) itemHandler;
    }

    void setDragOwner(LabMachineLayoutWidget dragOwner) {
        this.dragOwner = dragOwner;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (slotReference == null || dragOwner == null || !isMouseOverElement(mouseX, mouseY) || gui == null) {
            return false;
        }
        ItemStack carried = gui.getModularUIContainer().getCarried();
        ItemStack current = slotReference.getItem();

        if (button == LabColors.MOUSE_BUTTON_LEFT) {
            if (!carried.isEmpty()) {
                dragOwner.beginPaint(button);
                dragOwner.paintSlot(button, handler);
            } else if (!current.isEmpty()) {
                gui.getModularUIContainer().setCarried(current.copy());
                slotReference.set(ItemStack.EMPTY);
            } else {
                return false;
            }
        } else if (button == LabColors.MOUSE_BUTTON_RIGHT) {
            dragOwner.beginPaint(button);
            dragOwner.paintSlot(button, handler);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragOwner != null && dragOwner.isPainting(button) && isMouseOverElement(mouseX, mouseY)) {
            dragOwner.paintSlot(button, handler);
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
}
