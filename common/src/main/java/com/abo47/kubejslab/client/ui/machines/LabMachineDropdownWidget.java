package com.abo47.kubejslab.client.ui.machines;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabScrollMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class LabMachineDropdownWidget extends WidgetGroup {
    private static final ColorRectTexture SELECTED_FILL = new ColorRectTexture(LabColors.SURFACE_BASE);
    private static final ColorRectTexture POPUP_FILL = new ColorRectTexture(0xFF0D1114);
    private static final ColorRectTexture POPUP_BORDER = new ColorRectTexture(LabColors.BORDER_BASE);

    private final TextFieldWidget searchField;
    private List<LabMachine> machines = List.of();
    private LabMachine selected;
    private String filterText = "";
    private boolean open;
    private int scroll;
    private Consumer<LabMachine> onMachineChanged;
    private TextTexture selectedDisplayTex;
    private String cachedFilterText = "";
    private List<LabMachine> cachedFiltered = List.of();
    private List<LabMachine> cachedVisible = List.of();
    private List<RowTextures> cachedRowTextures = List.of();

    public LabMachineDropdownWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
        setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        searchField = new TextFieldWidget(0, 0, w, h, (Supplier<String>) null, this::onFilterChanged) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean handled = super.mouseClicked(mouseX, mouseY, button);
                if (button == LabColors.MOUSE_BUTTON_LEFT && isMouseOverElement(mouseX, mouseY)) {
                    openPopup();
                }
                return handled;
            }
        };
        searchField.setBordered(false);
        searchField.setBackground(IGuiTexture.EMPTY);
        searchField.setTextColor(LabColors.TEXT_PRIMARY);
        searchField.setMaxStringLength(64);
        addWidget(searchField);
    }

    public void setOnMachineChanged(Consumer<LabMachine> onMachineChanged) {
        this.onMachineChanged = onMachineChanged;
    }

    public LabMachine getSelectedMachine() {
        ensureMachines();
        return selected;
    }

    public void refreshSelection() {
        if (selected != null) {
            return;
        }
        ensureMachines();
        if (selected != null && onMachineChanged != null) {
            onMachineChanged.accept(selected);
        }
    }

    public void selectMachineByUid(ResourceLocation uid) {
        if (uid == null) {
            return;
        }
        ensureMachines();
        for (LabMachine machine : machines) {
            if (machine.recipeTypeUid().equals(uid)) {
                select(machine);
                return;
            }
        }
    }

    private void ensureMachines() {
        List<LabMachine> current = new ArrayList<>();
        for (LabMachine machine : LabMachineCatalog.machines()) {
            if (machine.supported()) {
                current.add(machine);
            }
        }
        if (current.isEmpty()) {
            return;
        }
        if (!current.equals(machines)) {
            ResourceLocation selectedUid = selected == null ? null : selected.recipeTypeUid();
            machines = current;
            scroll = 0;
            selectedDisplayTex = null;
            selected = null;
            if (selectedUid != null) {
                for (LabMachine machine : machines) {
                    if (machine.recipeTypeUid().equals(selectedUid)) {
                        selected = machine;
                        break;
                    }
                }
            }
            if (selected == null) {
                selected = machines.get(0);
            }
        }
    }

    private List<LabMachine> filtered() {
        if (filterText.isBlank()) {
            return machines;
        }
        if (filterText.equals(cachedFilterText)) {
            return cachedFiltered;
        }
        cachedFilterText = filterText;
        List<LabMachine> result = new ArrayList<>();
        for (LabMachine machine : machines) {
            if (machine.name().toLowerCase(Locale.ROOT).contains(filterText)) {
                result.add(machine);
            }
        }
        cachedFiltered = result;
        return result;
    }

    private void onFilterChanged(String text) {
        filterText = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        scroll = 0;
        if (!filterText.isEmpty()) {
            open = !filtered().isEmpty();
        }
    }

    private void openPopup() {
        ensureMachines();
        scroll = 0;
        if (selected != null) {
            int index = filtered().indexOf(selected);
            if (index >= 0) {
                int scrollMax = Math.max(0, filtered().size() - LabLayout.DROPDOWN_MAX_ROWS);
                scroll = Math.min(index, scrollMax);
            }
        }
        open = !machines.isEmpty();
    }

    private void select(LabMachine machine) {
        selected = machine;
        selectedDisplayTex = null;
        searchField.setCurrentString("");
        filterText = "";
        scroll = 0;
        open = false;
        if (onMachineChanged != null) {
            onMachineChanged.accept(machine);
        }
    }

    private void ensureRowTextures(List<LabMachine> visible) {
        if (visible.equals(cachedVisible)) {
            return;
        }
        cachedVisible = visible;
        cachedRowTextures = new ArrayList<>(visible.size());
        int w = getSizeWidth();
        for (LabMachine machine : visible) {
            cachedRowTextures.add(new RowTextures(
                    new ItemStackTexture(machine.icon()),
                    new TextTexture(displayName(machine), LabColors.TEXT_PRIMARY)
                            .setWidth(w - 22)
                            .setType(TextTexture.TextType.LEFT_HIDE),
                    new TextTexture(displayName(machine), LabColors.TEXT_MUTED)
                            .setWidth(w - 22)
                            .setType(TextTexture.TextType.LEFT_HIDE)));
        }
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        if (selected != null && searchField.getRawCurrentString().isEmpty() && !searchField.isFocus()) {
            if (selectedDisplayTex == null) {
                selectedDisplayTex = new TextTexture(displayName(selected), LabColors.TEXT_MUTED)
                        .setWidth(w - 12)
                        .setType(TextTexture.TextType.LEFT_HIDE);
            }
            selectedDisplayTex.draw(g, mx, my, x + 6, y, w - 12, h);
        }
        if (!open) {
            return;
        }
        List<LabMachine> visible = filtered();
        if (visible.isEmpty()) {
            return;
        }
        int rowH = LabLayout.DROPDOWN_ROW_H;
        int visibleRows = Math.min(LabLayout.DROPDOWN_MAX_ROWS, visible.size());
        int popupY = y + h + 1;
        int popupH = visibleRows * rowH;

        var pose = g.pose();
        pose.pushPose();
        pose.translate(0, 0, 200);

        POPUP_FILL.draw(g, mx, my, x, popupY, w, popupH);

        ensureRowTextures(visible);
        for (int row = 0; row < visibleRows; row++) {
            int index = row + scroll;
            if (index >= visible.size()) {
                break;
            }
            LabMachine machine = visible.get(index);
            RowTextures tex = cachedRowTextures.get(index);
            int ry = popupY + 1 + row * rowH;
            if (machine == selected) {
                SELECTED_FILL.draw(g, mx, my, x + 1, ry, w - 2, rowH - 1);
            } else if (Widget.isMouseOver(x + 1, ry, w - 2, rowH - 1, mx, my)) {
                LabGlow.drawGlow(g, mx, my, x + 1, ry, w - 2, rowH - 1);
            }
            tex.icon.draw(g, mx, my, x + 3, ry + 1, 15, 15);
            (machine == selected ? tex.nameSelected : tex.nameNormal)
                    .draw(g, mx, my, x + 19, ry + 1, w - 22, rowH - 1);
        }

        POPUP_BORDER.draw(g, mx, my, x, popupY, w, 1);
        POPUP_BORDER.draw(g, mx, my, x, popupY + popupH - 1, w, 1);
        POPUP_BORDER.draw(g, mx, my, x, popupY, 1, popupH);
        POPUP_BORDER.draw(g, mx, my, x + w - 1, popupY, 1, popupH);

        pose.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        if (open && button == LabColors.MOUSE_BUTTON_LEFT) {
            List<LabMachine> visible = filtered();
            int rowH = LabLayout.DROPDOWN_ROW_H;
            int visibleRows = Math.min(LabLayout.DROPDOWN_MAX_ROWS, visible.size());
            int popupY = y + h + 1;
            int popupH = visibleRows * rowH;
            if (Widget.isMouseOver(x, popupY, w, popupH, mouseX, mouseY)) {
                int row = (int) ((mouseY - popupY - 1) / rowH) + scroll;
                if (row >= 0 && row < visible.size()) {
                    select(visible.get(row));
                }
                open = false;
                return true;
            }
            open = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!open) {
            return false;
        }
        int x = getPositionX();
        int y = getPositionY() + getSizeHeight() + 1;
        int visibleRows = Math.min(LabLayout.DROPDOWN_MAX_ROWS, filtered().size());
        if (!Widget.isMouseOver(x, y, getSizeWidth(), visibleRows * LabLayout.DROPDOWN_ROW_H, mouseX, mouseY)) {
            return false;
        }
        int scrollMax = Math.max(0, filtered().size() - LabLayout.DROPDOWN_MAX_ROWS);
        int next = LabScrollMath.wheel(scroll, scrollMax, 1, wheelDelta);
        if (next != scroll) {
            scroll = next;
        }
        return true;
    }

    private static String displayName(LabMachine machine) {
        if (machine.supported()) {
            return machine.name();
        }
        return machine.name() + " (" + I18n.get(LabGuiKeys.NOT_SUPPORTED) + ")";
    }

    private record RowTextures(ItemStackTexture icon, TextTexture nameSelected, TextTexture nameNormal) {
    }
}