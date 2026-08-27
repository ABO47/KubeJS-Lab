package com.abo47.kubejslab.client.ui.assets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.HsbColorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabScrollBarWidget;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuAnimation;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuPanel;
import com.abo47.kubejslab.lab.LabPathResolver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public final class LabColorPickerModal {
    public static final int MODAL_W = 432;
    public static final int MODAL_H = 260;
    private static final int PREVIEW_X = 8;
    private static final int LEFT_W = 150;
    private static final int RIGHT_X = 166;
    private static final int BODY_Y = 22;
    private static final int BODY_BOTTOM_PAD = 26;
    private static final int PALETTE_TOP = 4;
    private static final int CELL = 16;
    private static final int CELL_GAP = 2;
    private static final int CELL_PAD = 4;
    private static final long DOUBLE_CLICK_MS = 350;

    private static final int DIM_COLOR = LabColors.withAlpha(LabColors.SURFACE_BASE, 140);
    private static final int ELEVATED_FILL = mix(LabColors.SURFACE_PANEL_ALT, LabColors.TEXT_PRIMARY, 10);
    private static final int SUBTLE_BORDER = mix(LabColors.BORDER_BASE, LabColors.SURFACE_BASE, 28);

    private static final List<Integer> PALETTE = loadPalette();

    private final WidgetGroup layer;
    private final WidgetGroup panel;
    private final WidgetGroup palettePanel;
    private final HsbColorWidget picker;
    private final TextFieldWidget hexField;
    private final TextTexture emptyTex;
    private final Consumer<String> onApply;
    private final State state = new State();
    private int draftColor;

    private WidgetGroup contextMenu;
    private ButtonWidget contextDismiss;
    private int contextMenuX;
    private int contextMenuY;
    private int contextValue;

    private static final class State {
        String hexDraft = "";
        int scroll;
        long lastSwatchMs;
        int lastSwatchColor = Integer.MIN_VALUE;
        long contextMenuMs;
    }

    private LabColorPickerModal(WidgetGroup layer, String title, String initialHex, Consumer<String> onApply) {
        this.layer = layer;
        this.onApply = onApply;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H,
                new ColorRectTexture(DIM_COLOR), cd -> close()).setClientSideWidget());

        int bodyH = MODAL_H - BODY_Y - BODY_BOTTOM_PAD;
        int wheelSize = Math.min(LEFT_W - 20, bodyH - 84);
        int initialColor = parseHexOrDefault(initialHex, 0xFFFFFFFF);
        this.draftColor = initialColor;

        this.panel = new WidgetGroup((LabLayout.ROOT_W - MODAL_W) / 2,
                (LabLayout.ROOT_H - MODAL_H) / 2, MODAL_W, MODAL_H) {
            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                super.mouseClicked(mx, my, button);
                return isMouseOverElement(mx, my);
            }
        };
        this.panel.setBackground(LabColors.bordered(
                LabColors.withAlpha(LabColors.SURFACE_BASE, 252), LabColors.BORDER_ACCENT));
        layer.addWidget(panel);
        panel.addWidget(titleLabel(title));
        addHeaderClose(MODAL_W - 25, 3);

        WidgetGroup left = new WidgetGroup(PREVIEW_X, BODY_Y, LEFT_W, bodyH);
        left.setBackground(LabColors.bordered(
                LabColors.withAlpha(LabColors.SURFACE_PANEL_ALT, 120), LabColors.BORDER_BASE));
        panel.addWidget(left);

        this.picker = new HsbColorWidget(8, 8, wheelSize, wheelSize)
                .setShowAlpha(false)
                .setColor(initialColor);
        left.addWidget(picker);

        this.hexField = buildHexField(8, wheelSize + 16, LEFT_W - 16, 12);
        left.addWidget(hexField);
        state.hexDraft = toHex(draftColor);
        hexField.setCurrentString(state.hexDraft);
        picker.setOnChanged(color -> {
            draftColor = color;
            state.hexDraft = toHex(color);
            hexField.setCurrentString(state.hexDraft);
        });

        left.addWidget(new LabActionButton(8, bodyH - 20, LEFT_W - 16, 16,
                I18n.get(LabGuiKeys.LAB_ASSETS_USE), () -> {
            onApply.accept(toHex(draftColor));
            close();
        }));

        WidgetGroup right = new WidgetGroup(RIGHT_X, BODY_Y, MODAL_W - RIGHT_X - PREVIEW_X, bodyH);
        right.setBackground(LabColors.bordered(LabColors.withAlpha(ELEVATED_FILL, 190), SUBTLE_BORDER));
        panel.addWidget(right);

        this.emptyTex = new TextTexture(I18n.get(LabGuiKeys.LAB_ASSETS_NO_ASSETS), LabColors.TEXT_MUTED)
                .setType(TextTexture.TextType.NORMAL);

        this.palettePanel = new WidgetGroup(0, 0, MODAL_W - RIGHT_X - PREVIEW_X, bodyH - 28);
        right.addWidget(palettePanel);

        right.addWidget(new LabActionButton(4, bodyH - 20, MODAL_W - RIGHT_X - PREVIEW_X - 8, 16,
                I18n.get(LabGuiKeys.LAB_COLOR_SAVE_PALETTE), () -> {
            int rgb = draftColor & 0xFFFFFF;
            if (!PALETTE.contains(rgb)) {
                PALETTE.add(rgb);
                savePalette();
            }
            rebuildPalette();
        }));
        rebuildPalette();
    }

    private WidgetGroup titleLabel(String title) {
        return new WidgetGroup(8, 6, MODAL_W - 50, 9) {
            private final TextTexture tex = new TextTexture(title, LabColors.TEXT_PRIMARY)
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(MODAL_W - 50);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                tex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        };
    }

    private void rebuildPalette() {
        closeContext();
        palettePanel.clearAllWidgets();

        if (PALETTE.isEmpty()) {
            palettePanel.addWidget(new WidgetGroup(12, PALETTE_TOP + 8, 120, 9) {
                @Override
                public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                    emptyTex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
            });
            return;
        }

        int areaW = MODAL_W - RIGHT_X - PREVIEW_X - 8;
        int areaH = palettePanel.getSizeHeight();
        TileGridLayout layout = TileGridLayout.calculate(
                areaW, areaH, CELL, CELL, CELL_GAP, CELL_PAD, CELL_PAD,
                PALETTE.size(), state.scroll);
        for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
            int vi = i - layout.scrollStart();
            int color = PALETTE.get(i);
            int px = CELL_PAD + layout.tileX(vi);
            int py = PALETTE_TOP + layout.tileY(vi);
            palettePanel.addWidget(buildSwatch(color, px, py));        }
        if (layout.showScroll()) {
            palettePanel.addWidget(new LabScrollBarWidget(
                    CELL_PAD + layout.scrollBarX() + 1, PALETTE_TOP + layout.scrollBarY(),
                    LabScrollBarWidget.RESERVED_WIDTH, layout.scrollBarH(),
                    () -> state.scroll,
                    () -> Math.max(0, layout.maxStart()),
                    layout::knobH,
                    value -> {
                        state.scroll = value;
                        rebuildPalette();
                    },
                    () -> false,
                    value -> {
                    },
                    this::rebuildPalette));
        }
    }

    private ButtonWidget buildSwatch(int color, int px, int py) {
        ButtonWidget hit = new ButtonWidget(px, py, CELL, CELL,
                LabColors.bordered(color | 0xFF000000, LabColors.BORDER_BASE),
                click -> onSwatchClick(click, color));
        hit.setClientSideWidget();
        hit.setHoverBorderTexture(1, LabColors.BORDER_ACCENT);
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
                            if (PALETTE.removeIf(value -> value == contextValue)) {
                                savePalette();
                            }
                            rebuildPalette();
                        }));
        int menuW = LabContextMenuPanel.menuWidth(actions);
        int menuH = LabContextMenuPanel.menuHeight(actions);
        Minecraft mc = Minecraft.getInstance();
        int cursorX = (int) Math.round(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth()
                / (double) mc.getWindow().getScreenWidth());
        int cursorY = (int) Math.round(mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight()
                / (double) mc.getWindow().getScreenHeight());
        contextMenuX = clamp(cursorX - panel.getPositionX(), 4, MODAL_W - menuW - 4);
        contextMenuY = clamp(cursorY - panel.getPositionY(), 4, MODAL_H - menuH - 4);
        contextDismiss = new ButtonWidget(0, 0, MODAL_W, MODAL_H, IGuiTexture.EMPTY,
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

    private void addHeaderClose(int x, int y) {
        com.lowdragmc.lowdraglib.gui.texture.ResourceTexture icon =
                com.abo47.kubejslab.client.ui.base.LabIconAtlas.iconTexture("close", LabColors.ERROR);
        IGuiTexture face = new IGuiTexture() {
            @Override
            public void draw(GuiGraphics g, int mx, int my, float x0, float y0, int w0, int h0) {
                LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE)
                        .draw(g, mx, my, x0, y0, w0, h0);
                icon.draw(g, mx, my, x0 + 2, y0 + 2, w0 - 4, h0 - 4);
            }
        };
        ButtonWidget button = new ButtonWidget(x, y, 16, 16, face, cd -> close());
        button.setClientSideWidget();
        button.setHoverTexture((g, mx, my, x0, y0, w0, h0) ->
                com.abo47.kubejslab.client.ui.base.LabGlow.drawGlow(g, mx, my,
                        (int) x0, (int) y0, (int) w0, (int) h0));
        panel.addWidget(button);
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

    private void close() {
        layer.setVisible(false);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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

    private static int mix(int a, int b, int percent) {
        int r = (a >> 16) & 0xFF;
        int g = (a >> 8) & 0xFF;
        int bl = a & 0xFF;
        int r2 = (b >> 16) & 0xFF;
        int g2 = (b >> 8) & 0xFF;
        int bl2 = b & 0xFF;
        float t = percent / 100f;
        int mr = Math.round(r + (r2 - r) * t);
        int mg = Math.round(g + (g2 - g) * t);
        int mb = Math.round(bl + (bl2 - bl) * t);
        return 0xFF000000 | (mr << 16) | (mg << 8) | mb;
    }

    private static List<Integer> loadPalette() {
        Path file = LabPathResolver.colorPaletteFile();
        if (Files.isRegularFile(file)) {
            try {
                JsonArray array = com.google.gson.JsonParser.parseString(Files.readString(file)).getAsJsonArray();
                List<Integer> loaded = new ArrayList<>();
                for (JsonElement element : array) {
                    try {
                        loaded.add((int) (Long.parseLong(element.getAsString(), 16) & 0xFFFFFF));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (!loaded.isEmpty()) {
                    return loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultPalette();
    }

    private static List<Integer> defaultPalette() {
        return new ArrayList<>(List.of(
                LabColors.SURFACE_BASE & 0xFFFFFF,
                LabColors.SURFACE_PANEL_ALT & 0xFFFFFF,
                LabColors.BORDER_BASE & 0xFFFFFF,
                LabColors.SUCCESS & 0xFFFFFF,
                LabColors.WARNING & 0xFFFFFF,
                LabColors.ERROR & 0xFFFFFF,
                LabColors.INTERACTIVE & 0xFFFFFF,
                LabColors.TEXT_PRIMARY & 0xFFFFFF));
    }

    private static void savePalette() {
        JsonArray array = new JsonArray();
        for (int color : PALETTE) {
            array.add(toHex(color));
        }
        try {
            Path file = LabPathResolver.colorPaletteFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, array.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void open(WidgetGroup layer, String title, String initialHex, Consumer<String> onApply) {
        new LabColorPickerModal(layer, title, initialHex, onApply);
    }
}
