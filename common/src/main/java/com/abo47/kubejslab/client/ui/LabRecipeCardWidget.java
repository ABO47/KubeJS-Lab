package com.abo47.kubejslab.client.ui;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class LabRecipeCardWidget extends Widget {
    private static final ColorRectTexture FILL = new ColorRectTexture(LabColors.SURFACE_PANEL_ALT);
    private static final ColorRectTexture BORDER_TEX = new ColorRectTexture(LabColors.BORDER_BASE);
    private static final int ICON_X = 4;
    private static final int ICON_SIZE = 16;
    private static final int TEXT_GAP = 4;
    private static final int TEXT_Y = 3;
    private static final int TEXT_LINE_H = 9;
    private static final int ID_Y = 14;
    private static final int ID_LINE_H = 8;

    private final ItemStackTexture iconTex;
    private final TextTexture nameTex;
    private final TextTexture idTex;
    private final int textW;
    private final int textX;
    private final Runnable onClick;

    public LabRecipeCardWidget(int x, int y, int w, int h, LabRecipeIndex.LabRecipeEntry entry, Runnable onClick) {
        super(x, y, w, h);
        this.onClick = onClick;
        this.iconTex = new ItemStackTexture(entry.output());
        this.textX = ICON_X + ICON_SIZE + TEXT_GAP;
        this.textW = w - textX;
        this.nameTex = new TextTexture(entry.name(), LabColors.TEXT_PRIMARY)
                .setWidth(textW)
                .setType(TextTexture.TextType.LEFT_HIDE);
        this.idTex = new TextTexture(entry.id().toString(), LabColors.TEXT_MUTED)
                .setWidth(textW)
                .setType(TextTexture.TextType.LEFT_HIDE);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();

        FILL.draw(g, mx, my, x, y, w, h);
        BORDER_TEX.draw(g, mx, my, x, y, w, 1);
        BORDER_TEX.draw(g, mx, my, x, y + h - 1, w, 1);
        BORDER_TEX.draw(g, mx, my, x, y, 1, h);
        BORDER_TEX.draw(g, mx, my, x + w - 1, y, 1, h);

        iconTex.draw(g, mx, my, x + ICON_X, y + (h - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE);
        nameTex.draw(g, mx, my, x + textX, y + TEXT_Y, textW, TEXT_LINE_H);
        idTex.draw(g, mx, my, x + textX, y + ID_Y, textW, ID_LINE_H);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == LabColors.MOUSE_BUTTON_LEFT && isMouseOverElement(mouseX, mouseY)) {
            if (onClick != null) {
                onClick.run();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}