package com.abo47.kubejslab.client.ui.widgets;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiGlow;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;


public final class ActionButton extends ButtonWidget {
    private final TextTexture label;

    public ActionButton(int x, int y, int w, int h, String label, Runnable onClick) {
        super(x, y, w, h, IGuiTexture.EMPTY, cd -> onClick.run());
        setClientSideWidget();
        setBackground(UiColors.bordered(UiColors.SURFACE_PANEL_ALT, UiColors.BORDER_BASE));
        setClickedTexture(UiColors.bordered(UiColors.pressedFill(UiColors.INTERACTIVE), UiColors.INTERACTIVE));
        this.label = new TextTexture(label, UiColors.TEXT_PRIMARY).setWidth(w).setType(TextTexture.TextType.ROLL);
    }

    public void setLabel(String label) {
        this.label.updateText(label);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (isMouseOverElement(mouseX, mouseY)) {
            UiGlow.drawGlow(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        label.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }
}