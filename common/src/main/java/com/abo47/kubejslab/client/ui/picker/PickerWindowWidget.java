package com.abo47.kubejslab.client.ui.picker;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.theme.IconAtlas;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiGlow;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.client.ui.widgets.ActionButton;
import com.abo47.kubejslab.client.ui.widgets.ScrollBarWidget;
import com.abo47.kubejslab.client.ui.widgets.ScrollMath;
import com.abo47.kubejslab.client.ui.widgets.TextField;

import com.abo47.kubejslab.workspace.ModConfig;


public final class PickerWindowWidget extends WidgetGroup {
    public static final int GRID_COLS = 6;
    public static final int GRID_ROWS = 5;
    public static final int DRAG_H = 6;
    private static final int TILE = 18;
    private static final int GAP = 2;
    private static final int PAD = 3;
    private static final int SCROLLBAR_W = 6;
    public static final int HEADER_H = DRAG_H + TILE;
    public static final int WINDOW_W = PAD + GRID_COLS * (TILE + GAP) + PAD + SCROLLBAR_W;
    public static final int BODY_H = PAD + GRID_ROWS * (TILE + GAP) - GAP + PAD;
    private static final int MODE_W = 18;
    private static final int BUTTON_W = 18;
    private static final int SEARCH_X = PAD + TILE + GAP + TILE + GAP;
    private static final int SEARCH_W = WINDOW_W - SEARCH_X - PAD;
    private static final IGuiTexture WINDOW_TEX =
            UiColors.bordered(UiColors.SURFACE_PANEL_ALT, UiColors.BORDER_BASE);
    private static final IGuiTexture HEADER_BTN_TEX =
            UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE);

    private enum Mode {
        ITEMS,
        TAGS,
        FLUIDS
    }

    private Consumer<Pick> pickListener;
    private boolean minimized;
    private boolean dragging;
    private double lastMouseX;
    private double lastMouseY;
    private int lastPositionX;
    private int lastPositionY;
    private Mode mode = Mode.ITEMS;
    private String query = "";
    private int scroll;
    private int scrollMax;
    private boolean scrollDragging;

    private final ActionButton minimizeButton;
    private final ModeButton modeButton;
    private final TextFieldWidget searchField;
    private final List<Widget> bodyWidgets = new ArrayList<>();
    private List<Pick> entries = List.of();
    private final TextTexture emptyText;

    public PickerWindowWidget(int x, int y) {
        super(x, y, WINDOW_W, HEADER_H);
        setClientSideWidget();
        minimizeButton = new ActionButton(PAD, DRAG_H, BUTTON_W, TILE, "-", this::toggleMinimized);
        minimizeButton.setHoverTooltips(List.of(
                Component.translatable(PickerKeys.PICKER_MINIMIZE)));
        modeButton = new ModeButton(PAD + TILE + GAP, DRAG_H, MODE_W, TILE, mode);
        searchField = new TextField(SEARCH_X, DRAG_H, SEARCH_W, TILE, () -> query,
                value -> {
                    query = SearchNormalizer.normalizeUserSearch(value);
                    scroll = 0;
                    rebuildBody();
                });
        searchField.setClientSideWidget();
        searchField.setMaxStringLength(Integer.MAX_VALUE);
        searchField.setValidator(SearchNormalizer::normalizeUserSearch);
        searchField.setBordered(false);
        searchField.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        searchField.setTextColor(UiColors.TEXT_PRIMARY);
        searchField.setCurrentString(query);
        emptyText = new TextTexture(Component.translatable(PickerKeys.PICKER_EMPTY).getString(),
                UiColors.TEXT_MUTED).setWidth(WINDOW_W).setType(TextTexture.TextType.NORMAL);
        rebuildHeader();
    }

    public static PickerWindowWidget create() {
        ModConfig.PickerState state = ModConfig.loadPicker();
        int x = Math.max(4, UiLayout.ROOT_W - WINDOW_W - 24);
        int y = 12;
        boolean minimized = false;
        if (state != null) {
            x = state.x();
            y = state.y();
            minimized = state.minimized();
        }
        PickerWindowWidget window = new PickerWindowWidget(x, y);
        window.minimized = minimized;
        window.setSize(WINDOW_W, minimized ? HEADER_H : HEADER_H + BODY_H);
        window.rebuildBody();
        return window;
    }

    public void setPickListener(Consumer<Pick> pickListener) {
        this.pickListener = pickListener;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public int getWindowX() {
        return getSelfPositionX();
    }

    public int getWindowY() {
        return getSelfPositionY();
    }

    private void toggleMinimized() {
        minimized = !minimized;
        saveState();
        rebuildBody();
        minimizeButton.setLabel(minimized ? "+" : "-");
        minimizeButton.setHoverTooltips(List.of(Component.translatable(
                minimized ? PickerKeys.PICKER_EXPAND : PickerKeys.PICKER_MINIMIZE)));
        setSize(WINDOW_W, minimized ? HEADER_H : HEADER_H + BODY_H);
    }

    private void rebuildHeader() {
        clearAllWidgets();
        addWidget(minimizeButton);
        addWidget(modeButton);
        addWidget(searchField);
        rebuildBody();
    }

    private void rebuildBody() {
        for (Widget widget : bodyWidgets) {
            removeWidget(widget);
        }
        bodyWidgets.clear();
        if (minimized) {
            return;
        }
        boolean fluids = mode == Mode.FLUIDS;
        boolean tags = mode == Mode.TAGS;
        entries = PickerEntries.entries(tags, fluids, query);
        if (entries.isEmpty()) {
            return;
        }
        int cols = GRID_COLS;
        int rows = GRID_ROWS;
        int page = cols * rows;
        scrollMax = Math.max(0, entries.size() - page);
        scroll = Math.min(scroll, scrollMax);
        int end = Math.min(scroll + page, entries.size());
        for (int i = scroll; i < end; i++) {
            int local = i - scroll;
            int tx = PAD + (local % cols) * (TILE + GAP);
            int ty = HEADER_H + PAD + (local / cols) * (TILE + GAP);
            Widget tile;
            Pick pick = entries.get(i);
            if (pick instanceof Pick.Item) {
                tile = PickTile.item(pick, this::handlePick);
            } else if (pick instanceof Pick.Tag) {
                tile = PickTile.tag(pick, this::handlePick);
            } else {
                tile = PickTile.fluid(pick, this::handlePick);
            }
            tile.setSelfPosition(tx, ty);
            bodyWidgets.add(tile);
            addWidget(tile);
        }
        if (scrollMax > 0) {
            ScrollBarWidget scrollBar = new ScrollBarWidget(WINDOW_W - SCROLLBAR_W - PAD, HEADER_H + PAD,
                    SCROLLBAR_W, BODY_H - PAD * 2, () -> scroll, () -> scrollMax, this::scrollKnobHeight,
                    value -> {
                        scroll = value;
                        rebuildBody();
                    }, () -> scrollDragging, value -> scrollDragging = value, this::rebuildBody);
            bodyWidgets.add(scrollBar);
            addWidget(scrollBar);
        }
    }

    private int scrollKnobHeight() {
        int contentH = Math.max(1, scrollMax + 1);
        return Math.max(UiLayout.KNOB_MIN_H, (BODY_H - PAD * 2) * (BODY_H - PAD * 2) / contentH);
    }

    private void handlePick(Pick pick) {
        if (gui != null) {
            gui.getModularUIContainer().setCarried(pick.carried());
        }
        if (pickListener != null) {
            pickListener.accept(pick);
        }
    }

    private boolean overHeaderChild(double mouseX, double mouseY) {
        return minimizeButton.isMouseOverElement(mouseX, mouseY)
                || modeButton.isMouseOverElement(mouseX, mouseY)
                || searchField.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)
                && mouseY - getPositionY() < HEADER_H && !overHeaderChild(mouseX, mouseY)) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            lastPositionX = getSelfPositionX();
            lastPositionY = getSelfPositionY();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            int screenW = gui == null ? WINDOW_W : gui.getModularUIGui().width;
            int screenH = gui == null ? HEADER_H + BODY_H : gui.getModularUIGui().height;
            int rootX = gui == null ? 0 : gui.mainGroup.getPositionX();
            int rootY = gui == null ? 0 : gui.mainGroup.getPositionY();
            int nx = lastPositionX + (int) (mouseX - lastMouseX);
            int ny = lastPositionY + (int) (mouseY - lastMouseY);
            nx = Math.max(-rootX, Math.min(nx, screenW - rootX - getSizeWidth()));
            ny = Math.max(-rootY, Math.min(ny, screenH - rootY - getSizeHeight()));
            setSelfPosition(nx, ny);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            saveState();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!minimized && isMouseOverElement(mouseX, mouseY) && scrollMax > 0) {
            scroll = ScrollMath.wheel(scroll, scrollMax, GRID_COLS, wheelDelta);
            rebuildBody();
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX();
        int y = getPositionY();
        WINDOW_TEX.draw(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
        minimizeButton.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        modeButton.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        searchField.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (minimized) {
            return;
        }
        graphics.flush();
        graphics.enableScissor(x, y + HEADER_H, x + getSizeWidth(), y + HEADER_H + BODY_H);
        if (entries.isEmpty()) {
            emptyText.draw(graphics, mouseX, mouseY, x, y + HEADER_H + (BODY_H - 8) / 2, WINDOW_W, 8);
        }
        for (Widget widget : bodyWidgets) {
            widget.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
        graphics.flush();
        graphics.disableScissor();
    }

    private final class ModeButton extends Widget {
        private ResourceTexture iconTex = IconAtlas.iconTexture(iconKey(mode), ActionTone.NEUTRAL);

        private ModeButton(int x, int y, int w, int h, Mode mode) {
            super(x, y, w, h);
            setClientSideWidget();
            setHoverTooltips(List.of(Component.translatable(modeLabelKey(mode))));
        }

        private String modeLabelKey(Mode mode) {
            return switch (mode) {
                case ITEMS -> PickerKeys.PICKER_MODE_ITEMS;
                case TAGS -> PickerKeys.PICKER_MODE_TAGS;
                case FLUIDS -> PickerKeys.PICKER_MODE_FLUIDS;
            };
        }

        private static String iconKey(Mode mode) {
            return switch (mode) {
                case ITEMS -> "box";
                case TAGS -> "name_tag";
                case FLUIDS -> "droplet";
            };
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY)) {
                return false;
            }
            Mode next = button == UiColors.MOUSE_BUTTON_RIGHT
                    ? Mode.values()[(mode.ordinal() + Mode.values().length - 1) % Mode.values().length]
                    : Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
            PickerWindowWidget.this.mode = next;
            PickerWindowWidget.this.scroll = 0;
            setHoverTooltips(List.of(Component.translatable(modeLabelKey(next))));
            iconTex = IconAtlas.iconTexture(iconKey(next), ActionTone.NEUTRAL);
            rebuildBody();
            return true;
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX();
            int y = getPositionY();
            HEADER_BTN_TEX.draw(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
            if (iconTex != null) {
                iconTex.draw(graphics, mouseX, mouseY, x + (getSizeWidth() - 14) / 2, y + (getSizeHeight() - 14) / 2,
                        14, 14);
            }
            if (isMouseOverElement(mouseX, mouseY)) {
                UiGlow.drawGlow(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
            }
        }
    }

    private void saveState() {
        ModConfig.savePicker(getWindowX(), getWindowY(), minimized);
    }
}
