package com.abo47.kubejslab.client.ui;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;

public final class LabSplitter {
    private static final ColorRectTexture FILL = new ColorRectTexture(LabColors.SURFACE_PANEL_ALT);
    private static final ColorRectTexture BORDER_TOP = new ColorRectTexture(LabColors.BORDER_BASE);
    private static final ColorRectTexture BORDER_BOTTOM = new ColorRectTexture(LabColors.BORDER_BASE);

    private int leftPanelWidth = LabLayout.LEFT_PANEL_W;
    private boolean dragging;
    private int dragStartX;
    private int dragStartWidth;

    public int getLeftPanelWidth() {
        return leftPanelWidth;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int rootX, int rootY) {
        int bodyX = LabLayout.BODY_X;
        int bodyY = LabLayout.BODY_Y;
        int bodyH = LabLayout.BODY_H;
        int splitterW = LabLayout.SPLITTER_W;

        int x = rootX + bodyX + leftPanelWidth;
        int y = rootY + bodyY;

        FILL.draw(graphics, mouseX, mouseY, x, y, splitterW, bodyH);
        BORDER_TOP.draw(graphics, mouseX, mouseY, x, y, splitterW, 1);
        BORDER_BOTTOM.draw(graphics, mouseX, mouseY, x, y + bodyH - 1, splitterW, 1);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int rootX, int rootY) {
        int bodyX = LabLayout.BODY_X;
        int bodyY = LabLayout.BODY_Y;
        int bodyH = LabLayout.BODY_H;
        int splitterW = LabLayout.SPLITTER_W;

        int x = rootX + bodyX + leftPanelWidth;
        int y = rootY + bodyY;

        if (mouseX >= x && mouseX < x + splitterW
                && mouseY >= y && mouseY < y + bodyH) {
            dragging = true;
            dragStartX = (int) Math.round(mouseX);
            dragStartWidth = leftPanelWidth;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (!dragging) {
            return false;
        }
        int dx = (int) Math.round(mouseX) - dragStartX;
        leftPanelWidth = Math.max(LabLayout.LEFT_PANEL_MIN,
                Math.min(LabLayout.LEFT_PANEL_MAX, dragStartWidth + dx));
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }
}
