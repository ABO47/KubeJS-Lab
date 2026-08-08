package com.abo47.kubejslab.client.ui.assets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

import com.abo47.kubejslab.client.ui.assets.LabAssetLibrary.AssetEntry;
import com.abo47.kubejslab.client.ui.assets.LabAssetLibrary.AssetKind;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabIconAtlas;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabScrollBarWidget;
import com.abo47.kubejslab.client.ui.base.LabScrollMath;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuAnimation;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuPanel;
import com.abo47.kubejslab.client.ui.picker.LabSearchFilter;

import dev.architectury.platform.Platform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import com.mojang.blaze3d.systems.RenderSystem;


public final class LabAssetPickerModal {
    private static final int MODAL_MARGIN = 20;
    private static final int HEADER_H = 22;
    private static final int HEADER_PAD = 6;
    private static final int HEADER_BUTTON_SIZE = 18;
    private static final int HEADER_GAP = 3;
    private static final int CONTROLS_GAP = 4;
    private static final int SEARCH_H = 16;
    private static final int PREVIEW_W = 140;
    private static final int BODY_PAD = 6;
    private static final int TILE_W = 56;
    private static final int TILE_H = 52;
    private static final int TILE_GAP = 6;
    private static final int TILE_LABEL_H = 14;
    private static final int TILE_PAD = 2;
    private static final int PREVIEW_LABEL_X = 8;
    private static final int PREVIEW_LABEL_LINE = 12;
    private static final int PREVIEW_IMAGE_Y = 44;
    private static final int PREVIEW_LABEL_MAX = 22;
    private static final long DOUBLE_CLICK_MS = 300;

    private final WidgetGroup layer;
    private final WidgetGroup panel;
    private final WidgetGroup surface;
    private final LabScrollBarWidget scrollBar;
    private final Consumer<String> onApply;
    private final Path root;
    private final State state = new State();
    private final int panelW;
    private final int panelH;
    private final int gridX;
    private final int gridW;
    private final int bodyY;
    private final int bodyH;

    private final String noneSelectedText = I18n.get(LabGuiKeys.LAB_ASSETS_NONE_SELECTED);
    private final String noAssetsText = I18n.get(LabGuiKeys.LAB_ASSETS_NO_ASSETS);
    private String dimsText = "";
    private final TextTexture dirTex = new TextTexture("", LabColors.TEXT_SECONDARY)
            .setType(TextTexture.TextType.LEFT_HIDE).setWidth(PREVIEW_W - PREVIEW_LABEL_X * 2)
            .setSupplier(() -> state.dir.isBlank() ? "/" : "/" + state.dir);
    private final TextTexture selectedTex = new TextTexture("", LabColors.TEXT_SECONDARY)
            .setType(TextTexture.TextType.LEFT_HIDE).setWidth(PREVIEW_W - PREVIEW_LABEL_X * 2)
            .setSupplier(() -> state.selected == null ? noneSelectedText
                    : crop(fileName(state.selected), PREVIEW_LABEL_MAX));
    private final TextTexture dimsTex = new TextTexture("", LabColors.TEXT_MUTED)
            .setType(TextTexture.TextType.LEFT_HIDE).setWidth(PREVIEW_W - PREVIEW_LABEL_X * 2)
            .setSupplier(() -> dimsText);
    private final TextTexture emptyTex = new TextTexture(noAssetsText,
            LabColors.TEXT_MUTED).setType(TextTexture.TextType.NORMAL);
    private IGuiTexture previewImageTex;
    private int previewImageX;
    private int previewImageY;
    private int previewImageW;
    private int previewImageH;

    private WidgetGroup backIcon;
    private ButtonWidget backHit;
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

    private LabAssetPickerModal(WidgetGroup layer, String title, Consumer<String> onApply) {
        this.layer = layer;
        this.onApply = onApply;
        this.root = Platform.getConfigFolder().resolve("kubejslab").resolve("assets");
        LabAssetLibrary.ensureAssetsDirs(root);
        this.panelW = LabLayout.ROOT_W - MODAL_MARGIN * 2;
        this.panelH = LabLayout.ROOT_H - MODAL_MARGIN * 2;
        this.gridX = BODY_PAD * 2 + PREVIEW_W;
        this.gridW = panelW - gridX - BODY_PAD;
        int controlsY = HEADER_H + CONTROLS_GAP;
        this.bodyY = controlsY + SEARCH_H + CONTROLS_GAP;
        this.bodyH = panelH - bodyY - BODY_PAD;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H, IGuiTexture.EMPTY,
                cd -> layer.setVisible(false)).setClientSideWidget());
        this.panel = new WidgetGroup(MODAL_MARGIN, MODAL_MARGIN, panelW, panelH);
        panel.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        layer.addWidget(panel);

        panel.addWidget(new WidgetGroup(HEADER_PAD, (HEADER_H - 9) / 2, panelW - HEADER_PAD * 2, HEADER_H) {
            private final TextTexture titleTex = new TextTexture(title, LabColors.TEXT_PRIMARY)
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(panelW - HEADER_PAD * 2 - HEADER_BUTTON_SIZE * 2 - HEADER_GAP);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                titleTex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        });

        int closeX = panelW - HEADER_PAD - HEADER_BUTTON_SIZE;
        int closeY = (HEADER_H - HEADER_BUTTON_SIZE) / 2;
        addHeaderIcon(closeX, closeY, "close", cd -> layer.setVisible(false));

        TextFieldWidget searchField = new TextFieldWidget(gridX, controlsY, gridW, SEARCH_H,
                () -> state.query, value -> {
                    state.query = value;
                    state.scroll = 0;
                    refresh();
                });
        searchField.setClientSideWidget();
        searchField.setMaxStringLength(Integer.MAX_VALUE);
        searchField.setValidator(LabSearchFilter::normalize);
        searchField.setBordered(false);
        searchField.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        searchField.setTextColor(LabColors.TEXT_PRIMARY);
        panel.addWidget(searchField);

        panel.addWidget(new WidgetGroup(BODY_PAD, bodyY, PREVIEW_W, bodyH) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
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
        });

        this.surface = new WidgetGroup(gridX, bodyY, gridW, bodyH) {
            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                int x = getPositionX();
                int y = getPositionY();
                int w = getSizeWidth();
                int h = getSizeHeight();
                g.flush();
                RenderSystem.enableScissor(x, y, x + w, y + h);
                for (Widget child : widgets) {
                    int cy = child.getPositionY();
                    if (cy + child.getSizeHeight() < y || cy > y + h) {
                        continue;
                    }
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    child.drawInBackground(g, mx, my, pt);
                }
                if (entries.isEmpty()) {
                    emptyTex.draw(g, mx, my, x, y + (h - 9) / 2, w, 9);
                }
                for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
                    int vi = i - layout.scrollStart();
                    int tx = x + layout.tileX(vi);
                    int ty = y + layout.tileY(vi);
                    if (mx >= tx && my >= ty && mx < tx + TILE_W && my < ty + TILE_H) {
                        LabGlow.drawGlow(g, mx, my, tx, ty, TILE_W, TILE_H);
                    }
                }
                g.flush();
                RenderSystem.disableScissor();
            }

            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!isMouseOverElement(mouseX, mouseY) || state.maxStart <= 0) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int next = LabScrollMath.wheel(state.scroll, state.maxStart, layout.wheelStep(), wheelDelta);
                if (next != state.scroll) {
                    state.scroll = next;
                    refresh();
                }
                return true;
            }
        };
        panel.addWidget(surface);

        this.scrollBar = new LabScrollBarWidget(
                gridX + gridW - LabScrollBarWidget.RESERVED_WIDTH - 1, bodyY + TILE_PAD,
                LabScrollBarWidget.RESERVED_WIDTH, bodyH - TILE_PAD * 2,
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

    public static void open(WidgetGroup layer, String title, Consumer<String> onApply) {
        new LabAssetPickerModal(layer, title, onApply);
    }

    private void refresh() {
        closeContext();
        entries = LabAssetLibrary.searchAssetEntries(root, state.dir, state.query);
        layout = TileGridLayout.calculate(gridW, bodyH, TILE_W, TILE_H, TILE_GAP, TILE_PAD, TILE_PAD,
                entries.size(), state.scroll);
        state.maxStart = layout.maxStart();
        state.knobH = layout.knobH();
        state.showScroll = layout.showScroll();
        scrollBar.setVisible(state.showScroll);

        if (backIcon != null) {
            panel.removeWidget(backIcon);
        }
        if (backHit != null) {
            panel.removeWidget(backHit);
        }
        backIcon = null;
        backHit = null;
        if (!state.dir.isBlank()) {
            int backX = panelW - HEADER_PAD - HEADER_BUTTON_SIZE - HEADER_GAP - HEADER_BUTTON_SIZE;
            int backY = (HEADER_H - HEADER_BUTTON_SIZE) / 2;
            backIcon = addHeaderIcon(backX, backY, "back", null);
            backHit = new ButtonWidget(backX, backY, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, IGuiTexture.EMPTY,
                    cd -> {
                        state.dir = LabAssetPathResolver.parentRelative(state.dir);
                        state.scroll = 0;
                        refresh();
                    });
            backHit.setClientSideWidget();
            panel.addWidget(backHit);
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
        LabAssetLibrary.AssetDimensions dims = LabAssetLibrary.assetDimensions(root, selected);
        if (dims == null) {
            return;
        }
        dimsText = dims.width() + "x" + dims.height();
        previewImageTex = LabAssetLibrary.chapterBackgroundTexture(root, selected);
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

    private WidgetGroup addHeaderIcon(int x, int y, String iconKey, Consumer<ClickData> onClick) {
        WidgetGroup icon = new WidgetGroup(x, y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE) {
            private final ResourceTexture iconTex = LabIconAtlas.iconTexture(iconKey, LabActionTone.NEUTRAL);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                iconTex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                if (isMouseOverElement(mx, my)) {
                    LabGlow.drawGlow(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
            }
        };
        panel.addWidget(icon);
        if (onClick != null) {
            panel.addWidget(new ButtonWidget(x, y, HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE, IGuiTexture.EMPTY,
                    onClick).setClientSideWidget());
        }
        return icon;
    }

    private WidgetGroup buildTile(AssetEntry entry, int tx, int ty) {
        boolean selected = state.selected != null && state.selected.equals(entry.relativePath());
        boolean renaming = state.renameOpen && state.contextFile != null
                && state.contextFile.equals(entry.relativePath());
        int iconAreaH = Math.max(24, TILE_H - TILE_LABEL_H - 8);
        WidgetGroup tile = new WidgetGroup(tx, ty, TILE_W, TILE_H);
        if (selected) {
            tile.setBackground(new ColorRectTexture(LabColors.withAlpha(LabColors.INTERACTIVE, 54)));
        }
        if (entry.kind() == AssetKind.DIRECTORY) {
            int iconSize = Math.max(24, Math.min(96, Math.min(TILE_W - 24, iconAreaH - 12)));
            int iconX = Math.max(0, (TILE_W - iconSize) / 2);
            int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
            tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize,
                    LabIconAtlas.iconTexture("folder", LabActionTone.NEUTRAL)));
        } else if (entry.kind() == AssetKind.IMAGE || entry.kind() == AssetKind.GIF) {
            int thumbW = Math.max(12, TILE_W - 14);
            int thumbH = Math.max(16, iconAreaH - 4);
            tile.addWidget(new ImageWidget((TILE_W - thumbW) / 2, 4, thumbW, thumbH,
                    LabAssetLibrary.assetThumbnailTexture(root, entry.relativePath())));
        } else {
            int iconSize = Math.max(24, Math.min(96, Math.min(TILE_W - 24, iconAreaH - 12)));
            int iconX = Math.max(0, (TILE_W - iconSize) / 2);
            int iconY = Math.max(4, (iconAreaH - iconSize) / 2);
            tile.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize,
                    LabIconAtlas.iconTexture("box", LabActionTone.NEUTRAL)));
        }
        if (renaming) {
            LabCommitFieldWidget renameField = new LabCommitFieldWidget(
                    2, TILE_H - TILE_LABEL_H - 2, TILE_W - 4, TILE_LABEL_H + 2,
                    null, name -> commitRename(entry.relativePath(), name));
            renameField.setClientSideWidget();
            renameField.setMaxStringLength(80);
            renameField.setBordered(false);
            renameField.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
            renameField.setTextColor(LabColors.TEXT_PRIMARY);
            renameField.setCurrentString(state.renameDraft);
            tile.addWidget(renameField);
        } else {
            tile.addWidget(new WidgetGroup(2, TILE_H - TILE_LABEL_H, TILE_W - 4, TILE_LABEL_H) {
                private final TextTexture nameTex = new TextTexture(entry.name(), LabColors.TEXT_SECONDARY)
                        .setType(TextTexture.TextType.LEFT_HIDE)
                        .setWidth(TILE_W - 4);

                @Override
                public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                    nameTex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
            });
        }
        tile.addWidget(new ButtonWidget(tx, ty, TILE_W, TILE_H, IGuiTexture.EMPTY,
                cd -> onTileClick(cd, entry, tx, ty)));
        return tile;
    }

    private void onTileClick(ClickData click, AssetEntry entry, int tx, int ty) {
        if (click.button == LabColors.MOUSE_BUTTON_LEFT) {
            long now = System.currentTimeMillis();
            if (now - state.lastClickMs < DOUBLE_CLICK_MS) {
                state.lastClickMs = 0;
                if (entry.directory()) {
                    state.dir = entry.relativePath();
                    state.scroll = 0;
                    refresh();
                } else {
                    onApply.accept(entry.relativePath());
                    layer.setVisible(false);
                }
            } else {
                state.lastClickMs = now;
                state.selected = entry.relativePath();
                refresh();
            }
        } else if (click.button == LabColors.MOUSE_BUTTON_RIGHT) {
            openContext(entry, gridX + tx, bodyY + ty);
        }
    }

    private void openContext(AssetEntry entry, int tilePanelX, int tilePanelY) {
        if (state.contextFile == null || !state.contextFile.equals(entry.relativePath())) {
            state.deleteArmed = false;
        }
        state.contextFile = entry.relativePath();
        state.renameOpen = false;
        List<LabContextAction> actions = contextActions(entry);
        int menuW = LabContextMenuPanel.menuWidth(actions);
        int menuH = LabContextMenuPanel.menuHeight(actions);
        contextMenuX = clamp(tilePanelX + TILE_W - menuW, 4, panelW - menuW - 4);
        contextMenuY = clamp(tilePanelY, 4, panelH - menuH - 4);
        contextDismiss = new ButtonWidget(0, 0, panelW, panelH, IGuiTexture.EMPTY,
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

    private List<LabContextAction> contextActions(AssetEntry entry) {
        List<LabContextAction> actions = new ArrayList<>();
        if (entry.directory()) {
            actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_ASSETS_OPEN), "open", LabActionTone.PRIMARY,
                    () -> {
                        state.dir = entry.relativePath();
                        state.scroll = 0;
                        refresh();
                    }));
        } else {
            actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_ASSETS_USE), "icon", LabActionTone.PRIMARY,
                    () -> {
                        onApply.accept(entry.relativePath());
                        layer.setVisible(false);
                    }));
        }
        actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_ASSETS_RENAME), "rename", LabActionTone.NEUTRAL,
                () -> {
                    state.renameOpen = true;
                    state.renameDraft = fileName(state.contextFile);
                    refresh();
                }));
        if (!entry.directory()) {
            if (state.deleteArmed) {
                actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_ASSETS_CONFIRM), "delete",
                        LabActionTone.DANGER, () -> {
                            LabAssetLibrary.deleteAssetFile(root, state.contextFile);
                            if (state.selected != null && state.selected.equals(state.contextFile)) {
                                state.selected = null;
                            }
                            refresh();
                        }));
            } else {
                actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_ASSETS_DELETE), "delete",
                        LabActionTone.DANGER, () -> {
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
        String parent = LabAssetPathResolver.parentRelative(oldRelative);
        String ext = extension(oldRelative);
        String nextName = name.indexOf('.') < 0 && !ext.isBlank() ? name + "." + ext : name;
        String nextRelative = parent.isBlank() ? nextName : parent + "/" + nextName;
        LabAssetLibrary.renameAssetFile(root, oldRelative, name);
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
