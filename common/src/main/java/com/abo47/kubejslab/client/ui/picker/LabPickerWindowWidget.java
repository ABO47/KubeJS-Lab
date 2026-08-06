package com.abo47.kubejslab.client.ui.picker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import dev.architectury.platform.Platform;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabScrollBarWidget;
import com.abo47.kubejslab.client.ui.base.LabScrollMath;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class LabPickerWindowWidget extends WidgetGroup {
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
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);
    private static final IGuiTexture HEADER_BTN_TEX =
            LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private enum Mode {
        ITEMS,
        TAGS,
        FLUIDS
    }

    private Consumer<LabPick> pickListener;
    private boolean minimized;
    private boolean dragging;
    private boolean bringToFront;
    private double lastMouseX;
    private double lastMouseY;
    private int lastPositionX;
    private int lastPositionY;
    private Mode mode = Mode.ITEMS;
    private String query = "";
    private int scroll;
    private int scrollMax;
    private boolean scrollDragging;

    private final LabActionButton minimizeButton;
    private final ModeButton modeButton;
    private final TextFieldWidget searchField;
    private final List<Widget> bodyWidgets = new ArrayList<>();
    private List<LabPick> entries = List.of();
    private final TextTexture emptyText;

    public LabPickerWindowWidget(int x, int y) {
        super(x, y, WINDOW_W, HEADER_H);
        setClientSideWidget();
        minimizeButton = new LabActionButton(PAD, DRAG_H, BUTTON_W, TILE, "-", this::toggleMinimized);
        minimizeButton.setHoverTooltips(List.of(
                Component.translatable(LabGuiKeys.LAB_PICKER_MINIMIZE)));
        modeButton = new ModeButton(PAD + TILE + GAP, DRAG_H, MODE_W, TILE, mode);
        searchField = new TextFieldWidget(SEARCH_X, DRAG_H, SEARCH_W, TILE, () -> query,
                value -> {
                    query = LabSearchNormalizer.normalizeUserSearch(value);
                    scroll = 0;
                    rebuildBody();
                });
        searchField.setClientSideWidget();
        searchField.setMaxStringLength(Integer.MAX_VALUE);
        searchField.setValidator(LabSearchNormalizer::normalizeUserSearch);
        searchField.setBordered(false);
        searchField.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        searchField.setTextColor(LabColors.TEXT_PRIMARY);
        searchField.setCurrentString(query);
        emptyText = new TextTexture(Component.translatable(LabGuiKeys.LAB_PICKER_EMPTY).getString(),
                LabColors.TEXT_MUTED).setWidth(WINDOW_W).setType(TextTexture.TextType.NORMAL);
        rebuildHeader();
    }

    public static LabPickerWindowWidget create() {
        State state = loadState();
        LabPickerWindowWidget window = new LabPickerWindowWidget(state.x(), state.y());
        window.minimized = state.minimized();
        window.setSize(WINDOW_W, state.minimized() ? HEADER_H : HEADER_H + BODY_H);
        window.rebuildBody();
        return window;
    }

    public void setPickListener(Consumer<LabPick> pickListener) {
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
                minimized ? LabGuiKeys.LAB_PICKER_EXPAND : LabGuiKeys.LAB_PICKER_MINIMIZE)));
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
        entries = LabPickerEntries.entries(tags, fluids, query);
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
            LabPick pick = entries.get(i);
            if (pick instanceof LabPick.Item) {
                tile = LabPickTile.item(pick, this::handlePick);
            } else if (pick instanceof LabPick.Tag) {
                tile = LabPickTile.tag(pick, this::handlePick);
            } else {
                tile = LabPickTile.fluid(pick, this::handlePick);
            }
            tile.setSelfPosition(tx, ty);
            bodyWidgets.add(tile);
            addWidget(tile);
        }
        if (scrollMax > 0) {
            LabScrollBarWidget scrollBar = new LabScrollBarWidget(WINDOW_W - SCROLLBAR_W - PAD, HEADER_H + PAD,
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
        return Math.max(LabLayout.KNOB_MIN_H, (BODY_H - PAD * 2) * (BODY_H - PAD * 2) / contentH);
    }

    private void handlePick(LabPick pick) {
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
            bringToFront = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            int screenW = gui == null ? WINDOW_W : gui.getModularUIGui().width;
            int screenH = gui == null ? HEADER_H + BODY_H : gui.getModularUIGui().height;
            int nx = lastPositionX + (int) (mouseX - lastMouseX);
            int ny = lastPositionY + (int) (mouseY - lastMouseY);
            nx = Math.max(0, Math.min(nx, screenW - getSizeWidth()));
            ny = Math.max(0, Math.min(ny, screenH - getSizeHeight()));
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
            scroll = LabScrollMath.wheel(scroll, scrollMax, GRID_COLS, wheelDelta);
            rebuildBody();
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (bringToFront && gui != null) {
            bringToFront = false;
            WidgetGroup parent = gui.mainGroup;
            if (parent != null && parent.getContainedWidgets(false).contains(this)) {
                parent.removeWidget(this);
                parent.addWidget(this);
            }
        }
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
        private final TextTexture letterTex =
                new TextTexture("I", LabColors.TEXT_PRIMARY).setWidth(MODE_W).setType(TextTexture.TextType.NORMAL);

        private ModeButton(int x, int y, int w, int h, Mode mode) {
            super(x, y, w, h);
            setClientSideWidget();
            setHoverTooltips(List.of(Component.translatable(modeLabelKey(mode))));
        }

        private String modeLabelKey(Mode mode) {
            return switch (mode) {
                case ITEMS -> LabGuiKeys.LAB_PICKER_MODE_ITEMS;
                case TAGS -> LabGuiKeys.LAB_PICKER_MODE_TAGS;
                case FLUIDS -> LabGuiKeys.LAB_PICKER_MODE_FLUIDS;
            };
        }

        private String modeLetter(Mode mode) {
            return switch (mode) {
                case ITEMS -> "I";
                case TAGS -> "T";
                case FLUIDS -> "F";
            };
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY)) {
                return false;
            }
            Mode next = button == LabColors.MOUSE_BUTTON_RIGHT
                    ? Mode.values()[(mode.ordinal() + Mode.values().length - 1) % Mode.values().length]
                    : Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
            LabPickerWindowWidget.this.mode = next;
            LabPickerWindowWidget.this.scroll = 0;
            setHoverTooltips(List.of(Component.translatable(modeLabelKey(next))));
            letterTex.updateText(modeLetter(next));
            rebuildBody();
            return true;
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX();
            int y = getPositionY();
            HEADER_BTN_TEX.draw(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
            letterTex.draw(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
            if (isMouseOverElement(mouseX, mouseY)) {
                LabGlow.drawGlow(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
            }
        }
    }

    private record State(int x, int y, boolean minimized) {
    }

    private static Path stateFile() {
        return Platform.getConfigFolder().resolve("kubejslab_picker.json");
    }

    private static State loadState() {
        Path file = stateFile();
        if (Files.exists(file)) {
            try {
                State state = GSON.fromJson(Files.readString(file), State.class);
                if (state != null) {
                    return state;
                }
            } catch (IOException | RuntimeException ignored) {
            }
        }
        return new State(Minecraft.getInstance().getWindow().getGuiScaledWidth() - WINDOW_W - 24, 12, false);
    }

    private void saveState() {
        try {
            Path file = stateFile();
            Files.writeString(file, GSON.toJson(new State(getWindowX(), getWindowY(), minimized)));
        } catch (IOException ignored) {
        }
    }
}
