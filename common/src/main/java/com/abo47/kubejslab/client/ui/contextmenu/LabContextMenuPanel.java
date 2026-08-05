package com.abo47.kubejslab.client.ui.contextmenu;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGlow;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class LabContextMenuPanel {
    private static final int CONTEXT_ROW_H = 12;
    private static final int OUTER_PAD = 4;
    private static final int TEXT_X = 8;
    private static final int TEXT_LINE_H = 9;
    private static final int MIN_WIDTH = 72;
    private static final int MAX_WIDTH = 140;
    private static final int TEXT_PAD = 8;

    private LabContextMenuPanel() {
    }

    public static WidgetGroup build(int x, int y, List<LabContextAction> actions, Runnable onActionDone) {
        int menuW = menuWidth(actions);
        int menuH = menuHeight(actions);
        WidgetGroup menu = new WidgetGroup(x, y, menuW, menuH);
        menu.setBackground(LabColors.bordered(LabColors.withAlpha(LabColors.SURFACE_BASE, 246), LabColors.BORDER_BASE));
        for (int i = 0; i < actions.size(); i++) {
            addRow(menu, actions.get(i), OUTER_PAD + i * CONTEXT_ROW_H, menuW, onActionDone);
        }
        return menu;
    }

    public static int menuHeight(List<LabContextAction> actions) {
        return OUTER_PAD * 2 + Math.max(1, actions.size()) * CONTEXT_ROW_H;
    }

    public static int menuWidth(List<LabContextAction> actions) {
        int widest = 0;
        for (LabContextAction action : actions) {
            widest = Math.max(widest, Minecraft.getInstance().font.width(action.label()));
        }
        return Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, widest + TEXT_X + TEXT_PAD));
    }

    private static void addRow(WidgetGroup menu, LabContextAction action, int rowY, int menuW, Runnable onActionDone) {
        WidgetGroup row = new WidgetGroup(0, rowY, menuW, CONTEXT_ROW_H) {
            private final ColorRectTexture rowBg = new ColorRectTexture(
                    LabColors.withAlpha(LabColors.SURFACE_PANEL_ALT, 84));
            private final TextTexture label = new TextTexture(action.label(), action.tone().accentColor())
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(menuW - TEXT_X);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                rowBg.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                label.draw(g, mx, my, getPositionX() + TEXT_X, getPositionY() + (getSizeHeight() - TEXT_LINE_H) / 2,
                        getSizeWidth() - TEXT_X, getSizeHeight());
            }
        };
        menu.addWidget(row);
        menu.addWidget(new ButtonWidget(0, rowY, menuW, CONTEXT_ROW_H, IGuiTexture.EMPTY,
                cd -> {
                    action.action().run();
                    onActionDone.run();
                })
                .setHoverTexture((g, mx, my, x, y, w, h) -> LabGlow.drawGlow(g, mx, my, (int) x, (int) y, w, h))
                .setClickedTexture(new ColorRectTexture(LabColors.pressedFill(LabColors.INTERACTIVE)))
                .setClientSideWidget());
    }
}
