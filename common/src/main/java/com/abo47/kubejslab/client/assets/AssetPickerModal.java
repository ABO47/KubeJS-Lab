package com.abo47.kubejslab.client.assets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.assets.AssetLibrary.AssetEntry;
import com.abo47.kubejslab.client.assets.AssetLibrary.AssetKind;
import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.ContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuAnimation;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuPanel;
import com.abo47.kubejslab.client.ui.picker.SearchFilter;
import com.abo47.kubejslab.client.ui.theme.IconAtlas;
import com.abo47.kubejslab.client.ui.theme.ModalHeader;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiGlow;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.client.ui.widgets.CommitField;
import com.abo47.kubejslab.client.ui.widgets.ScrollBarWidget;
import com.abo47.kubejslab.client.ui.widgets.ScrollMath;
import com.abo47.kubejslab.client.ui.widgets.TextField;


public final class AssetPickerModal {
    private static final int MODAL_W = 432;
    private static final int MODAL_H = 260;
    private static final int HEADER_BUTTON_SIZE = 16;
    private static final int HEADER_BUTTON_Y = 3;
    private static final int HEADER_GAP = 3;
    private static final int HEADER_TITLE_Y = 6;
    private static final int SEARCH_Y = 3;
    private static final int PREVIEW_X = 8;
    private static final int PREVIEW_W = 150;
    private static final int GRID_X = 166;
    private static final int BODY_Y = 22;
    private static final int BODY_BOTTOM_PAD = 26;
    private static final int SEARCH_H = 16;
    private static final int TILE_MIN = 44;
    private static final int TILE_MAX = 96;
    private static final int TILE_GAP = 8;
    private static final int TILE_PAD = 8;
    private static final int TILE_LABEL_H = 14;
    private static final int PREVIEW_LABEL_X = 8;
    private static final int PREVIEW_LABEL_LINE = 12;
    private static final int PREVIEW_IMAGE_Y = 44;
    private static final int PREVIEW_LABEL_MAX = 22;
    private static final long DOUBLE_CLICK_MS = 350;

    private static final int HEADER_CLOSE_X = ModalHeader.closeX(MODAL_W);
    private static final int DIM_COLOR = UiColors.withAlpha(UiColors.SURFACE_BASE, 140);
    private static final int PANEL_FILL = UiColors.withAlpha(UiColors.SURFACE_BASE, 252);
    private static final int ELEVATED_FILL = mix(UiColors.SURFACE_PANEL_ALT, UiColors.TEXT_PRIMARY, 10);
    private static final int SUBTLE_BORDER = mix(UiColors.BORDER_BASE, UiColors.SURFACE_BASE, 28);
    private static final IGuiTexture PANEL_TEXTURE = UiColors.bordered(PANEL_FILL, UiColors.BORDER_ACCENT);
    private static final IGuiTexture GRID_TEXTURE = UiColors.bordered(UiColors.withAlpha(ELEVATED_FILL, 190), SUBTLE_BORDER);
    private static final IGuiTexture HEADER_BUTTON_TEXTURE = UiColors.bordered(ELEVATED_FILL, UiColors.BORDER_BASE);
    private static final ColorRectTexture SHADOW_DEEP = new ColorRectTexture(UiColors.withAlpha(UiColors.SURFACE_BASE, 82));
    private static final ColorRectTexture SHADOW_NEAR = new ColorRectTexture(UiColors.withAlpha(UiColors.SURFACE_BASE, 120));

    private final WidgetGroup layer;
    private final WidgetGroup panel;
    private final WidgetGroup surface;
    private final ScrollBarWidget scrollBar;
    private final Consumer<String> onApply;
    private final Path root;
    private final State state = new State();
    private final int panelW;
    private final int panelH;
    private final int gridX;
    private final int gridW;
    private final int bodyY;
    private final int bodyH;
    private final int tileSize;

    private final String noneSelectedText = I18n.get(AssetKeys.ASSETS_NONE_SELECTED);
    private final String noAssetsText = I18n.get(AssetKeys.ASSETS_NO_ASSETS);
    private String dimsText = "";
    private final TextTexture dirTex = new TextTexture("", UiColors.TEXT_SECONDARY)
            .setType(TextTexture.TextType.LEFT_HIDE).setWidth(PREVIEW_W - PREVIEW_LABEL_X * 2)
            .setSupplier(() -> state.dir.isBlank() ? "/" : "/" + state.dir);
    private final TextTexture selectedTex = new TextTexture("", UiColors.TEXT_SECONDARY)
            .setType(TextTexture.TextType.LEFT_HIDE).setWidth(PREVIEW_W - PREVIEW_LABEL_X * 2)
            .setSupplier(() -> state.selected == null ? noneSelectedText
                    : crop(fileName(state.selected), PREVIEW_LABEL_MAX));
    private final TextTexture dimsTex = new TextTexture("", UiColors.TEXT_MUTED)
            .setType(TextTexture.TextType.LEFT_HIDE).setWidth(PREVIEW_W - PREVIEW_LABEL_X * 2)
            .setSupplier(() -> dimsText);
    private final TextTexture emptyTex = new TextTexture(noAssetsText,
            UiColors.TEXT_MUTED).setType(TextTexture.TextType.NORMAL);
    private IGuiTexture previewImageTex;
    private int previewImageX;
    private int previewImageY;
    private int previewImageW;
    private int previewImageH;

    private ButtonWidget backButton;
    private TextFieldWidget searchField;
    private WidgetGroup contextMenu;
    private ButtonWidget contextDismiss;
    private int contextMenuX;
    private int contextMenuY;
    private TileGridLayout layout;
    private List<AssetEntry> entries = List.of();

    private static final class State {
        String dir = "";
        String query = "";
        int scroll;
        int maxStart;
        int knobH;
        boolean showScroll;
        boolean dragging;
        String selected;
        String contextFile;
        boolean renameOpen;
        String renameDraft;
        boolean deleteArmed;
        long lastClickMs;
        long contextMenuMs;
    }

    private AssetPickerModal(WidgetGroup layer, Path root, String title, Consumer<String> onApply) {
        this.layer = layer;
        this.onApply = onApply;
        this.root = root;
        AssetLibrary.ensureAssetsDirs(root);
        this.panelW = MODAL_W;
        this.panelH = MODAL_H;
        this.gridX = GRID_X;
        this.gridW = panelW - GRID_X - PREVIEW_X;
        this.bodyY = BODY_Y;
        this.bodyH = panelH - BODY_Y - BODY_BOTTOM_PAD;
        int assetGridW = Math.max(1, gridW - TILE_PAD * 2 - ScrollBarWidget.RESERVED_WIDTH - TILE_GAP);
        int cols = Math.max(3, (assetGridW + TILE_GAP) / (TILE_MIN + TILE_GAP));
        this.tileSize = Math.min(TILE_MAX, Math.max(TILE_MIN, (assetGridW - (cols - 1) * TILE_GAP) / cols));
        int panelX = (UiLayout.ROOT_W - panelW) / 2;
        int panelY = (UiLayout.ROOT_H - panelH) / 2;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, UiLayout.ROOT_W, UiLayout.ROOT_H,
                new ColorRectTexture(DIM_COLOR), cd -> close()).setClientSideWidget());
        this.panel = new WidgetGroup(panelX, panelY, panelW, panelH) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
            }

            @Override
            public void drawInForeground(GuiGraphics g, int mx, int my, float pt) {
                int x = getPositionX();
                int y = getPositionY();
                SHADOW_DEEP.draw(g, mx, my, x + 5, y + 5, panelW, panelH);
                SHADOW_NEAR.draw(g, mx, my, x + 3, y + 3, panelW, panelH);
                PANEL_TEXTURE.draw(g, mx, my, x, y, panelW, panelH);
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

        panel.addWidget(new WidgetGroup(PREVIEW_X, HEADER_TITLE_Y, panelW - PREVIEW_X * 2, 9) {
            private final TextTexture titleTex = new TextTexture(title, UiColors.TEXT_PRIMARY)
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(panelW - PREVIEW_X * 2);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                titleTex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        });

        addHeaderButton(HEADER_CLOSE_X, HEADER_BUTTON_Y, "close", UiColors.ERROR, cd -> close());
        searchField = buildSearchField();
        panel.addWidget(searchField);

        WidgetGroup previewGroup = new WidgetGroup(PREVIEW_X, bodyY, PREVIEW_W, bodyH) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                UiColors.bordered(UiColors.withAlpha(UiColors.SURFACE_PANEL_ALT, 120), UiColors.BORDER_BASE)
                        .draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                dirTex.draw(g, mx, my, getPositionX() + PREVIEW_LABEL_X, getPositionY() + 8,
                        getSizeWidth() - PREVIEW_LABEL_X * 2, PREVIEW_LABEL_LINE);
                selectedTex.draw(g, mx, my, getPositionX() + PREVIEW_LABEL_X, getPositionY() + 8 + PREVIEW_LABEL_LINE,
                        getSizeWidth() - PREVIEW_LABEL_X * 2, PREVIEW_LABEL_LINE);
                dimsTex.draw(g, mx, my, getPositionX() + PREVIEW_LABEL_X, getPositionY() + 8 + PREVIEW_LABEL_LINE * 2,
                        getSizeWidth() - PREVIEW_LABEL_X * 2, PREVIEW_LABEL_LINE);
                if (previewImageTex != null) {
                    previewImageTex.draw(g, mx, my, getPositionX() + previewImageX, getPositionY() + previewImageY,
                            previewImageW, previewImageH);
                }
            }
        };
        panel.addWidget(previewGroup);

        this.surface = new WidgetGroup(GRID_X, bodyY, gridW, bodyH) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                int x = getPositionX();
                int y = getPositionY();
                int w = getSizeWidth();
                int h = getSizeHeight();
                GRID_TEXTURE.draw(g, mx, my, x, y, w, h);
                g.flush();
                g.enableScissor(x, y, x + w, y + h);
                for (Widget child : widgets) {
                    int cy = child.getPositionY();
                    if (cy + child.getSizeHeight() < y || cy > y + h) {
                        continue;
                    }
                    child.drawInBackground(g, mx, my, pt);
                }
                if (entries.isEmpty()) {
                    emptyTex.draw(g, mx, my, x + TILE_PAD, y + TILE_PAD, w - TILE_PAD * 2, 9);
                }
                for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
                    int vi = i - layout.scrollStart();
                    int tx = x + layout.tileX(vi);
                    int ty = y + layout.tileY(vi);
                    if (mx >= tx && my >= ty && mx < tx + tileSize && my < ty + tileSize) {
                        UiGlow.drawGlow(g, mx, my, tx, ty, tileSize, tileSize);
                    }
                }
                g.flush();
                g.disableScissor();
            }

            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!isMouseOverElement(mouseX, mouseY) || state.maxStart <= 0) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int next = ScrollMath.wheel(state.scroll, state.maxStart, layout.wheelStep(), wheelDelta);
                if (next != state.scroll) {
                    state.scroll = next;
                    refresh();
                }
                return true;
            }
        };
        panel.addWidget(surface);

        this.scrollBar = new ScrollBarWidget(
                gridX + gridW - ScrollBarWidget.RESERVED_WIDTH - 1, bodyY + TILE_PAD,
                ScrollBarWidget.RESERVED_WIDTH, bodyH - TILE_PAD * 2,
                () -> state.scroll,
                () -> state.maxStart,
                () -> state.knobH,
                value -> {
                    state.scroll = value;
                    refresh();
                },
                () -> state.dragging,
                value -> state.dragging = value,
                this::refresh);
        panel.addWidget(scrollBar);

        refresh();
    }

    public static void open(WidgetGroup layer, Path root, String title, Consumer<String> onApply) {
        new AssetPickerModal(layer, root, title, onApply);
    }

    private void close() {
        layer.setVisible(false);
    }

    private void refresh() {
        closeContext();
        entries = AssetLibrary.searchAssetEntries(root, state.dir, state.query);
        layout = TileGridLayout.calculate(gridW, bodyH, tileSize, tileSize, TILE_GAP, TILE_PAD, TILE_PAD,
                entries.size(), state.scroll);
        state.maxStart = layout.maxStart();
        state.knobH = layout.knobH();
        state.showScroll = layout.showScroll();
        scrollBar.setVisible(state.showScroll);

        if (backButton != null) {
            panel.removeWidget(backButton);
            backButton = null;
        }
        int searchW = firstHeaderX() - GRID_X - HEADER_GAP;
        if (searchField.getSizeWidth() != searchW) {
            boolean focused = searchField.isFocus();
            String text = searchField.getCurrentString();
            panel.removeWidget(searchField);
            searchField = buildSearchField();
            searchField.setCurrentString(text);
            panel.addWidget(searchField);
            if (focused) {
                searchField.setFocus(true);
            }
        }
        if (!state.dir.isBlank()) {
            backButton = addHeaderButton(backButtonX(), HEADER_BUTTON_Y, "back", UiColors.TEXT_SECONDARY, cd -> {
                state.dir = AssetPathResolver.parentRelative(state.dir);
                state.scroll = 0;
                refresh();
            });
        }

        refreshPreview();

        surface.clearAllWidgets();
        for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
            AssetEntry entry = entries.get(i);
            int vi = i - layout.scrollStart();
            int tx = layout.tileX(vi);
            int ty = layout.tileY(vi);
            surface.addWidget(buildTile(entry, tx, ty));
        }
    }

    private void refreshPreview() {
        String selected = state.selected;
        dimsText = "";
        previewImageTex = null;
        if (selected == null) {
            return;
        }
        AssetLibrary.AssetDimensions dims = AssetLibrary.assetDimensions(root, selected);
        if (dims == null) {
            return;
        }
        dimsText = dims.width() + "x" + dims.height();
        previewImageTex = AssetLibrary.chapterBackgroundTexture(root, selected);
        if (previewImageTex == null) {
            return;
        }
        int areaW = PREVIEW_W - PREVIEW_LABEL_X * 2;
        int areaH = bodyH - PREVIEW_IMAGE_Y - 8;
        float scale = Math.min(1.0f, Math.min((float) areaW / dims.width(), (float) areaH / dims.height()));
        previewImageW = Math.max(1, Math.round(dims.width() * scale));
        previewImageH = Math.max(1, Math.round(dims.height() * scale));
        previewImageX = PREVIEW_LABEL_X + (areaW - previewImageW) / 2;
        previewImageY = PREVIEW_IMAGE_Y + (areaH - previewImageH) / 2;
    }

    private ButtonWidget addHeaderButton(int x, int y, String iconKey, int accent, Consumer<ClickData> onClick) {
        ResourceTexture iconTex = IconAtlas.iconTexture(iconKey, accent);
        IGuiTexture face = new IGuiTexture() {
            @Override
            public void draw(GuiGraphics g, int mx, int my, float x0, float y0, int w0, int h0) {
                HEADER_BUTTON_TEXTURE.draw(g, mx, my, x0, y0, w0, h0);
                if (iconTex != null) {
                    iconTex.draw(g, mx, my, x0 + 2, y0 + 2, w0 - 4, h0 - 4);
                }
            }
        };
        ButtonWidget button = new ButtonWidget(x, y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, face, onClick);
        button.setClientSideWidget();
        button.setHoverTexture((g, mx, my, x0, y0, w0, h0) ->
                UiGlow.drawGlow(g, mx, my, (int) x0, (int) y0, (int) w0, (int) h0, accent));
        button.setClickedTexture(new IGuiTexture() {
            @Override
            public void draw(GuiGraphics g, int mx, int my, float x0, float y0, int w0, int h0) {
                UiColors.bordered(UiColors.pressedFill(accent), accent)
                        .draw(g, mx, my, x0, y0, w0, h0);
                if (iconTex != null) {
                    iconTex.draw(g, mx, my, x0 + 2, y0 + 2, w0 - 4, h0 - 4);
                }
            }
        });
        panel.addWidget(button);
        return button;
    }

    private TextFieldWidget buildSearchField() {
        int searchW = firstHeaderX() - GRID_X - HEADER_GAP;
        TextFieldWidget field = new TextField(GRID_X, SEARCH_Y, searchW, SEARCH_H,
                () -> state.query, value -> {
                    state.query = value;
                    state.scroll = 0;
                    refresh();
                });
        field.setClientSideWidget();
        field.setMaxStringLength(80);
        field.setValidator(SearchFilter::normalize);
        field.setBordered(false);
        field.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        field.setTextColor(UiColors.TEXT_PRIMARY);
        return field;
    }

    private int firstHeaderX() {
        return state.dir.isBlank() ? HEADER_CLOSE_X : backButtonX();
    }

    private static int backButtonX() {
        return ModalHeader.beforeX(HEADER_CLOSE_X);
    }

    private static int mix(int a, int b, int percent) {
        int inverse = 100 - percent;
        return (Math.max(0, ((a >>> 24) * inverse + (b >>> 24) * percent) / 100) << 24)
                | (Math.max(0, ((a >>> 16 & 0xFF) * inverse + (b >>> 16 & 0xFF) * percent) / 100) << 16)
                | (Math.max(0, ((a >>> 8 & 0xFF) * inverse + (b >>> 8 & 0xFF) * percent) / 100) << 8)
                | Math.max(0, ((a & 0xFF) * inverse + (b & 0xFF) * percent) / 100);
    }

    private WidgetGroup buildTile(AssetEntry entry, int tx, int ty) {
        boolean selected = state.selected != null && state.selected.equals(entry.relativePath());
        boolean renaming = state.renameOpen && state.contextFile != null
                && state.contextFile.equals(entry.relativePath());
        int iconAreaH = Math.max(24, tileSize - TILE_LABEL_H - 8);
        WidgetGroup tile = new WidgetGroup(tx, ty, tileSize, tileSize);
        if (selected) {
            tile.setBackground(new ColorRectTexture(UiColors.withAlpha(UiColors.INTERACTIVE, 54)));
        }
        if (entry.kind() == AssetKind.DIRECTORY) {
            int iconSize = Math.max(24, Math.min(96, Math.min(tileSize - 24, iconAreaH - 12)));
            int iconX = Math.max(0, (tileSize - iconSize) / 2);
            int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
            ResourceTexture folderIcon = IconAtlas.iconTexture("folder", UiColors.TEXT_SECONDARY);
            if (folderIcon != null) {
                tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, folderIcon));
            } else {
                tile.addWidget(new WidgetGroup(0, 2, tileSize, iconAreaH) {
                    private final TextTexture dirFallback = new TextTexture("[dir]", UiColors.TEXT_MUTED)
                            .setType(TextTexture.TextType.NORMAL)
                            .setWidth(tileSize - 4);

                    @Override
                    public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                        dirFallback.draw(g, mx, my, getPositionX() + 2, getPositionY(),
                                getSizeWidth() - 4, getSizeHeight());
                    }
                });
            }
        } else if (entry.kind() == AssetKind.IMAGE || entry.kind() == AssetKind.GIF) {
            int thumbW = Math.max(12, tileSize - 14);
            int thumbH = Math.max(16, iconAreaH - 4);
            tile.addWidget(new ImageWidget((tileSize - thumbW) / 2, 4, thumbW, thumbH,
                    AssetLibrary.assetThumbnailTexture(root, entry.relativePath())));
        } else {
            int iconSize = Math.max(24, Math.min(96, Math.min(tileSize - 24, iconAreaH - 12)));
            int iconX = Math.max(0, (tileSize - iconSize) / 2);
            int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
            tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize,
                    IconAtlas.iconTexture("box", UiColors.TEXT_SECONDARY)));
        }
        if (renaming) {
            CommitField renameField = new CommitField(
                    2, tileSize - TILE_LABEL_H - 2, tileSize - 4, TILE_LABEL_H + 2,
                    null, name -> commitRename(entry.relativePath(), name));
            renameField.setClientSideWidget();
            renameField.setMaxStringLength(80);
            renameField.setBordered(false);
            renameField.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
            renameField.setTextColor(UiColors.TEXT_PRIMARY);
            renameField.setCurrentString(state.renameDraft);
            tile.addWidget(renameField);
        } else {
            tile.addWidget(new WidgetGroup(2, tileSize - TILE_LABEL_H, tileSize - 4, TILE_LABEL_H) {
                private final TextTexture nameTex = new TextTexture(entry.name(), UiColors.TEXT_SECONDARY)
                        .setType(TextTexture.TextType.NORMAL)
                        .setWidth(tileSize - 4);

                @Override
                public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                    nameTex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
            });
        }
        tile.addWidget(new ButtonWidget(0, 0, tileSize, tileSize, IGuiTexture.EMPTY,
                cd -> onTileClick(cd, entry)));
        return tile;
    }

    private void onTileClick(ClickData click, AssetEntry entry) {
        if (click.button == UiColors.MOUSE_BUTTON_LEFT) {
            if (entry.directory()) {
                state.dir = entry.relativePath();
                state.scroll = 0;
                refresh();
                return;
            }
            long now = System.currentTimeMillis();
            if (now - state.lastClickMs < DOUBLE_CLICK_MS) {
                state.lastClickMs = 0;
                onApply.accept(entry.relativePath());
                close();
            } else {
                state.lastClickMs = now;
                state.selected = entry.relativePath();
                refresh();
            }
        } else if (click.button == UiColors.MOUSE_BUTTON_RIGHT) {
            openContext(entry, gridX + txOf(entry), bodyY + tyOf(entry));
        }
    }

    private int txOf(AssetEntry entry) {
        int i = entries.indexOf(entry);
        return i < 0 ? 0 : layout.tileX(i - layout.scrollStart());
    }

    private int tyOf(AssetEntry entry) {
        int i = entries.indexOf(entry);
        return i < 0 ? 0 : layout.tileY(i - layout.scrollStart());
    }

    private void openContext(AssetEntry entry, int tilePanelX, int tilePanelY) {
        if (state.contextFile == null || !state.contextFile.equals(entry.relativePath())) {
            state.deleteArmed = false;
        }
        state.contextFile = entry.relativePath();
        state.renameOpen = false;
        List<ContextAction> actions = contextActions(entry);
        int menuW = ContextMenuPanel.menuWidth(actions);
        int menuH = ContextMenuPanel.menuHeight(actions);
        contextMenuX = clamp(tilePanelX + tileSize - menuW, 4, panelW - menuW - 4);
        contextMenuY = clamp(tilePanelY, 4, panelH - menuH - 4);
        contextDismiss = new ButtonWidget(0, 0, panelW, panelH, IGuiTexture.EMPTY,
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

    private List<ContextAction> contextActions(AssetEntry entry) {
        List<ContextAction> actions = new ArrayList<>();
        if (entry.directory()) {
            actions.add(new ContextAction(I18n.get(AssetKeys.ASSETS_OPEN), "open", ActionTone.PRIMARY,
                    () -> {
                        state.dir = entry.relativePath();
                        state.scroll = 0;
                        refresh();
                    }));
        } else {
            actions.add(new ContextAction(I18n.get(AssetKeys.ASSETS_USE), "icon", ActionTone.PRIMARY,
                    () -> {
                        onApply.accept(entry.relativePath());
                        layer.setVisible(false);
                    }));
        }
        actions.add(new ContextAction(I18n.get(AssetKeys.ASSETS_RENAME), "rename", ActionTone.NEUTRAL,
                () -> {
                    state.renameOpen = true;
                    state.renameDraft = fileName(state.contextFile);
                    refresh();
                }));
        if (!entry.directory()) {
            if (state.deleteArmed) {
                actions.add(new ContextAction(I18n.get(AssetKeys.ASSETS_CONFIRM), "delete",
                        ActionTone.DANGER, () -> {
                            AssetLibrary.deleteAssetFile(root, state.contextFile);
                            if (state.selected != null && state.selected.equals(state.contextFile)) {
                                state.selected = null;
                            }
                            refresh();
                        }));
            } else {
                actions.add(new ContextAction(I18n.get(AssetKeys.ASSETS_DELETE), "delete",
                        ActionTone.DANGER, () -> {
                            state.deleteArmed = true;
                            closeContext();
                            openContext(entry, contextMenuX, contextMenuY);
                        }));
            }
        }
        return actions;
    }

    private void commitRename(String oldRelative, String rawName) {
        String name = rawName.trim();
        if (name.isBlank()) {
            state.renameOpen = false;
            refresh();
            return;
        }
        String parent = AssetPathResolver.parentRelative(oldRelative);
        String ext = extension(oldRelative);
        String nextName = name.indexOf('.') < 0 && !ext.isBlank() ? name + "." + ext : name;
        String nextRelative = parent.isBlank() ? nextName : parent + "/" + nextName;
        AssetLibrary.renameAssetFile(root, oldRelative, name);
        if (state.selected != null && state.selected.equals(oldRelative)) {
            state.selected = nextRelative;
        }
        state.renameOpen = false;
        refresh();
    }

    private static String fileName(String relativePath) {
        int slash = relativePath.lastIndexOf('/');
        return slash < 0 ? relativePath : relativePath.substring(slash + 1);
    }

    private static String extension(String relativePath) {
        String name = fileName(relativePath);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }

    private static String crop(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
