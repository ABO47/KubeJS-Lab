package com.abo47.kubejslab.client.ui.machines;

import com.abo47.kubejslab.client.ui.base.LabColors;

import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class LabPhantomSlotWidget extends PhantomSlotWidget {
    private final LabMachineLayoutWidget.PhantomHandler handler;
    private LabMachineLayoutWidget dragOwner;
    private boolean tagTooltipSet;
    private RecipeIngredientRole role;

    public LabPhantomSlotWidget(IItemTransfer itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
        this.handler = (LabMachineLayoutWidget.PhantomHandler) itemHandler;
    }

    void setDragOwner(LabMachineLayoutWidget dragOwner) {
        this.dragOwner = dragOwner;
    }

    void setRole(RecipeIngredientRole role) {
        this.role = role;
    }

    @Override
    public void updateScreen() {
        boolean isTag = handler.data().kind == LabMachineLayoutWidget.SlotKind.TAG
                && handler.data().tag != null;
        if (isTag && !tagTooltipSet) {
            setHoverTooltips(Component.literal("#" + handler.data().tag));
            tagTooltipSet = true;
        } else if (!isTag && tagTooltipSet) {
            setHoverTooltips(java.util.List.of());
            tagTooltipSet = false;
        }
    }

    @Override
    public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
        if (role != null) {
            int x = getPositionX();
            int y = getPositionY();
            int color = role == RecipeIngredientRole.INPUT ? 0x402E7CF6 : 0x40FF8C42;
            g.fill(x, y, x + getSizeWidth(), y + getSizeHeight(), color);
        }
        super.drawInBackground(g, mx, my, pt);
        if (handler.data().kind == LabMachineLayoutWidget.SlotKind.TAG) {
            g.drawString(Minecraft.getInstance().font, "#", getPositionX() + getSizeWidth() - 8,
                    getPositionY() + getSizeHeight() - 9, 0xffd9b84c);
        }
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
                dragOwner.paintSlot(button, handler.data());
            } else if (!current.isEmpty()) {
                gui.getModularUIContainer().setCarried(current.copy());
                slotReference.set(ItemStack.EMPTY);
                handler.data().clear();
            } else {
                return false;
            }
        } else if (button == LabColors.MOUSE_BUTTON_RIGHT) {
            dragOwner.beginPaint(button);
            dragOwner.paintSlot(button, handler.data());
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragOwner != null && dragOwner.isPainting(button) && isMouseOverElement(mouseX, mouseY)) {
            dragOwner.paintSlot(button, handler.data());
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
