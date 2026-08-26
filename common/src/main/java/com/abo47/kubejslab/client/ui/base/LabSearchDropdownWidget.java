package com.abo47.kubejslab.client.ui.base;

import java.util.function.Consumer;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import org.lwjgl.glfw.GLFW;


public final class LabSearchDropdownWidget extends WidgetGroup implements LabPopupProvider {
    private static final ColorRectTexture POPUP_FILL = new ColorRectTexture(LabColors.POPUP_FILL);
    private static final ColorRectTexture POPUP_BORDER = new ColorRectTexture(LabColors.BORDER_BASE);
    private static final ColorRectTexture SELECTED_FILL = new ColorRectTexture(LabColors.SURFACE_BASE);
    private static final int ROW_H = 12;

    private final TextFieldWidget field;
    private List<String> options = List.of();
    private Consumer<String> onSelect;
    private boolean open;
    private int scroll;

    public LabSearchDropdownWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
        field = new LabTextFieldWidget(0, 0, w, h, null, text -> {
        });
        field.setClientSideWidget();
        field.setMaxStringLength(40);
        field.setBordered(false);
        field.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        field.setTextColor(LabColors.TEXT_PRIMARY);
        addWidget(field);
    }

    public void setOptions(List<String> options) {
        this.options = List.copyOf(options.stream().filter(s -> s != null && !s.isBlank()).toList());
        this.scroll = 0;
    }

    public void setSelected(String selected) {
        field.setCurrentString(selected == null ? "" : selected);
        scroll = 0;
    }

    public String getSelected() {
        return field.getCurrentString();
    }

    public void setOnSelect(Consumer<String> onSelect) {
        this.onSelect = onSelect;
    }

    private List<String> filtered() {
        String query = field.getCurrentString().toLowerCase();
        if (query.isEmpty()) {
            return options;
        }
        return options.stream().filter(o -> o.toLowerCase().contains(query)).toList();
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        if (!open) {
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        List<String> filtered = filtered();
        int visibleRows = Math.min(5, filtered.size());
        int popupY = y + h + 1;
        int popupH = visibleRows * ROW_H + 2;
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        POPUP_FILL.draw(g, mx, my, x, popupY, w, popupH);
        for (int row = 0; row < visibleRows; row++) {
            int index = row + scroll;
            if (index >= filtered.size()) {
                break;
            }
            String option = filtered.get(index);
            int ry = popupY + 1 + row * ROW_H;
            if (option.equals(field.getCurrentString())) {
                SELECTED_FILL.draw(g, mx, my, x + 1, ry, w - 2, ROW_H - 1);
            }
            TextTexture tex = new TextTexture(option, LabColors.TEXT_PRIMARY)
                    .setWidth(w - 8)
                    .setType(TextTexture.TextType.LEFT_HIDE);
            tex.draw(g, mx, my, x + 4, ry, w - 8, ROW_H);
        }
        POPUP_BORDER.draw(g, mx, my, x, popupY, w, 1);
        POPUP_BORDER.draw(g, mx, my, x, popupY + popupH - 1, w, 1);
        POPUP_BORDER.draw(g, mx, my, x, popupY, 1, popupH);
        POPUP_BORDER.draw(g, mx, my, x + w - 1, popupY, 1, popupH);
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        if (open) {
            List<String> filtered = filtered();
            int visibleRows = Math.min(5, filtered.size());
            int popupY = y + h + 1;
            int popupH = visibleRows * ROW_H + 1;
            if (Widget.isMouseOver(x, popupY, w, popupH, mouseX, mouseY)) {
                int row = (int) ((mouseY - popupY - 1) / ROW_H) + scroll;
                if (row >= 0 && row < filtered.size()) {
                    select(filtered.get(row));
                    return true;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    return true;
                }
            }
        }
        if (Widget.isMouseOver(x, y, w, h, mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            open = true;
            scroll = 0;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!open) {
            return false;
        }
        int scrollMax = Math.max(0, filtered().size() - 5);
        int next = Math.max(0, Math.min(scrollMax, scroll + (wheelDelta > 0 ? -1 : 1)));
        if (next != scroll) {
            scroll = next;
        }
        return true;
    }

    public boolean isOpen() {
        return open;
    }

    public void closePopup() {
        open = false;
    }

    public boolean isPopupOver(double mouseX, double mouseY) {
        if (!open) {
            return false;
        }
        int visibleRows = Math.min(5, filtered().size());
        int popupY = getPositionY() + getSizeHeight() + 1;
        int popupH = visibleRows * ROW_H + 1;
        return Widget.isMouseOver(getPositionX(), popupY, getSizeWidth(), popupH, mouseX, mouseY);
    }

    private void select(String option) {
        field.setCurrentString(option);
        open = false;
        scroll = 0;
        if (onSelect != null) {
            onSelect.accept(option);
        }
    }
}