package com.abo47.kubejslab.client.ui.base;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;


public final class LabModalHeader {
    public static final int BUTTON_SIZE = 16;
    public static final int BUTTON_Y = 3;
    public static final int GAP = 3;
    public static final int EDGE = 8;
    public static final int TITLE_Y = 6;
    public static final int TITLE_H = 9;

    private LabModalHeader() {
    }

    public static int closeX(int modalW) {
        return modalW - EDGE - BUTTON_SIZE;
    }

    public static int contentW(int modalW, int left) {
        return closeX(modalW) - GAP - left;
    }

    public static int beforeX(int x) {
        return x - BUTTON_SIZE - GAP;
    }

    public static ButtonWidget closeButton(int x, Runnable onClose) {
        ResourceTexture icon = LabIconAtlas.iconTexture("close", LabColors.ERROR);
        IGuiTexture face = new IGuiTexture() {
            @Override
            public void draw(GuiGraphics g, int mx, int my, float x0, float y0, int w0, int h0) {
                LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE)
                        .draw(g, mx, my, x0, y0, w0, h0);
                icon.draw(g, mx, my, x0 + 2, y0 + 2, w0 - 4, h0 - 4);
            }
        };
        ButtonWidget button = new ButtonWidget(x, BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE, face, cd -> onClose.run());
        button.setClientSideWidget();
        button.setHoverTexture((g, mx, my, x0, y0, w0, h0) ->
                LabGlow.drawGlow(g, mx, my, (int) x0, (int) y0, (int) w0, (int) h0));
        return button;
    }

    public static WidgetGroup titleLabel(String title, int left, int width) {
        return new WidgetGroup(left, TITLE_Y, width, TITLE_H) {
            private final TextTexture tex = new TextTexture(title, LabColors.TEXT_PRIMARY)
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(width);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                tex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        };
    }
}
