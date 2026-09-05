package com.abo47.kubejslab.client.assets;

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

import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.ContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuAnimation;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuPanel;
import com.abo47.kubejslab.client.ui.theme.ModalHeader;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.client.ui.widgets.ActionButton;
import com.abo47.kubejslab.client.ui.widgets.CommitField;
import com.abo47.kubejslab.client.ui.widgets.ScrollBarWidget;
import com.abo47.kubejslab.workspace.WorkspacePaths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


public final class ColorPickerModal {
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

    private static final int DIM_COLOR = UiColors.withAlpha(UiColors.SURFACE_BASE, 140);
    private static final int ELEVATED_FILL = mix(UiColors.SURFACE_PANEL_ALT, UiColors.TEXT_PRIMARY, 10);
    private static final int SUBTLE_BORDER = mix(UiColors.BORDER_BASE, UiColors.SURFACE_BASE, 28);

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

    private ColorPickerModal(WidgetGroup layer, String title, String initialHex, Consumer<String> onApply) {
        this.layer = layer;
        this.onApply = onApply;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, UiLayout.ROOT_W, UiLayout.ROOT_H,
                new ColorRectTexture(DIM_COLOR), cd -> close()).setClientSideWidget());

        int bodyH = MODAL_H - BODY_Y - BODY_BOTTOM_PAD;
        int wheelSize = Math.min(LEFT_W - 20, bodyH - 84);
        int initialColor = parseHexOrDefault(initialHex, 0xFFFFFFFF);
        this.draftColor = initialColor;

        this.panel = new WidgetGroup((UiLayout.ROOT_W - MODAL_W) / 2,
                (UiLayout.ROOT_H - MODAL_H) / 2, MODAL_W, MODAL_H) {
            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                super.mouseClicked(mx, my, button);
                return isMouseOverElement(mx, my);
            }
        };
        this.panel.setBackground(UiColors.bordered(
                UiColors.withAlpha(UiColors.SURFACE_BASE, 252), UiColors.BORDER_ACCENT));
        layer.addWidget(panel);
        panel.addWidget(ModalHeader.titleLabel(title, 8, ModalHeader.contentW(MODAL_W, 8)));
        panel.addWidget(ModalHeader.closeButton(ModalHeader.closeX(MODAL_W), this::close));

        WidgetGroup left = new WidgetGroup(PREVIEW_X, BODY_Y, LEFT_W, bodyH);
        left.setBackground(UiColors.bordered(
                UiColors.withAlpha(UiColors.SURFACE_PANEL_ALT, 120), UiColors.BORDER_BASE));
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

        left.addWidget(new ActionButton(8, bodyH - 20, LEFT_W - 16, 16,
                I18n.get(AssetKeys.ASSETS_USE), () -> {
            onApply.accept(toHex(draftColor));
            close();
        }));

        WidgetGroup right = new WidgetGroup(RIGHT_X, BODY_Y, MODAL_W - RIGHT_X - PREVIEW_X, bodyH);
        right.setBackground(UiColors.bordered(UiColors.withAlpha(ELEVATED_FILL, 190), SUBTLE_BORDER));
        panel.addWidget(right);

        this.emptyTex = new TextTexture(I18n.get(AssetKeys.ASSETS_NO_ASSETS), UiColors.TEXT_MUTED)
                .setType(TextTexture.TextType.NORMAL);

        this.palettePanel = new WidgetGroup(0, 0, MODAL_W - RIGHT_X - PREVIEW_X, bodyH - 28);
        right.addWidget(palettePanel);

        right.addWidget(new ActionButton(4, bodyH - 20, MODAL_W - RIGHT_X - PREVIEW_X - 8, 16,
                I18n.get(AssetKeys.COLOR_SAVE_PALETTE), () -> {
            int rgb = draftColor & 0xFFFFFF;
            if (!PALETTE.contains(rgb)) {
                PALETTE.add(rgb);
                savePalette();
            }
            rebuildPalette();
        }));
        rebuildPalette();
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
            palettePanel.addWidget(new ScrollBarWidget(
                    CELL_PAD + layout.scrollBarX() + 1, PALETTE_TOP + layout.scrollBarY(),
                    ScrollBarWidget.RESERVED_WIDTH, layout.scrollBarH(),
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
                UiColors.bordered(color | 0xFF000000, UiColors.BORDER_BASE),
                click -> onSwatchClick(click, color));
        hit.setClientSideWidget();
        hit.setHoverBorderTexture(1, UiColors.BORDER_ACCENT);
        return hit;
    }

    private void onSwatchClick(ClickData click, int color) {
        if (click.button == UiColors.MOUSE_BUTTON_RIGHT) {
            openContext(color);
            return;
        }
        if (click.button != UiColors.MOUSE_BUTTON_LEFT) {
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
        List<ContextAction> actions = List.of(
                new ContextAction(I18n.get(AssetKeys.ASSETS_USE), "icon", ActionTone.PRIMARY,
                        () -> {
                            onApply.accept(toHex(contextValue));
                            close();
                        }),
                new ContextAction(I18n.get(AssetKeys.ASSETS_DELETE), "delete", ActionTone.DANGER,
                        () -> {
                            if (PALETTE.removeIf(value -> value == contextValue)) {
                                savePalette();
                            }
                            rebuildPalette();
                        }));
        int menuW = ContextMenuPanel.menuWidth(actions);
        int menuH = ContextMenuPanel.menuHeight(actions);
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
        contextMenu = ContextMenuAnimation.wrap(
                ContextMenuPanel.build(contextMenuX, contextMenuY, actions, this::closeContext),
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

    private TextFieldWidget buildHexField(int x, int y, int w, int h) {
        CommitField field = new CommitField(x, y, w, h,
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
        field.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        field.setTextColor(UiColors.TEXT_PRIMARY);
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
        Path file = WorkspacePaths.colorPaletteFile();
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
                UiColors.SURFACE_BASE & 0xFFFFFF,
                UiColors.SURFACE_PANEL_ALT & 0xFFFFFF,
                UiColors.BORDER_BASE & 0xFFFFFF,
                UiColors.SUCCESS & 0xFFFFFF,
                UiColors.WARNING & 0xFFFFFF,
                UiColors.ERROR & 0xFFFFFF,
                UiColors.INTERACTIVE & 0xFFFFFF,
                UiColors.TEXT_PRIMARY & 0xFFFFFF));
    }

    private static void savePalette() {
        JsonArray array = new JsonArray();
        for (int color : PALETTE) {
            array.add(toHex(color));
        }
        try {
            Path file = WorkspacePaths.colorPaletteFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, array.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void open(WidgetGroup layer, String title, String initialHex, Consumer<String> onApply) {
        new ColorPickerModal(layer, title, initialHex, onApply);
    }
}
