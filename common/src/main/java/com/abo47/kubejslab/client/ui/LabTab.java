package com.abo47.kubejslab.client.ui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class LabTab extends Widget {
    private static final ColorRectTexture ACTIVE_FILL = new ColorRectTexture(LabColors.SURFACE_BASE);
    private static final ColorRectTexture INACTIVE_FILL = new ColorRectTexture(
            LabColors.withAlpha(LabColors.SURFACE_PANEL_ALT, 142));
    private static final ColorRectTexture BORDER_TEX = new ColorRectTexture(LabColors.BORDER_BASE);

    private final String label;
    private boolean active;

    public LabTab(int x, int y, int w, int h, String translationKey, boolean active) {
        super(x, y, w, h);
        this.label = translationKey;
        this.active = active;
    }

    public boolean isTabActive() { return active; }

    public void setTabActive(boolean active) { this.active = active; }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();

        if (active) {
            ACTIVE_FILL.draw(g, mx, my, x, y, w, h + 1);
            BORDER_TEX.draw(g, mx, my, x, y, w, 1);
            BORDER_TEX.draw(g, mx, my, x, y, 1, h + 1);
            BORDER_TEX.draw(g, mx, my, x + w - 1, y, 1, h + 1);
        } else {
            INACTIVE_FILL.draw(g, mx, my, x, y, w, h);
            BORDER_TEX.draw(g, mx, my, x, y, w, 1);
            BORDER_TEX.draw(g, mx, my, x, y, 1, h);
            BORDER_TEX.draw(g, mx, my, x + w - 1, y, 1, h);
        }

        if (!label.isEmpty()) {
            int textColor = active ? LabColors.TEXT_PRIMARY : LabColors.TEXT_MUTED;
            String text = net.minecraft.network.chat.Component.translatable(label).getString();
            var font = Minecraft.getInstance().font;
            int textW = font.width(text);
            int maxTextW = w - 8;
            if (textW > maxTextW) {
                StringBuilder sb = new StringBuilder();
                int running = 0;
                for (int i = 0; i < text.length(); i++) {
                    int cw = font.width(String.valueOf(text.charAt(i)));
                    if (running + cw > maxTextW - font.width("...")) break;
                    sb.append(text.charAt(i));
                    running += cw;
                }
                text = sb.toString() + "...";
                textW = font.width(text);
            }
            int textX = x + (w - textW) / 2;
            int textY = y + (h - 8) / 2;
            g.drawString(font, text, textX, textY, textColor, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
