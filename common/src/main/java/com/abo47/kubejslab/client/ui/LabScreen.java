package com.abo47.kubejslab.client.ui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class LabScreen {
    private static final ColorRectTexture ROOT_FILL = new ColorRectTexture(LabColors.SURFACE_BASE);
    private static final ColorRectTexture PANEL_FILL = new ColorRectTexture(LabColors.SURFACE_PANEL);
    private static final ColorRectTexture INNER_FILL = new ColorRectTexture(LabColors.SURFACE_BASE);
    private static final ColorRectTexture BORDER_TEX = new ColorRectTexture(LabColors.BORDER_BASE);

    private LabScreen() {
    }

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        LabRootWidget root = new LabRootWidget();
        root.setClientSideWidget();

        LabPanelWidget leftPanel = new LabPanelWidget(true);
        LabPanelWidget rightPanel = new LabPanelWidget(false);

        root.addWidget(leftPanel);
        root.addWidget(rightPanel);

        ModularUI ui = new ModularUI(root, IUIHolder.EMPTY, player);
        ui.initWidgets();
        mc.setScreen(new LabGuiContainer(ui, player.containerMenu.containerId));
    }

    public static final class LabRootWidget extends WidgetGroup {
        LabRootWidget() {
            super(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H);
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
            ROOT_FILL.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            BORDER_TEX.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), 1);
            BORDER_TEX.draw(g, mx, my, getPositionX(), getPositionY() + getSizeHeight() - 1, getSizeWidth(), 1);
            BORDER_TEX.draw(g, mx, my, getPositionX(), getPositionY(), 1, getSizeHeight());
            BORDER_TEX.draw(g, mx, my, getPositionX() + getSizeWidth() - 1, getPositionY(), 1, getSizeHeight());
            super.drawInBackground(g, mx, my, pt);
        }
    }

    public static final class LabPanelWidget extends WidgetGroup {
        private final boolean isLeft;
        private final LabTab[] tabs;

        LabPanelWidget(boolean isLeft) {
            super(
                    isLeft ? LabLayout.BODY_X : LabLayout.BODY_X + LabLayout.LEFT_PANEL_W + LabLayout.GAP,
                    LabLayout.BODY_Y,
                    isLeft ? LabLayout.LEFT_PANEL_W : LabLayout.BODY_W - LabLayout.LEFT_PANEL_W - LabLayout.GAP,
                    LabLayout.BODY_H);
            this.isLeft = isLeft;

            int tabInset = LabLayout.TAB_INSET;
            int tabH = LabLayout.TAB_H;
            int tabGap = LabLayout.TAB_GAP;

            String[] keys = isLeft
                    ? new String[]{"kubejslab.gui.lab_tab_built_in", "kubejslab.gui.lab_tab_custom"}
                    : new String[]{"", "", "", ""};
            this.tabs = new LabTab[keys.length];

            int tabCount = keys.length;
            int totalTabGap = tabGap * (tabCount - 1);
            int panelW = isLeft ? LabLayout.LEFT_PANEL_W : (LabLayout.BODY_W - LabLayout.LEFT_PANEL_W - LabLayout.GAP);
            int areaW = Math.max(1, panelW - tabInset * 2);
            int baseW = (areaW - totalTabGap) / tabCount;
            int remainder = (areaW - totalTabGap) % tabCount;
            int tabX = tabInset;

            for (int i = 0; i < keys.length; i++) {
                int w = baseW + (i < remainder ? 1 : 0);
                tabs[i] = new LabTab(tabX, LabLayout.PANEL_INSET, w, tabH, keys[i], i == 0);
                addWidget(tabs[i]);
                tabX += w + tabGap;
            }
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
            int px = getPositionX();
            int py = getPositionY();
            int pw = getSizeWidth();
            int ph = getSizeHeight();
            int panelInset = LabLayout.PANEL_INSET;
            int tabH = LabLayout.TAB_H;
            int innerTopY = py + panelInset + tabH;
            int innerH = ph - panelInset - tabH - panelInset;

            PANEL_FILL.draw(g, mx, my, px, py, pw, ph);

            int innerX = px + panelInset;
            int innerW = pw - panelInset * 2;
            INNER_FILL.draw(g, mx, my, innerX, innerTopY, innerW, innerH);
            BORDER_TEX.draw(g, mx, my, innerX, innerTopY, innerW, 1);
            BORDER_TEX.draw(g, mx, my, innerX, innerTopY + innerH - 1, innerW, 1);
            BORDER_TEX.draw(g, mx, my, innerX, innerTopY, 1, innerH);
            BORDER_TEX.draw(g, mx, my, innerX + innerW - 1, innerTopY, 1, innerH);

            BORDER_TEX.draw(g, mx, my, px, py, pw, 1);
            BORDER_TEX.draw(g, mx, my, px, py + ph - 1, pw, 1);
            BORDER_TEX.draw(g, mx, my, px, py, 1, ph);
            BORDER_TEX.draw(g, mx, my, px + pw - 1, py, 1, ph);

            super.drawInBackground(g, mx, my, pt);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

            for (int i = 0; i < tabs.length; i++) {
                if (tabs[i].isMouseOverElement(mouseX, mouseY)) {
                    selectTab(i);
                    return true;
                }
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        private void selectTab(int index) {
            if (tabs[index].isTabActive()) return;
            for (LabTab tab : tabs) tab.setTabActive(false);
            tabs[index].setTabActive(true);
        }
    }
}
