package com.abo47.kubejslab.client.ui.widgets;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import org.lwjgl.glfw.GLFW;


public final class OptionDropdownWidget extends WidgetGroup implements PopupProvider {
    private static final ColorRectTexture POPUP_FILL = new ColorRectTexture(UiColors.POPUP_FILL);
    private static final ColorRectTexture POPUP_BORDER = new ColorRectTexture(UiColors.BORDER_BASE);
    private static final ColorRectTexture SELECTED_FILL = new ColorRectTexture(UiColors.SURFACE_BASE);
    private static final int ROW_H = 12;

    private List<String> options = List.of();
    private String selected;
    private Consumer<String> onSelect;
    private DropdownRightClick onItemRightClick;
    private Function<String, String> labelMapper = Function.identity();
    private boolean open;
    private int scroll;
    private TextTexture selectedTex;

    public OptionDropdownWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
        setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        setClientSideWidget();
    }

    @FunctionalInterface
    public interface DropdownRightClick {
        void onRightClick(String option, double mouseX, double mouseY);
    }

    public void setOnItemRightClick(DropdownRightClick onItemRightClick) {
        this.onItemRightClick = onItemRightClick;
    }

    public void setLabelMapper(Function<String, String> labelMapper) {
        this.labelMapper = labelMapper == null ? Function.identity() : labelMapper;
        this.selectedTex = null;
    }

    public void setOptions(List<String> options) {
        this.options = List.copyOf(options.stream().filter(s -> s != null && !s.isBlank()).toList());
        this.scroll = 0;
        this.selectedTex = null;
    }

    public void setSelected(String selected) {
        if (selected == null || selected.isBlank()) {
            this.selected = null;
            this.selectedTex = null;
            return;
        }
        this.selected = selected;
        if (!options.contains(selected)) {
            List<String> merged = new ArrayList<>(options);
            merged.add(selected);
            this.options = List.copyOf(merged);
        }
        this.selectedTex = null;
    }

    public String getSelected() {
        return selected;
    }

    public void setOnSelect(Consumer<String> onSelect) {
        this.onSelect = onSelect;
    }

    public void openPopup() {
        open = !open;
        if (open) {
            scroll = 0;
            int index = selected == null ? -1 : options.indexOf(selected);
            if (index >= 0) {
                int scrollMax = Math.max(0, options.size() - 5);
                scroll = Math.min(index, scrollMax);
            }
        }
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        super.drawInBackground(g, mx, my, pt);
        drawSelected(g, mx, my);
    }

    private void drawSelected(@Nonnull GuiGraphics g, int mx, int my) {
        if (selected == null) {
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        if (selectedTex == null) {
            selectedTex = new TextTexture(labelMapper.apply(selected), UiColors.TEXT_PRIMARY)
                    .setWidth(w - 12)
                    .setType(TextTexture.TextType.LEFT_HIDE);
        }
        selectedTex.draw(g, mx, my, x + 6, y, w - 14, h);
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        if (!open) {
            return;
        }
        int visibleRows = Math.min(5, options.size());
        int popupY = y + h + 1;
        int popupH = visibleRows * ROW_H + 2;
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        POPUP_FILL.draw(g, mx, my, x, popupY, w, popupH);
        for (int row = 0; row < visibleRows; row++) {
            int index = row + scroll;
            if (index >= options.size()) {
                break;
            }
            String option = options.get(index);
            int ry = popupY + 1 + row * ROW_H;
            if (option.equals(selected)) {
                SELECTED_FILL.draw(g, mx, my, x + 1, ry, w - 2, ROW_H - 1);
            }
            TextTexture tex = new TextTexture(labelMapper.apply(option), UiColors.TEXT_PRIMARY)
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
            int visibleRows = Math.min(5, options.size());
            int popupY = y + h + 1;
            int popupH = visibleRows * ROW_H + 1;
            if (Widget.isMouseOver(x, popupY, w, popupH, mouseX, mouseY)) {
                int row = (int) ((mouseY - popupY - 1) / ROW_H) + scroll;
                if (row >= 0 && row < options.size()) {
                    open = false;
                    if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && onItemRightClick != null) {
                        onItemRightClick.onRightClick(options.get(row), mouseX, mouseY);
                    } else {
                        select(options.get(row));
                    }
                }
                return true;
            }
            open = false;
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && Widget.isMouseOver(x, y, w, h, mouseX, mouseY)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (Widget.isMouseOver(x, y, w, h, mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            openPopup();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!open) {
            return false;
        }
        int scrollMax = Math.max(0, options.size() - 5);
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
        int visibleRows = Math.min(5, options.size());
        int popupY = getPositionY() + getSizeHeight() + 1;
        int popupH = visibleRows * ROW_H + 1;
        return Widget.isMouseOver(getPositionX(), popupY, getSizeWidth(), popupH, mouseX, mouseY);
    }

    private void select(String option) {
        selected = option;
        selectedTex = null;
        open = false;
        if (onSelect != null) {
            onSelect.accept(option);
        }
    }
}