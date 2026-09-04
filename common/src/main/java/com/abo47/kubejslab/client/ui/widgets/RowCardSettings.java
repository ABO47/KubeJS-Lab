package com.abo47.kubejslab.client.ui.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.mojang.blaze3d.systems.RenderSystem;


public abstract class RowCardSettings extends WidgetGroup {
    public static final IGuiTexture CARD_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_PANEL_ALT, UiColors.BORDER_BASE);
    public static final int FIELD_H = 15;
    public static final int CONTROL_W = 44;

    private final List<PopupProvider> popupDropdowns = new ArrayList<>();
    private final ScrollBarWidget scrollBar;
    private final ActionButton clearButton;
    private final ActionButton saveButton;
    private final Set<Widget> disabledControls = new HashSet<>();
    private static final IGuiTexture DISABLED_OVERLAY =
            new ColorRectTexture(UiColors.withAlpha(UiColors.ERROR, 56));

    private List<FieldRow> rows = List.of();
    private int scrollOffset;
    private int scrollMax;
    private boolean dragging;
    private Runnable onClear;
    private Runnable onSave;

    protected RowCardSettings(int x, int y, int w, int h, String clearLabel, String saveLabel) {
        super(x, y, w, h);

        int pad = UiLayout.SETTINGS_PAD;
        int cardW = w - pad * 2;
        int btnH = UiLayout.SETTINGS_BTN_H;
        int bottomY = h - pad - btnH;
        int btnW = (cardW - UiLayout.SETTINGS_BTN_GAP) / 2;

        clearButton = new ActionButton(UiLayout.SETTINGS_PAD, bottomY, btnW, btnH, clearLabel, () -> {
            if (onClear != null) onClear.run();
        });
        addWidget(clearButton);

        saveButton = new ActionButton(UiLayout.SETTINGS_PAD + btnW + UiLayout.SETTINGS_BTN_GAP, bottomY, btnW, btnH,
                saveLabel, () -> {
            if (onSave != null) onSave.run();
        });
        addWidget(saveButton);

        scrollBar = new ScrollBarWidget(
                w - UiLayout.SCROLLBAR_W - 2, 0, UiLayout.SCROLLBAR_W, bottomY - FieldRowLayout.ROW_GAP,
                () -> scrollOffset,
                () -> scrollMax,
                this::scrollKnobHeight,
                value -> {
                    scrollOffset = value;
                    relayoutFields();
                },
                () -> dragging,
                value -> dragging = value,
                this::relayoutFields);
        addWidget(scrollBar);
        scrollBar.setVisible(false);
    }

    protected void setRows(List<FieldRow> rows) {
        this.rows = rows;
        disabledControls.clear();
        for (FieldRow row : rows) {
            row.label().setColor(row.disabled() ? UiColors.ERROR : UiColors.TEXT_PRIMARY);
            if (row.control() != null) {
                if (row.disabled()) {
                    disabledControls.add(row.control());
                    row.control().setActive(false);
                    if (row.control().isFocus()) row.control().setFocus(false);
                    if (row.control() instanceof PopupProvider popup && popup.isOpen()) popup.closePopup();
                    if (row.control() instanceof com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget tf) tf.setFocus(false);
                } else {
                    row.control().setActive(true);
                }
            }
        }
        recomputeScrollMax();
        relayoutFields();
    }

    protected void addPopupDropdown(PopupProvider dropdown) {
        popupDropdowns.add(dropdown);
    }

    protected void removePopupDropdown(PopupProvider dropdown) {
        popupDropdowns.remove(dropdown);
    }

    public void closeAllPopups() {
        for (PopupProvider dropdown : popupDropdowns) {
            dropdown.closePopup();
        }
    }

    public void resetScroll() {
        scrollOffset = 0;
        relayoutFields();
    }

    public void setOnClear(Runnable onClear) {
        this.onClear = onClear;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        int pad = UiLayout.SETTINGS_PAD;
        int cardX = x + pad;
        int cardW = FieldRowLayout.contentCardW(w, scrollMax);
        int bottomY = h - pad - UiLayout.SETTINGS_BTN_H;
        int contentBottom = y + bottomY - FieldRowLayout.ROW_GAP;

        g.flush();
        FieldRowLayout.scissorRect(g, x + 1, y, x + w - 1, contentBottom);
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            int rowY = FieldRowLayout.rowY(i) - scrollOffset;
            int cardY = y + rowY;
            if (cardY + UiLayout.CARD_H < y || cardY > contentBottom) {
                continue;
            }
            FieldRowLayout.drawCard(g, mx, my, cardX, y, cardW, rowY, row.label(),
                    CONTROL_W, row.icon());
            if (row.control() != null) {
                g.flush();
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                FieldRowLayout.scissorRect(g, cardX, cardY, cardX + cardW, cardY + UiLayout.CARD_H);
                row.control().drawInBackground(g, mx, my, pt);
                if (row.disabled()) {
                    DISABLED_OVERLAY.draw(g, mx, my, cardX + 1, cardY + 1, cardW - 2, UiLayout.CARD_H - 2);
                }
                g.disableScissor();
                g.flush();
            }
        }
        g.disableScissor();

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (scrollBar.isVisible()) {
            scrollBar.drawInBackground(g, mx, my, pt);
        }
        clearButton.drawInBackground(g, mx, my, pt);
        saveButton.drawInBackground(g, mx, my, pt);
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        boolean anyPopupOpen = false;
        for (PopupProvider dropdown : popupDropdowns) {
            if (dropdown.isOpen()) {
                anyPopupOpen = true;
                break;
            }
        }
        if (anyPopupOpen) {
            for (PopupProvider dropdown : popupDropdowns) {
                if (dropdown.isOpen() && dropdown instanceof Widget widget) {
                    widget.drawInForeground(g, mx, my, pt);
                }
            }
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        int bottomY = getSizeHeight() - UiLayout.SETTINGS_PAD - UiLayout.SETTINGS_BTN_H;
        FieldRowLayout.scissorRect(g, x + 1, y, x + getSizeWidth() - 1, y + bottomY - FieldRowLayout.ROW_GAP);
        super.drawInForeground(g, mx, my, pt);
        g.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (PopupProvider dropdown : popupDropdowns) {
            if (dropdown.isOpen() && dropdown.isPopupOver(mouseX, mouseY)) {
                return dropdown.mouseClicked(mouseX, mouseY, button);
            }
        }
        if (clearButton.isMouseOverElement(mouseX, mouseY)) {
            return clearButton.mouseClicked(mouseX, mouseY, button);
        }
        if (saveButton.isMouseOverElement(mouseX, mouseY)) {
            return saveButton.mouseClicked(mouseX, mouseY, button);
        }
        if (!FieldRowLayout.isInsideViewport(mouseY, getPositionY(), getSizeHeight())) {
            return false;
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) {
                continue;
            }
            if (widget.isVisible() && widget.isActive() && FieldRowLayout.childInsideViewport(widget,
                    getPositionY(), getSizeHeight())
                    && widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        for (PopupProvider dropdown : popupDropdowns) {
            if (dropdown.isOpen() && dropdown.isPopupOver(mouseX, mouseY)) {
                return dropdown.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) {
                continue;
            }
            if (widget.isVisible() && widget.isActive() && widget.isMouseOverElement(mouseX, mouseY)
                    && widget.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) {
                continue;
            }
            if (widget.isVisible() && widget.isActive() && FieldRowLayout.childInsideViewport(widget,
                    getPositionY(), getSizeHeight())
                    && widget.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
        }
        if (isMouseOverElement(mouseX, mouseY) && scrollMax > 0) {
            int step = Math.max(8, FieldRowLayout.ROW_STRIDE / 3);
            scrollOffset = ScrollMath.wheel(scrollOffset, scrollMax, step, wheelDelta);
            relayoutFields();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget w : disabledControls) {
            if (w.isFocus()) return true;
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) continue;
            if (widget.isVisible() && widget.isActive() && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (Widget w : disabledControls) {
            if (w.isFocus()) return true;
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) continue;
            if (widget.isVisible() && widget.isActive() && widget.charTyped(codePoint, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) continue;
            if (widget.isVisible() && widget.isActive() && widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void recomputeScrollMax() {
        scrollMax = FieldRowLayout.maxScroll(getSizeHeight(), rows.size());
        scrollOffset = Math.min(scrollOffset, scrollMax);
        scrollBar.setVisible(scrollMax > 0);
    }

    private int scrollKnobHeight() {
        return FieldRowLayout.knobHeight(getSizeHeight(), rows.size());
    }

    private void relayoutFields() {
        int pad = UiLayout.SETTINGS_PAD;
        int cardX = pad;
        int cardW = FieldRowLayout.contentCardW(getSizeWidth(), scrollMax);
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            int rowY = FieldRowLayout.rowY(i) - scrollOffset;
            int y = rowY + (UiLayout.CARD_H - row.control().getSizeHeight()) / 2;
            row.control().setSelfPosition(new Position(FieldRowLayout.controlX(cardX, cardW, pad), y));
        }
    }

    protected static TextTexture rowLabel(String key, int width) {
        return new TextTexture(Component.translatable(key).getString(), UiColors.TEXT_PRIMARY)
                .setWidth(width)
                .setType(TextTexture.TextType.ROLL);
    }

    protected static TextFieldWidget numberField(int x, int y, java.util.function.Supplier<String> supplier,
            java.util.function.Consumer<String> responder, String initial) {
        return numberField(x, y, supplier, responder, initial, 6);
    }

    protected static TextFieldWidget numberField(int x, int y, java.util.function.Supplier<String> supplier,
            java.util.function.Consumer<String> responder, String initial, int maxLength) {
        TextFieldWidget field = NumberField.create(x, y, CONTROL_W, FIELD_H, supplier, responder);
        configureField(field, initial, maxLength);
        return field;
    }

    private static void configureField(TextFieldWidget field, String initial, int maxLength) {
        field.setClientSideWidget();
        field.setMaxStringLength(maxLength);
        field.setBordered(false);
        field.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        field.setTextColor(UiColors.TEXT_PRIMARY);
        field.setCurrentString(initial);
    }

    protected static void configureCommit(CommitField field) {
        field.setClientSideWidget();
        field.setMaxStringLength(256);
        field.setBordered(false);
        field.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        field.setTextColor(UiColors.TEXT_PRIMARY);
        field.setCurrentString("");
    }

    protected static float clampChance(float chance) {
        return Math.max(0f, Math.min(1f, chance));
    }

    protected static float parseFloat(String text, float fallback) {
        try {
            return Float.parseFloat(text.replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    protected static int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}