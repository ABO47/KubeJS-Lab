package com.abo47.kubejslab.client.ui.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import org.joml.Vector4f;


public abstract class LabRowCardSettingsWidget extends WidgetGroup {
    public static final IGuiTexture CARD_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);
    public static final int ROW_STRIDE = LabLayout.CARD_H + 4;
    public static final int FIELD_H = 15;
    public static final int CONTROL_W = 44;
    private static final int ROW_GAP = 4;

    private final List<LabPopupProvider> popupDropdowns = new ArrayList<>();
    private final LabScrollBarWidget scrollBar;
    private final LabActionButton clearButton;
    private final LabActionButton saveButton;
    private final Set<Widget> disabledControls = new HashSet<>();
    private static final IGuiTexture DISABLED_OVERLAY =
            new ColorRectTexture(LabColors.withAlpha(LabColors.ERROR, 56));

    private List<FieldRow> rows = List.of();
    private int scrollOffset;
    private int scrollMax;
    private boolean dragging;
    private Runnable onClear;
    private Runnable onSave;

    protected LabRowCardSettingsWidget(int x, int y, int w, int h, String clearLabel, String saveLabel) {
        super(x, y, w, h);

        int pad = LabLayout.SETTINGS_PAD;
        int cardW = w - pad * 2;
        int btnH = LabLayout.SETTINGS_BTN_H;
        int bottomY = h - pad - btnH;
        int btnW = (cardW - LabLayout.SETTINGS_BTN_GAP) / 2;

        clearButton = new LabActionButton(LabLayout.SETTINGS_PAD, bottomY, btnW, btnH, clearLabel, () -> {
            if (onClear != null) onClear.run();
        });
        addWidget(clearButton);

        saveButton = new LabActionButton(LabLayout.SETTINGS_PAD + btnW + LabLayout.SETTINGS_BTN_GAP, bottomY, btnW, btnH,
                saveLabel, () -> {
            if (onSave != null) onSave.run();
        });
        addWidget(saveButton);

        scrollBar = new LabScrollBarWidget(
                w - LabLayout.SCROLLBAR_W - 2, 0, LabLayout.SCROLLBAR_W, bottomY - ROW_GAP,
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
            row.label().setColor(row.disabled() ? LabColors.ERROR : LabColors.TEXT_PRIMARY);
            if (row.disabled() && row.control() != null) {
                disabledControls.add(row.control());
            }
        }
        recomputeScrollMax();
        relayoutFields();
    }

    protected void addPopupDropdown(LabPopupProvider dropdown) {
        popupDropdowns.add(dropdown);
    }

    public void closeAllPopups() {
        for (LabPopupProvider dropdown : popupDropdowns) {
            dropdown.closePopup();
        }
    }

    protected void resetScroll() {
        scrollOffset = 0;
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
        int pad = LabLayout.SETTINGS_PAD;
        int cardX = x + pad;
        int cardW = contentCardW(w);
        int bottomY = h - pad - LabLayout.SETTINGS_BTN_H;
        int contentBottom = y + bottomY - ROW_GAP;

        g.flush();
        scissorRect(g, x + 1, y, x + w - 1, contentBottom);
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            int rowY = rowY(i) - scrollOffset;
            int cardY = y + rowY;
            if (cardY + LabLayout.CARD_H < y || cardY > contentBottom) {
                continue;
            }
            drawCard(g, mx, my, cardX, y, cardW, rowY, row.label,
                    controlWidth(row.control), row.icon);
            if (row.control != null) {
                g.flush();
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                scissorRect(g, cardX, cardY, cardX + cardW, cardY + LabLayout.CARD_H);
                row.control.drawInBackground(g, mx, my, pt);
                if (row.disabled()) {
                    DISABLED_OVERLAY.draw(g, mx, my, cardX + 1, cardY + 1, cardW - 2, LabLayout.CARD_H - 2);
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
        for (LabPopupProvider dropdown : popupDropdowns) {
            if (dropdown.isOpen()) {
                anyPopupOpen = true;
                break;
            }
        }
        if (anyPopupOpen) {
            for (LabPopupProvider dropdown : popupDropdowns) {
                if (dropdown.isOpen() && dropdown instanceof Widget widget) {
                    widget.drawInForeground(g, mx, my, pt);
                }
            }
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        int bottomY = getSizeHeight() - LabLayout.SETTINGS_PAD - LabLayout.SETTINGS_BTN_H;
        scissorRect(g, x + 1, y, x + getSizeWidth() - 1, y + bottomY - ROW_GAP);
        super.drawInForeground(g, mx, my, pt);
        g.disableScissor();
    }

    private static void scissorRect(GuiGraphics g, int x1, int y1, int x2, int y2) {
        var trans = g.pose().last().pose();
        var realPos = new Vector4f(x1, y1, 0, 1);
        var realPos2 = new Vector4f(x2, y2, 0, 1);
        trans.transform(realPos);
        trans.transform(realPos2);
        g.enableScissor((int) realPos.x, (int) realPos.y, (int) realPos2.x, (int) realPos2.y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (LabPopupProvider dropdown : popupDropdowns) {
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
        if (!isInsideViewport(mouseX, mouseY)) {
            return false;
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) {
                continue;
            }
            if (widget.isVisible() && widget.isActive() && childInsideViewport(widget)
                    && widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        for (LabPopupProvider dropdown : popupDropdowns) {
            if (dropdown.isOpen() && dropdown.isPopupOver(mouseX, mouseY)) {
                return dropdown.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (disabledControls.contains(widget)) {
                continue;
            }
            if (widget.isVisible() && widget.isActive() && childInsideViewport(widget)
                    && widget.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
        }
        if (isMouseOverElement(mouseX, mouseY) && scrollMax > 0) {
            int step = Math.max(8, ROW_STRIDE / 3);
            scrollOffset = LabScrollMath.wheel(scrollOffset, scrollMax, step, wheelDelta);
            relayoutFields();
            return true;
        }
        return false;
    }

    private boolean isInsideViewport(double mouseX, double mouseY) {
        return mouseY >= getPositionY() && mouseY <= viewportBottom();
    }

    private boolean childInsideViewport(Widget child) {
        int top = getPositionY();
        int bottom = viewportBottom();
        return child.getPositionY() < bottom && child.getPositionY() + child.getSizeHeight() > top;
    }

    private int viewportBottom() {
        return getPositionY() + getSizeHeight() - LabLayout.SETTINGS_PAD - LabLayout.SETTINGS_BTN_H - ROW_GAP;
    }

    private void recomputeScrollMax() {
        int pad = LabLayout.SETTINGS_PAD;
        int bottomY = getSizeHeight() - pad - LabLayout.SETTINGS_BTN_H;
        int viewport = Math.max(1, bottomY - ROW_GAP);
        int contentH = rows.size() * ROW_STRIDE;
        scrollMax = Math.max(0, contentH - viewport);
        scrollOffset = Math.min(scrollOffset, scrollMax);
        scrollBar.setVisible(scrollMax > 0);
    }

    private int scrollKnobHeight() {
        int pad = LabLayout.SETTINGS_PAD;
        int bottomY = getSizeHeight() - pad - LabLayout.SETTINGS_BTN_H;
        int viewport = Math.max(1, bottomY - ROW_GAP);
        int contentH = Math.max(1, rows.size() * ROW_STRIDE);
        return Math.max(LabLayout.KNOB_MIN_H, viewport * viewport / contentH);
    }

    private int controlWidth(Widget control) {
        return CONTROL_W;
    }

    private int contentCardW(int panelW) {
        int pad = LabLayout.SETTINGS_PAD;
        return panelW - pad * 2 - (scrollMax > 0 ? LabLayout.SCROLLBAR_W + 2 : 0);
    }

    private int controlX(int cardX, int cardW, int pad, FieldRow row) {
        return cardX + cardW - pad - CONTROL_W - 4;
    }

    private void relayoutFields() {
        int pad = LabLayout.SETTINGS_PAD;
        int cardX = pad;
        int cardW = contentCardW(getSizeWidth());
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            int rowY = rowY(i) - scrollOffset;
            int y = rowY + (LabLayout.CARD_H - row.control.getSizeHeight()) / 2;
            row.control.setSelfPosition(new Position(controlX(cardX, cardW, pad, row), y));
        }
    }

    private void drawCard(GuiGraphics g, int mx, int my, int cardX, int panelY, int cardW,
            int rowY, TextTexture label, int controlW, ItemStackTexture icon) {
        int cardY = panelY + rowY;
        CARD_TEXTURE.draw(g, mx, my, cardX, cardY, cardW, LabLayout.CARD_H);
        int pad = LabLayout.SETTINGS_PAD;
        int iconW = icon == null ? 0 : 16 + 4;
        int labelW = cardW - pad * 2 - controlW - 4 - iconW;
        if (icon != null) {
            icon.draw(g, mx, my, cardX + pad, cardY + (LabLayout.CARD_H - 16) / 2, 16, 16);
        }
        label.draw(g, mx, my, cardX + pad + iconW, cardY, labelW, LabLayout.CARD_H);
    }

    private static int rowY(int row) {
        return row * ROW_STRIDE;
    }

    protected static TextTexture rowLabel(String key, int width) {
        return new TextTexture(Component.translatable(key).getString(), LabColors.TEXT_PRIMARY)
                .setWidth(width)
                .setType(TextTexture.TextType.ROLL);
    }

    protected static TextFieldWidget numberField(int x, int y, java.util.function.Supplier<String> supplier,
            java.util.function.Consumer<String> responder, String initial) {
        return numberField(x, y, supplier, responder, initial, 6);
    }

    protected static TextFieldWidget numberField(int x, int y, java.util.function.Supplier<String> supplier,
            java.util.function.Consumer<String> responder, String initial, int maxLength) {
        TextFieldWidget field = LabNumberFieldWidget.create(x, y, CONTROL_W, FIELD_H, supplier, responder);
        configureField(field, initial, maxLength);
        return field;
    }

    private static void configureField(TextFieldWidget field, String initial, int maxLength) {
        field.setClientSideWidget();
        field.setMaxStringLength(maxLength);
        field.setBordered(false);
        field.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        field.setTextColor(LabColors.TEXT_PRIMARY);
        field.setCurrentString(initial);
    }

    protected static void configureCommit(LabCommitFieldWidget field) {
        field.setClientSideWidget();
        field.setMaxStringLength(256);
        field.setBordered(false);
        field.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        field.setTextColor(LabColors.TEXT_PRIMARY);
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

    protected static String formatFloat(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return Float.toString(value);
    }

    public record FieldRow(TextTexture label, Widget control, ItemStackTexture icon, boolean disabled) {
        public FieldRow(TextTexture label, Widget control, ItemStackTexture icon) {
            this(label, control, icon, false);
        }
    }
}