package com.abo47.kubejslab.client.ui.widgets;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.abo47.kubejslab.client.ui.theme.IconAtlas;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiGlow;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;


public final class CarouselTabWidget extends Widget {
    private static final ColorRectTexture ACTIVE_FILL = new ColorRectTexture(UiColors.SURFACE_BASE);
    private static final ColorRectTexture BORDER_TEX = new ColorRectTexture(UiColors.BORDER_BASE);
    private static final int CHEVRON_SIZE = 12;
    private static final int CHEVRON_PAD = 4;
    private static final int CHEVRON_TEXT_GAP = 6;
    private static final float SIDE_SCALE = 0.80f;
    private static final int SIDE_ALPHA = 150;
    private static final int CENTER_COLOR = UiColors.TEXT_PRIMARY;
    private static final int SIDE_COLOR = UiColors.withAlpha(UiColors.TEXT_MUTED, SIDE_ALPHA);
    private static final int CHEVRON_COLOR = UiColors.TEXT_SECONDARY;
    private static final int CHEVRON_HOVER_COLOR = UiColors.TEXT_PRIMARY;

    private final String[] keys;
    private int selected;
    private Runnable onChanged;

    private ResourceTexture leftIcon;
    private ResourceTexture rightIcon;
    private ResourceTexture leftIconHover;
    private ResourceTexture rightIconHover;

    public CarouselTabWidget(int x, int y, int w, int h, String[] keys, int initialSelected) {
        super(x, y, w, h);
        setClientSideWidget();
        this.keys = keys.clone();
        this.selected = Math.max(0, Math.min(initialSelected, keys.length - 1));
        this.leftIcon = IconAtlas.iconTexture("back", CHEVRON_COLOR);
        this.rightIcon = IconAtlas.iconTexture("forward", CHEVRON_COLOR);
        this.leftIconHover = IconAtlas.iconTexture("back", CHEVRON_HOVER_COLOR);
        this.rightIconHover = IconAtlas.iconTexture("forward", CHEVRON_HOVER_COLOR);
        if (this.leftIcon == null || this.rightIcon == null) {
            this.leftIcon = IconAtlas.iconTexture("back", CHEVRON_COLOR);
            this.rightIcon = IconAtlas.iconTexture("forward", CHEVRON_COLOR);
        }
    }

    private void ensureIcons() {
        if (leftIcon == null) leftIcon = IconAtlas.iconTexture("back", CHEVRON_COLOR);
        if (rightIcon == null) rightIcon = IconAtlas.iconTexture("forward", CHEVRON_COLOR);
        if (leftIconHover == null) leftIconHover = IconAtlas.iconTexture("back", CHEVRON_HOVER_COLOR);
        if (rightIconHover == null) rightIconHover = IconAtlas.iconTexture("forward", CHEVRON_HOVER_COLOR);
    }

    public int getSelectedIndex() {
        return selected;
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= keys.length) return;
        if (this.selected == index) return;
        this.selected = index;
        if (onChanged != null) onChanged.run();
    }

    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public String[] getKeys() {
        return keys.clone();
    }

    private int prevIndex() {
        return (selected - 1 + keys.length) % keys.length;
    }

    private int nextIndex() {
        return (selected + 1) % keys.length;
    }

    private void selectPrev() {
        setSelectedIndex(prevIndex());
    }

    private void selectNext() {
        setSelectedIndex(nextIndex());
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        ensureIcons();
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();

        ACTIVE_FILL.draw(g, mx, my, x, y, w, h);
        BORDER_TEX.draw(g, mx, my, x, y, w, 1);
        BORDER_TEX.draw(g, mx, my, x, y, 1, h);
        BORDER_TEX.draw(g, mx, my, x + w - 1, y, 1, h);

        Font font = Minecraft.getInstance().font;

        int leftChevronX = x + CHEVRON_PAD;
        int chevronY = y + (h - CHEVRON_SIZE) / 2;
        int rightChevronX = x + w - CHEVRON_PAD - CHEVRON_SIZE;

        boolean hoverLeft = isHoveringLeftChevron(mx, my);
        boolean hoverRight = isHoveringRightChevron(mx, my);
        boolean hoverPrev = isHoveringPrevText(mx, my);
        boolean hoverNext = isHoveringNextText(mx, my);

        ResourceTexture leftTex = (hoverLeft || hoverPrev) ? leftIconHover : leftIcon;
        ResourceTexture rightTex = (hoverRight || hoverNext) ? rightIconHover : rightIcon;

        if (leftTex != null) {
            leftTex.draw(g, mx, my, leftChevronX, chevronY, CHEVRON_SIZE, CHEVRON_SIZE);
        }
        if (rightTex != null) {
            rightTex.draw(g, mx, my, rightChevronX, chevronY, CHEVRON_SIZE, CHEVRON_SIZE);
        }

        if ((hoverLeft || hoverPrev) && isMouseOverElement(mx, my)) {
            int glowX = leftChevronX - 2;
            int glowY = chevronY - 2;
            int glowW = CHEVRON_SIZE + 4;
            int glowH = CHEVRON_SIZE + 4;
            UiGlow.drawGlow(g, mx, my, glowX, glowY, glowW, glowH);
        }
        if ((hoverRight || hoverNext) && isMouseOverElement(mx, my)) {
            int glowX = rightChevronX - 2;
            int glowY = chevronY - 2;
            int glowW = CHEVRON_SIZE + 4;
            int glowH = CHEVRON_SIZE + 4;
            UiGlow.drawGlow(g, mx, my, glowX, glowY, glowW, glowH);
        }

        Component centerComp = Component.translatable(keys[selected]);
        int centerW = font.width(centerComp);
        int centerX = x + w / 2 - centerW / 2;
        int centerY = y + (h - 8) / 2;
        g.drawString(font, centerComp, centerX, centerY, CENTER_COLOR, false);

        if (keys.length > 1) {
            Component prevComp = Component.translatable(keys[prevIndex()]);
            int prevW = font.width(prevComp);
            int scaledPrevW = Math.round(prevW * SIDE_SCALE);
            int prevX = leftChevronX + CHEVRON_SIZE + CHEVRON_TEXT_GAP;
            int prevY = y + (h - Math.round(8 * SIDE_SCALE)) / 2;
            prevX = Math.min(prevX, x + w / 2 - centerW / 2 - scaledPrevW - 10);
            drawScaledText(g, prevComp, prevX, prevY, hoverPrev || hoverLeft ? UiColors.TEXT_SECONDARY : SIDE_COLOR, SIDE_SCALE);

            Component nextComp = Component.translatable(keys[nextIndex()]);
            int nextW = font.width(nextComp);
            int scaledNextW = Math.round(nextW * SIDE_SCALE);
            int nextX = rightChevronX - CHEVRON_TEXT_GAP - scaledNextW;
            int nextY = prevY;
            nextX = Math.max(nextX, x + w / 2 + centerW / 2 + 10);
            drawScaledText(g, nextComp, nextX, nextY, hoverNext || hoverRight ? UiColors.TEXT_SECONDARY : SIDE_COLOR, SIDE_SCALE);
        }
    }

    private void drawScaledText(GuiGraphics g, Component text, int x, int y, int color, float scale) {
        Font font = Minecraft.getInstance().font;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private boolean isHoveringLeftChevron(int mx, int my) {
        int x = getPositionX();
        int y = getPositionY();
        int h = getSizeHeight();
        int lx = x + CHEVRON_PAD;
        int ly = y + (h - CHEVRON_SIZE) / 2;
        return mx >= lx - 2 && mx < lx + CHEVRON_SIZE + 2 && my >= ly - 2 && my < ly + CHEVRON_SIZE + 2;
    }

    private boolean isHoveringRightChevron(int mx, int my) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        int rx = x + w - CHEVRON_PAD - CHEVRON_SIZE;
        int ry = y + (h - CHEVRON_SIZE) / 2;
        return mx >= rx - 2 && mx < rx + CHEVRON_SIZE + 2 && my >= ry - 2 && my < ry + CHEVRON_SIZE + 2;
    }

    private boolean isHoveringPrevText(int mx, int my) {
        if (!isMouseOverElement(mx, my) || keys.length <= 1) return false;
        Font font = Minecraft.getInstance().font;
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        int leftChevronX = x + CHEVRON_PAD;
        int centerW = font.width(Component.translatable(keys[selected]));
        Component prevComp = Component.translatable(keys[prevIndex()]);
        int prevW = font.width(prevComp);
        int scaledPrevW = Math.round(prevW * SIDE_SCALE);
        int prevX = leftChevronX + CHEVRON_SIZE + CHEVRON_TEXT_GAP;
        prevX = Math.min(prevX, x + w / 2 - centerW / 2 - scaledPrevW - 10);
        int prevY = y + (h - Math.round(8 * SIDE_SCALE)) / 2;
        int scaledH = Math.round(8 * SIDE_SCALE);
        return mx >= prevX && mx < prevX + scaledPrevW && my >= prevY && my < prevY + scaledH;
    }

    private boolean isHoveringNextText(int mx, int my) {
        if (!isMouseOverElement(mx, my) || keys.length <= 1) return false;
        Font font = Minecraft.getInstance().font;
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        int rightChevronX = x + w - CHEVRON_PAD - CHEVRON_SIZE;
        int centerW = font.width(Component.translatable(keys[selected]));
        Component nextComp = Component.translatable(keys[nextIndex()]);
        int nextW = font.width(nextComp);
        int scaledNextW = Math.round(nextW * SIDE_SCALE);
        int nextX = rightChevronX - CHEVRON_TEXT_GAP - scaledNextW;
        nextX = Math.max(nextX, x + w / 2 + centerW / 2 + 10);
        int nextY = y + (h - Math.round(8 * SIDE_SCALE)) / 2;
        int scaledH = Math.round(8 * SIDE_SCALE);
        return mx >= nextX && mx < nextX + scaledNextW && my >= nextY && my < nextY + scaledH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != UiColors.MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);
        if (!isMouseOverElement(mouseX, mouseY)) return false;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (isHoveringLeftChevron(mx, my) || isHoveringPrevText(mx, my)) {
            selectPrev();
            return true;
        }
        if (isHoveringRightChevron(mx, my) || isHoveringNextText(mx, my)) {
            selectNext();
            return true;
        }
        int x = getPositionX();
        int w = getSizeWidth();
        int centerL = x + w / 2 - 30;
        int centerR = x + w / 2 + 30;
        if (mouseX < centerL) {
            selectPrev();
            return true;
        }
        if (mouseX > centerR) {
            selectNext();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) return false;
        if (wheelDelta > 0) {
            selectPrev();
        } else if (wheelDelta < 0) {
            selectNext();
        } else {
            return false;
        }
        return true;
    }
}
