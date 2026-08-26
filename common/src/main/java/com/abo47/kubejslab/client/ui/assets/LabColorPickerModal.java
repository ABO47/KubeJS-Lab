package com.abo47.kubejslab.client.ui.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.HsbColorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabScrollBarWidget;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuAnimation;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuPanel;


public final class LabColorPickerModal {
    private static final int MODAL_W = 260;
    private static final int MODAL_H = 200;
    private static final int BODY_Y = 22;
    private static final int BODY_BOTTOM_PAD = 12;
    private static final int LEFT_W = 112;
    private static final int RIGHT_X = LEFT_W + 16;
    private static final int CELL = 16;
    private static final int CELL_GAP = 2;
    private static final int CELL_PAD = 4;
    private static final long DOUBLE_CLICK_MS = 350;

    private static final int DIM_COLOR = LabColors.withAlpha(LabColors.SURFACE_BASE, 140);
    private static final int PANEL_FILL = LabColors.withAlpha(LabColors.SURFACE_BASE, 252);
    private static final IGuiTexture PANEL_TEXTURE =
            LabColors.bordered(PANEL_FILL, LabColors.BORDER_ACCENT);
    private static final IGuiTexture PANEL_INNER_TEXTURE =
            LabColors.bordered(LabColors.withAlpha(LabColors.SURFACE_PANEL_ALT, 120), LabColors.BORDER_BASE);
    private static final ColorRectTexture SHADOW_DEEP =
            new ColorRectTexture(LabColors.withAlpha(LabColors.SURFACE_BASE, 82));
    private static final ColorRectTexture SHADOW_NEAR =
            new ColorRectTexture(LabColors.withAlpha(LabColors.SURFACE_BASE, 120));

    private static final List<Integer> PALETTE = new ArrayList<>();

    private final WidgetGroup layer;
    private final WidgetGroup panel;
    private final WidgetGroup surface;
    private final HsbColorWidget picker;
    private final TextFieldWidget hexField;
    private final Consumer<String> onApply;
    private final State state = new State();
    private int draftColor;    private static final class State {
        String hexDraft = "";
        int scroll;
        int maxStart;
        int knobH;
        boolean dragging;
        long lastSwatchMs;
        int lastSwatchColor = Integer.MIN_VALUE;
        long contextMenuMs;
    }

    private WidgetGroup contextMenu;
    private ButtonWidget contextDismiss;
    private int contextMenuX;
    private int contextMenuY;
    private int contextValue;

    private LabColorPickerModal(WidgetGroup layer, String title, String initialHex, Consumer<String> onApply) {
        this.layer = layer;
        this.onApply = onApply;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H,
                new ColorRectTexture(DIM_COLOR), cd -> close()).setClientSideWidget());

        int panelX = (LabLayout.ROOT_W - MODAL_W) / 2;
        int panelY = (LabLayout.ROOT_H - MODAL_H) / 2;
        this.panel = new WidgetGroup(panelX, panelY, MODAL_W, MODAL_H) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
            }

            @Override
            public void drawInForeground(GuiGraphics g, int mx, int my, float pt) {
                int x = getPositionX();
                int y = getPositionY();
                SHADOW_DEEP.draw(g, mx, my, x + 5, y + 5, MODAL_W, MODAL_H);
                SHADOW_NEAR.draw(g, mx, my, x + 3, y + 3, MODAL_W, MODAL_H);
                PANEL_TEXTURE.draw(g, mx, my, x, y, MODAL_W, MODAL_H);
                g.pose().pushPose();
                g.pose().translate(0, 0, 300);
                for (Widget child : widgets) {
                    child.drawInBackground(g, mx, my, pt);
                    child.drawInForeground(g, mx, my, pt);
                }
                g.pose().popPose();
            }

            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                super.mouseClicked(mx, my, button);
                return isMouseOverElement(mx, my);
            }
        };
        layer.addWidget(panel);

        panel.addWidget(new WidgetGroup(8, 6, MODAL_W - 50, 9) {
            private final com.lowdragmc.lowdraglib.gui.texture.TextTexture titleTex =
                    new com.lowdragmc.lowdraglib.gui.texture.TextTexture(title, LabColors.TEXT_PRIMARY)
                            .setType(com.lowdragmc.lowdraglib.gui.texture.TextTexture.TextType.LEFT_HIDE)
                            .setWidth(MODAL_W - 50);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                titleTex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        });
        addHeaderButton(MODAL_W - 25, 3, "close", cd -> close());

        int bodyH = MODAL_H - BODY_Y - BODY_BOTTOM_PAD;
        int wheelSize = Math.min(LEFT_W - 16, bodyH - 52);

        WidgetGroup left = new WidgetGroup(8, BODY_Y, LEFT_W, bodyH) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                PANEL_INNER_TEXTURE.draw(g, mx, my, getPositionX(), getPositionY(),
                        getSizeWidth(), getSizeHeight());
            }
        };
        panel.addWidget(left);

        int initialColor = parseHexOrDefault(initialHex, 0xFFFFFFFF);
        this.draftColor = initialColor;
        this.picker = new HsbColorWidget(8, 6, wheelSize, wheelSize)
                .setShowAlpha(false)
                .setColor(initialColor)
                .setOnChanged(color -> {
                    draftColor = color;
                    state.hexDraft = toHex(color);
                });
        left.addWidget(picker);
        state.hexDraft = toHex(draftColor);

        this.hexField = buildHexField(8, wheelSize + 14, LEFT_W - 16, 14);
        left.addWidget(hexField);

        LabActionButton useButton = new LabActionButton(8, bodyH - 20, LEFT_W - 16,
                LabLayout.SETTINGS_BTN_H, I18n.get(LabGuiKeys.LAB_ASSETS_USE), () -> {
            onApply.accept(toHex(draftColor));
            close();
        });
        left.addWidget(useButton);

        WidgetGroup right = new WidgetGroup(RIGHT_X, BODY_Y, MODAL_W - RIGHT_X - 8, bodyH) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                PANEL_INNER_TEXTURE.draw(g, mx, my, getPositionX(), getPositionY(),
                        getSizeWidth(), getSizeHeight());
            }
        };
        panel.addWidget(right);

        this.surface = new WidgetGroup(CELL_PAD, 4, MODAL_W - RIGHT_X - 8 - CELL_PAD * 2, bodyH - 28) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                int x = getPositionX();
                int y = getPositionY();
                int w = getSizeWidth();
                int h = getSizeHeight();
                g.flush();
                g.enableScissor(x, y, x + w, y + h);
                for (Widget child : widgets) {
                    int cy = child.getPositionY();
                    if (cy + child.getSizeHeight() < y || cy > y + h) {
                        continue;
                    }
                    child.drawInBackground(g, mx, my, pt);
                }
                g.flush();
                g.disableScissor();
            }
        };
        right.addWidget(surface);

        LabActionButton saveButton = new LabActionButton(CELL_PAD, bodyH - 20, MODAL_W - RIGHT_X - 8 - CELL_PAD * 2,
                LabLayout.SETTINGS_BTN_H, I18n.get(LabGuiKeys.LAB_COLOR_SAVE_PALETTE), () -> {
            int rgb = draftColor & 0xFFFFFF;
            if (!PALETTE.contains(rgb)) {
                PALETTE.add(rgb);
            }
            refresh();
        });
        right.addWidget(saveButton);

        refresh();
    }

    private TextFieldWidget buildHexField(int x, int y, int w, int h) {
        LabCommitFieldWidget field = new LabCommitFieldWidget(x, y, w, h,
                () -> state.hexDraft,
                value -> {
                    if (value == null) {
                        return;
                    }
                    int parsed = parseHexOrDefault(value, draftColor);
                    picker.setColor(parsed);
                    draftColor = parsed;
                    state.hexDraft = toHex(parsed);
                });
        field.setClientSideWidget();
        field.setMaxStringLength(9);
        field.setBordered(false);
        field.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        field.setTextColor(LabColors.TEXT_PRIMARY);
        field.setCurrentString(state.hexDraft);
        return field;
    }

    private void refresh() {
        closeContext();
        int areaW = surface.getSizeWidth();
        int areaH = surface.getSizeHeight();
        TileGridLayout layout = TileGridLayout.calculate(
                areaW - LabScrollBarWidget.RESERVED_WIDTH, areaH,
                CELL, CELL, CELL_GAP, 0, 0,
                PALETTE.size(), state.scroll);
        state.maxStart = layout.maxStart();
        state.knobH = layout.knobH();

        surface.clearAllWidgets();
        for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
            int vi = i - layout.scrollStart();
            int color = PALETTE.get(i);
            int px = layout.tileX(vi);
            int py = layout.tileY(vi);
            surface.addWidget(buildSwatch(color, px, py));
        }
        surface.addWidget(new LabScrollBarWidget(
                areaW - LabScrollBarWidget.RESERVED_WIDTH, 0,
                LabScrollBarWidget.RESERVED_WIDTH, areaH,
                () -> state.scroll,
                () -> state.maxStart,
                () -> state.knobH,
                value -> {
                    state.scroll = value;
                    refresh();
                },
                () -> state.dragging,
                value -> state.dragging = value,
                this::refresh));
    }

    private ButtonWidget buildSwatch(int color, int px, int py) {
        ButtonWidget hit = new ButtonWidget(px, py, CELL, CELL,
                LabColors.bordered(color | 0xFF000000, LabColors.BORDER_BASE), click -> onSwatchClick(click, color));
        hit.setClientSideWidget();
        hit.setHoverTexture((g, mx, my, x0, y0, w0, h0) ->
                LabGlow.drawGlow(g, (int) mx, (int) my, (int) x0, (int) y0, (int) w0, (int) h0));
        return hit;
    }

    private void onSwatchClick(ClickData click, int color) {
        if (click.button == LabColors.MOUSE_BUTTON_RIGHT) {
            openContext(color);
            return;
        }
        if (click.button != LabColors.MOUSE_BUTTON_LEFT) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean doubleClick = state.lastSwatchColor == color && now - state.lastSwatchMs < DOUBLE_CLICK_MS;
        state.lastSwatchMs = now;
        state.lastSwatchColor = color;
        picker.setColor(color);
        draftColor = color;
        state.hexDraft = toHex(color);
        hexField.setCurrentString(state.hexDraft);
        if (doubleClick) {
            onApply.accept(state.hexDraft);
            close();
        }
    }

    private void openContext(int color) {
        this.contextValue = color;
        List<LabContextAction> actions = List.of(
                new LabContextAction(I18n.get(LabGuiKeys.LAB_ASSETS_USE), "icon", LabActionTone.PRIMARY,
                        () -> {
                            onApply.accept(toHex(contextValue));
                            close();
                        }),
                new LabContextAction(I18n.get(LabGuiKeys.LAB_ASSETS_DELETE), "delete", LabActionTone.DANGER,
                        () -> {
                            PALETTE.removeIf(value -> value == contextValue);
                            refresh();
                        }));
        int menuW = LabContextMenuPanel.menuWidth(actions);
        int menuH = LabContextMenuPanel.menuHeight(actions);
        contextMenuX = Math.max(4, Math.min(CELL_PAD + CELL, panelW() - menuW - 4));
        contextMenuY = Math.max(4, Math.min(BODY_Y + 4, panelH() - menuH - 4));
        contextDismiss = new ButtonWidget(0, 0, panelW(), panelH(), IGuiTexture.EMPTY,
                cd -> closeContext());
        contextDismiss.setClientSideWidget();
        panel.addWidget(contextDismiss);
        state.contextMenuMs = System.currentTimeMillis();
        contextMenu = LabContextMenuAnimation.wrap(
                LabContextMenuPanel.build(contextMenuX, contextMenuY, actions, this::closeContext),
                () -> state.contextMenuMs);
        panel.addWidget(contextMenu);
    }

    private void closeContext() {
        if (contextDismiss != null) {
            panel.removeWidget(contextDismiss);
            contextDismiss = null;
        }
        if (contextMenu != null) {
            panel.removeWidget(contextMenu);
            contextMenu = null;
        }
    }

    private void addHeaderButton(int x, int y, String iconKey, java.util.function.Consumer<ClickData> onClick) {
        ButtonWidget button = new ButtonWidget(x, y, 16, 16,
                LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE), onClick);
        button.setClientSideWidget();
        panel.addWidget(button);
    }

    private int panelW() {
        return MODAL_W;
    }

    private int panelH() {
        return MODAL_H;
    }

    private void close() {
        layer.setVisible(false);
    }

    private static String toHex(int argb) {
        return String.format("%06X", argb & 0xFFFFFF);
    }

    private static int parseHexOrDefault(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String cleaned = value.trim().replace("#", "").replace("0x", "").replace("0X", "");
        if (cleaned.isEmpty()) {
            return fallback;
        }
        try {
            return (int) (Long.parseLong(cleaned, 16) & 0xFFFFFF) | 0xFF000000;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void open(WidgetGroup layer, String title, String initialHex, Consumer<String> onApply) {
        new LabColorPickerModal(layer, title, initialHex, onApply);
    }
}
