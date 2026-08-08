package com.abo47.kubejslab.client.ui.machines;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import com.mojang.blaze3d.systems.RenderSystem;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.recipe.model.LabSlotKind;
import com.abo47.kubejslab.recipe.model.LabSlotTint;

import mezz.jei.api.recipe.RecipeIngredientRole;


public final class LabPhantomSlotWidget extends PhantomSlotWidget {
    private final LabPhantomHandler handler;
    private LabMachineLayoutWidget dragOwner;
    private boolean tagTooltipSet;
    private RecipeIngredientRole role;
    private LabSlotTint tint = LabSlotTint.NORMAL;

    public LabPhantomSlotWidget(IItemTransfer itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
        this.handler = (LabPhantomHandler) itemHandler;
    }

    void setDragOwner(LabMachineLayoutWidget dragOwner) {
        this.dragOwner = dragOwner;
    }

    void setRole(RecipeIngredientRole role) {
        this.role = role;
    }

    void setTint(LabSlotTint tint) {
        this.tint = tint;
    }

    @Override
    public void updateScreen() {
        boolean isTag = handler.data().kind == LabSlotKind.TAG
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
        if (role == null) {
            super.drawInBackground(g, mx, my, pt);
        } else {
            int x = getPositionX();
            int y = getPositionY();
            SlotWidget.ITEM_SLOT_TEXTURE.draw(g, mx, my, x, y, getSizeWidth(), getSizeHeight());
            g.fill(x, y, x + getSizeWidth(), y + getSizeHeight(), tintColor());
            ItemStack stack = handler.data().stack;
            if (!stack.isEmpty()) {
                DrawerHelper.drawItemStack(g, stack, x + 1, y + 1, -1, null);
            }
            if (drawHoverOverlay && isMouseOverElement(mx, my) && getHoverElement(mx, my) == this) {
                RenderSystem.colorMask(true, true, true, false);
                DrawerHelper.drawSolidRect(g, x + 1, y + 1, 16, 16, 0x80FFFFFF);
                RenderSystem.colorMask(true, true, true, true);
            }
        }
        if (handler.data().kind == LabSlotKind.TAG) {
            g.drawString(Minecraft.getInstance().font, "#", getPositionX() + getSizeWidth() - 8,
                    getPositionY() + getSizeHeight() - 9, LabColors.TAG_GOLD);
        }
    }

    private int tintColor() {
        if (tint == LabSlotTint.ADDITIVE) {
            return LabColors.ADDITIVE_TINT;
        }
        if (tint == LabSlotTint.BLUEPRINT) {
            return LabColors.BLUEPRINT_TINT;
        }
        if (tint == LabSlotTint.MOLD) {
            return LabColors.MOLD_TINT;
        }
        return role == RecipeIngredientRole.INPUT ? LabColors.INPUT_TINT : LabColors.OUTPUT_TINT;
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

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (dragOwner != null && isMouseOverElement(mouseX, mouseY) && !handler.data().isEmpty()) {
            dragOwner.adjustStackCount(handler.data(), (int) Math.signum(wheelDelta));
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }
}
