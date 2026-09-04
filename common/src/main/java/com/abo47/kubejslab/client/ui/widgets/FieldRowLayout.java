package com.abo47.kubejslab.client.ui.widgets;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.theme.UiLayout;
import org.joml.Vector4f;


final class FieldRowLayout {
    static final int ROW_STRIDE = UiLayout.CARD_H + 4;
    static final int ROW_GAP = 4;

    static int rowY(int row) {
        return row * ROW_STRIDE;
    }

    static int contentHeight(int rowCount) {
        return rowCount * ROW_STRIDE;
    }

    static int viewportHeight(int widgetH) {
        return Math.max(1, widgetH - UiLayout.SETTINGS_PAD - UiLayout.SETTINGS_BTN_H - ROW_GAP);
    }

    static int maxScroll(int widgetH, int rowCount) {
        return Math.max(0, contentHeight(rowCount) - viewportHeight(widgetH));
    }

    static int knobHeight(int widgetH, int rowCount) {
        int viewport = viewportHeight(widgetH);
        int contentH = Math.max(1, contentHeight(rowCount));
        return Math.max(UiLayout.KNOB_MIN_H, viewport * viewport / contentH);
    }

    static int viewportBottom(int y, int widgetH) {
        return y + widgetH - UiLayout.SETTINGS_PAD - UiLayout.SETTINGS_BTN_H - ROW_GAP;
    }

    static int contentCardW(int panelW, int scrollMax) {
        int pad = UiLayout.SETTINGS_PAD;
        return panelW - pad * 2 - (scrollMax > 0 ? UiLayout.SCROLLBAR_W + 2 : 0);
    }

    static int controlX(int cardX, int cardW, int pad) {
        return cardX + cardW - pad - RowCardSettings.CONTROL_W - 4;
    }

    static boolean isInsideViewport(double mouseY, int y, int widgetH) {
        return mouseY >= y && mouseY <= viewportBottom(y, widgetH);
    }

    static boolean childInsideViewport(Widget child, int y, int widgetH) {
        int bottom = viewportBottom(y, widgetH);
        return child.getPositionY() < bottom && child.getPositionY() + child.getSizeHeight() > y;
    }

    static void drawCard(GuiGraphics g, int mx, int my, int cardX, int panelY, int cardW,
            int rowY, TextTexture label, int controlW, ItemStackTexture icon) {
        int cardY = panelY + rowY;
        RowCardSettings.CARD_TEXTURE.draw(g, mx, my, cardX, cardY, cardW, UiLayout.CARD_H);
        int pad = UiLayout.SETTINGS_PAD;
        int iconW = icon == null ? 0 : 16 + 4;
        int labelW = cardW - pad * 2 - controlW - 4 - iconW;
        if (icon != null) {
            icon.draw(g, mx, my, cardX + pad, cardY + (UiLayout.CARD_H - 16) / 2, 16, 16);
        }
        label.draw(g, mx, my, cardX + pad + iconW, cardY, labelW, UiLayout.CARD_H);
    }

    static void scissorRect(GuiGraphics g, int x1, int y1, int x2, int y2) {
        var trans = g.pose().last().pose();
        var realPos = new Vector4f(x1, y1, 0, 1);
        var realPos2 = new Vector4f(x2, y2, 0, 1);
        trans.transform(realPos);
        trans.transform(realPos2);
        g.enableScissor((int) realPos.x, (int) realPos.y, (int) realPos2.x, (int) realPos2.y);
    }

    private FieldRowLayout() {
    }
}
