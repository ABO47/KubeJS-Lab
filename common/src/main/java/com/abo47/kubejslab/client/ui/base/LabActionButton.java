package com.abo47.kubejslab.client.ui.base;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;


public final class LabActionButton extends ButtonWidget {
    private final TextTexture label;

    public LabActionButton(int x, int y, int w, int h, String label, Runnable onClick) {
        super(x, y, w, h, IGuiTexture.EMPTY, cd -> onClick.run());
        setClientSideWidget();
        setBackground(LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE));
        setClickedTexture(LabColors.bordered(LabColors.pressedFill(LabColors.INTERACTIVE), LabColors.INTERACTIVE));
        this.label = new TextTexture(label, LabColors.TEXT_PRIMARY).setWidth(w).setType(TextTexture.TextType.NORMAL);
    }

    public void setLabel(String label) {
        this.label.updateText(label);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (isMouseOverElement(mouseX, mouseY)) {
            LabGlow.drawGlow(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        label.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }
}