package com.abo47.kubejslab.client.ui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;

public final class LabScreen extends Screen {
    private static final ColorRectTexture ROOT_FILL = new ColorRectTexture(LabColors.SURFACE_BASE);
    private static final ColorRectTexture PANEL_FILL = new ColorRectTexture(LabColors.SURFACE_PANEL);
    private static final ColorRectTexture BORDER = new ColorRectTexture(LabColors.BORDER_BASE);

    private final LabSplitter splitter = new LabSplitter();

    private LabScreen() {
        super(Component.literal("KubeJS Lab"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new LabScreen());
    }

    private int offsetX() {
        return (width - LabLayout.ROOT_W) / 2;
    }

    private int offsetY() {
        return (height - LabLayout.ROOT_H) / 2;
    }

    private static void drawBorderedRect(GuiGraphics graphics, int mouseX, int mouseY,
            int x, int y, int w, int h, ColorRectTexture fill) {
        fill.draw(graphics, mouseX, mouseY, x, y, w, h);
        BORDER.draw(graphics, mouseX, mouseY, x, y, w, 1);
        BORDER.draw(graphics, mouseX, mouseY, x, y + h - 1, w, 1);
        BORDER.draw(graphics, mouseX, mouseY, x, y, 1, h);
        BORDER.draw(graphics, mouseX, mouseY, x + w - 1, y, 1, h);
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);

        int ox = offsetX();
        int oy = offsetY();
        int rootW = LabLayout.ROOT_W;
        int rootH = LabLayout.ROOT_H;
        int bodyX = LabLayout.BODY_X;
        int bodyY = LabLayout.BODY_Y;
        int bodyH = LabLayout.BODY_H;
        int gap = LabLayout.GAP;

        drawBorderedRect(graphics, mouseX, mouseY, ox, oy, rootW, rootH, ROOT_FILL);

        int leftW = splitter.getLeftPanelWidth();
        int leftX = ox + bodyX;
        int leftY = oy + bodyY;

        drawBorderedRect(graphics, mouseX, mouseY, leftX, leftY, leftW, bodyH, PANEL_FILL);

        splitter.render(graphics, mouseX, mouseY, ox, oy);

        int rightX = ox + bodyX + leftW + gap;
        int rightY = oy + bodyY;
        int rightW = LabLayout.BODY_W - leftW - gap;

        drawBorderedRect(graphics, mouseX, mouseY, rightX, rightY, rightW, bodyH, PANEL_FILL);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (splitter.mouseClicked(mouseX, mouseY, offsetX(), offsetY())) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (splitter.mouseDragged(mouseX, mouseY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (splitter.mouseReleased(mouseX, mouseY)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
