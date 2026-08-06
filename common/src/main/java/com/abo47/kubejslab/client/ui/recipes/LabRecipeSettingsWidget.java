package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabNumberFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabScrollBarWidget;
import com.abo47.kubejslab.client.ui.base.LabScrollMath;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

public final class LabRecipeSettingsWidget extends WidgetGroup {
    private static final IGuiTexture CARD_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);
    private static final int ROW_STRIDE = LabLayout.CARD_H + 4;
    private static final int FIELD_W = 44;
    private static final int FIELD_H = 15;
    private static final int CYCLE_W = 70;

    private final LabToggleSwitchWidget shapelessToggle;
    private final TextFieldWidget experienceField;
    private final TextFieldWidget cookingTimeField;
    private final TextFieldWidget countField;
    private final TextFieldWidget processingTimeField;
    private final LabActionButton heatCycleButton;
    private final LabToggleSwitchWidget keepHeldItemToggle;
    private final LabToggleSwitchWidget acceptMirroredToggle;
    private final TextFieldWidget gridWidthField;
    private final TextFieldWidget gridHeightField;
    private final TextTexture shapelessLabel;
    private final TextTexture experienceLabel;
    private final TextTexture cookingTimeLabel;
    private final TextTexture countLabel;
    private final TextTexture processingTimeLabel;
    private final TextTexture heatRequirementLabel;
    private final TextTexture keepHeldItemLabel;
    private final TextTexture acceptMirroredLabel;
    private final TextTexture gridWidthLabel;
    private final TextTexture gridHeightLabel;
    private final TextTexture chanceLabel;
    private final UnitLabel experienceUnit;
    private final UnitLabel cookingTimeUnit;
    private final UnitLabel processingTimeUnit;
    private final UnitLabel percentUnit;
    private final LabActionButton clearButton;
    private final LabActionButton saveButton;
    private final LabScrollBarWidget scrollBar;

    private boolean shapeless;
    private String experienceText = formatFloat(LabRecipeFieldValues.defaults().experience());
    private String cookingTimeText = Integer.toString(LabRecipeFieldValues.defaults().cookingTime());
    private String countText = Integer.toString(LabRecipeFieldValues.defaults().count());
    private String processingTimeText = Integer.toString(LabRecipeFieldValues.defaults().processingTime());
    private HeatRequirement heatRequirement = HeatRequirement.NONE;
    private boolean keepHeldItem;
    private boolean acceptMirrored = true;
    private String gridWidthText = Integer.toString(LabRecipeFieldValues.defaults().gridWidth());
    private String gridHeightText = Integer.toString(LabRecipeFieldValues.defaults().gridHeight());
    private List<LabRecipeField> fields = List.of();
    private List<FieldRow> rows = List.of();
    private List<OutputRow> outputRows = List.of();
    private final List<TextFieldWidget> outputChanceFields = new ArrayList<>();
    private boolean unitsMeasured;
    private int scrollOffset;
    private int scrollMax;
    private boolean dragging;
    private Runnable onClear;
    private Runnable onSave;
    private Runnable gridSizeListener;

    public LabRecipeSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h);

        int pad = LabLayout.SETTINGS_PAD;
        int cardX = pad;
        int cardW = w - pad * 2;
        int labelW = cardW - pad * 2 - FIELD_W - 4;

        shapelessLabel = rowLabel(LabGuiKeys.LAB_RECIPE_SHAPELESS, labelW);
        experienceLabel = rowLabel(LabGuiKeys.LAB_RECIPE_EXPERIENCE, labelW);
        cookingTimeLabel = rowLabel(LabGuiKeys.LAB_RECIPE_COOKING_TIME, labelW);
        countLabel = rowLabel(LabGuiKeys.LAB_RECIPE_COUNT, labelW);
        processingTimeLabel = rowLabel(LabGuiKeys.LAB_RECIPE_PROCESSING_TIME, labelW);
        heatRequirementLabel = rowLabel(LabGuiKeys.LAB_RECIPE_HEAT_REQUIREMENT, labelW);
        keepHeldItemLabel = rowLabel(LabGuiKeys.LAB_RECIPE_KEEP_HELD_ITEM, labelW);
        acceptMirroredLabel = rowLabel(LabGuiKeys.LAB_RECIPE_ACCEPT_MIRRORED, labelW);
        gridWidthLabel = rowLabel(LabGuiKeys.LAB_RECIPE_GRID_WIDTH, labelW);
        gridHeightLabel = rowLabel(LabGuiKeys.LAB_RECIPE_GRID_HEIGHT, labelW);
        chanceLabel = rowLabel(LabGuiKeys.LAB_RECIPE_CHANCE, labelW);

        experienceUnit = new UnitLabel(LabGuiKeys.LAB_RECIPE_UNIT_XP);
        cookingTimeUnit = new UnitLabel(LabGuiKeys.LAB_RECIPE_UNIT_TICKS);
        processingTimeUnit = new UnitLabel(LabGuiKeys.LAB_RECIPE_UNIT_TICKS);
        percentUnit = new UnitLabel(LabGuiKeys.LAB_RECIPE_UNIT_PERCENT);

        shapelessToggle = new LabToggleSwitchWidget(
                0, 0,
                () -> shapeless,
                value -> shapeless = value,
                null);
        addWidget(shapelessToggle);

        experienceField = numberField(0, 0, FIELD_W, () -> experienceText,
                value -> experienceText = value, experienceText);
        addWidget(experienceField);

        cookingTimeField = numberField(0, 0, FIELD_W, () -> cookingTimeText,
                value -> cookingTimeText = value, cookingTimeText, 0, Integer.MAX_VALUE);
        addWidget(cookingTimeField);

        countField = numberField(0, 0, FIELD_W, () -> countText,
                value -> countText = value, countText, 1, 64);
        addWidget(countField);

        processingTimeField = numberField(0, 0, FIELD_W, () -> processingTimeText,
                value -> processingTimeText = value, processingTimeText, 0, Integer.MAX_VALUE);
        addWidget(processingTimeField);

        heatCycleButton = new LabActionButton(0, 0, CYCLE_W, FIELD_H, heatLabelText(heatRequirement), this::cycleHeat);
        addWidget(heatCycleButton);

        keepHeldItemToggle = new LabToggleSwitchWidget(
                0, 0,
                () -> keepHeldItem,
                value -> keepHeldItem = value,
                null);
        addWidget(keepHeldItemToggle);

        acceptMirroredToggle = new LabToggleSwitchWidget(
                0, 0,
                () -> acceptMirrored,
                value -> acceptMirrored = value,
                null);
        addWidget(acceptMirroredToggle);

        gridWidthField = numberField(0, 0, FIELD_W,
                () -> gridWidthText,
                value -> {
                    gridWidthText = value;
                    notifyGridSize();
                },
                gridWidthText, 1, 9);
        addWidget(gridWidthField);

        gridHeightField = numberField(0, 0, FIELD_W,
                () -> gridHeightText,
                value -> {
                    gridHeightText = value;
                    notifyGridSize();
                },
                gridHeightText, 1, 9);
        addWidget(gridHeightField);

        int btnH = LabLayout.SETTINGS_BTN_H;
        int bottomY = h - pad - btnH;
        int btnW = (cardW - LabLayout.SETTINGS_BTN_GAP) / 2;

        clearButton = new LabActionButton(cardX, bottomY, btnW, btnH,
                Component.translatable(LabGuiKeys.LAB_RECIPE_CLEAR).getString(), () -> {
            if (onClear != null) onClear.run();
        });
        addWidget(clearButton);

        saveButton = new LabActionButton(cardX + btnW + LabLayout.SETTINGS_BTN_GAP, bottomY, btnW, btnH,
                Component.translatable(LabGuiKeys.LAB_RECIPE_SAVE).getString(), () -> {
            if (onSave != null) onSave.run();
        });
        addWidget(saveButton);

        scrollBar = new LabScrollBarWidget(
                w - LabLayout.SCROLLBAR_W - 2, pad, LabLayout.SCROLLBAR_W, bottomY - pad,
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

    public boolean isShapeless() {
        return shapeless;
    }

    public void setFields(List<LabRecipeField> fields) {
        this.fields = fields;
        if (!fields.contains(LabRecipeField.SHAPELESS)) {
            shapeless = false;
        }
        if (!fields.contains(LabRecipeField.HEAT_REQUIREMENT)) {
            heatRequirement = HeatRequirement.NONE;
            heatCycleButton.setLabel(heatLabelText(heatRequirement));
        }
        if (!fields.contains(LabRecipeField.KEEP_HELD_ITEM)) {
            keepHeldItem = false;
        }
        if (!fields.contains(LabRecipeField.ACCEPT_MIRRORED)) {
            acceptMirrored = true;
        }
        shapelessToggle.setVisible(fields.contains(LabRecipeField.SHAPELESS));
        experienceField.setVisible(fields.contains(LabRecipeField.EXPERIENCE));
        cookingTimeField.setVisible(fields.contains(LabRecipeField.COOKING_TIME));
        countField.setVisible(fields.contains(LabRecipeField.COUNT));
        processingTimeField.setVisible(fields.contains(LabRecipeField.PROCESSING_TIME));
        heatCycleButton.setVisible(fields.contains(LabRecipeField.HEAT_REQUIREMENT));
        keepHeldItemToggle.setVisible(fields.contains(LabRecipeField.KEEP_HELD_ITEM));
        acceptMirroredToggle.setVisible(fields.contains(LabRecipeField.ACCEPT_MIRRORED));
        gridWidthField.setVisible(fields.contains(LabRecipeField.GRID_WIDTH));
        gridHeightField.setVisible(fields.contains(LabRecipeField.GRID_HEIGHT));
        scrollOffset = 0;
        rebuildRows();
    }

    public void setOutputRows(List<OutputRow> outputRows) {
        for (TextFieldWidget field : outputChanceFields) {
            removeWidget(field);
        }
        outputChanceFields.clear();
        this.outputRows = List.copyOf(outputRows);
        for (OutputRow row : outputRows) {
            String initial = formatFloat(row.chanceSupplier().get() * 100f);
            TextFieldWidget field = numberField(0, 0, FIELD_W,
                    () -> formatFloat(row.chanceSupplier().get() * 100f),
                    value -> {
                        if (value != null && !value.isBlank()) {
                            row.chanceSetter().accept(clampChance(parseFloat(value, 100f) / 100f));
                        }
                    },
                    initial);
            addWidget(field);
            outputChanceFields.add(field);
        }
        rebuildRows();
    }

    public LabRecipeFieldValues getValues() {
        LabRecipeFieldValues defaults = LabRecipeFieldValues.defaults();
        return new LabRecipeFieldValues(
                shapeless,
                parseFloat(experienceText, defaults.experience()),
                parseInt(cookingTimeText, defaults.cookingTime()),
                parseInt(countText, defaults.count()),
                parseInt(processingTimeText, defaults.processingTime()),
                heatRequirement,
                keepHeldItem,
                fields.contains(LabRecipeField.ACCEPT_MIRRORED) ? acceptMirrored : defaults.acceptMirrored(),
                parseInt(gridWidthText, defaults.gridWidth()),
                parseInt(gridHeightText, defaults.gridHeight()));
    }

    public void applyValues(LabRecipeFieldValues values) {
        shapeless = values.shapeless();
        experienceText = formatFloat(values.experience());
        cookingTimeText = Integer.toString(values.cookingTime());
        countText = Integer.toString(values.count());
        processingTimeText = Integer.toString(values.processingTime());
        heatRequirement = values.heatRequirement();
        keepHeldItem = values.keepHeldItem();
        acceptMirrored = values.acceptMirrored();
        gridWidthText = Integer.toString(Math.max(1, Math.min(9, values.gridWidth())));
        gridHeightText = Integer.toString(Math.max(1, Math.min(9, values.gridHeight())));
        experienceField.setCurrentString(experienceText);
        cookingTimeField.setCurrentString(cookingTimeText);
        countField.setCurrentString(countText);
        processingTimeField.setCurrentString(processingTimeText);
        gridWidthField.setCurrentString(gridWidthText);
        gridHeightField.setCurrentString(gridHeightText);
        heatCycleButton.setLabel(heatLabelText(heatRequirement));
    }

    public void setOnClear(Runnable onClear) {
        this.onClear = onClear;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void setGridSizeListener(Runnable gridSizeListener) {
        this.gridSizeListener = gridSizeListener;
    }

    public int gridWidthValue() {
        return Math.max(1, Math.min(9, parseInt(gridWidthText, LabRecipeFieldValues.defaults().gridWidth())));
    }

    public int gridHeightValue() {
        return Math.max(1, Math.min(9, parseInt(gridHeightText, LabRecipeFieldValues.defaults().gridHeight())));
    }

    private void notifyGridSize() {
        if (gridSizeListener != null) {
            gridSizeListener.run();
        }
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        ensureUnitWidths();
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        int pad = LabLayout.SETTINGS_PAD;
        int cardX = x + pad;
        int cardW = w - pad * 2;
        int bottomY = h - pad - LabLayout.SETTINGS_BTN_H;
        int contentBottom = y + bottomY;

        g.flush();
        g.enableScissor(x + 1, y + 1, x + w - 1, contentBottom);
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            int rowY = rowY(i) - scrollOffset;
            int cardY = y + rowY;
            if (cardY + LabLayout.CARD_H < y || cardY > contentBottom) {
                continue;
            }
            drawCard(g, mx, my, cardX, y, cardW, rowY, row.label, row.unit,
                    controlWidth(row.control), row.icon);
            if (row.control != null) {
                g.flush();
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                row.control.drawInBackground(g, mx, my, pt);
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
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY) && scrollMax > 0) {
            int step = Math.max(8, ROW_STRIDE / 3);
            scrollOffset = LabScrollMath.wheel(scrollOffset, scrollMax, step, wheelDelta);
            relayoutFields();
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    private void rebuildRows() {
        List<FieldRow> built = new ArrayList<>(fields.size() + outputRows.size());
        for (LabRecipeField field : fields) {
            switch (field) {
                case SHAPELESS -> built.add(new FieldRow(shapelessLabel, null, shapelessToggle, null));
                case EXPERIENCE -> built.add(new FieldRow(experienceLabel, experienceUnit, experienceField, null));
                case COOKING_TIME ->
                        built.add(new FieldRow(cookingTimeLabel, cookingTimeUnit, cookingTimeField, null));
                case COUNT -> built.add(new FieldRow(countLabel, null, countField, null));
                case PROCESSING_TIME ->
                        built.add(new FieldRow(processingTimeLabel, processingTimeUnit, processingTimeField, null));
                case HEAT_REQUIREMENT ->
                        built.add(new FieldRow(heatRequirementLabel, null, heatCycleButton, null));
                case KEEP_HELD_ITEM ->
                        built.add(new FieldRow(keepHeldItemLabel, null, keepHeldItemToggle, null));
                case ACCEPT_MIRRORED ->
                        built.add(new FieldRow(acceptMirroredLabel, null, acceptMirroredToggle, null));
                case GRID_WIDTH -> built.add(new FieldRow(gridWidthLabel, null, gridWidthField, null));
                case GRID_HEIGHT -> built.add(new FieldRow(gridHeightLabel, null, gridHeightField, null));
            }
        }
        for (int i = 0; i < outputRows.size(); i++) {
            OutputRow row = outputRows.get(i);
            built.add(new FieldRow(chanceLabel, percentUnit, outputChanceFields.get(i),
                    new ItemStackTexture(row.icon())));
        }
        rows = built;
        recomputeScrollMax();
        relayoutFields();
    }

    private void recomputeScrollMax() {
        int pad = LabLayout.SETTINGS_PAD;
        int bottomY = getSizeHeight() - pad - LabLayout.SETTINGS_BTN_H;
        int viewport = Math.max(1, bottomY - pad);
        int contentH = pad + rows.size() * ROW_STRIDE;
        scrollMax = Math.max(0, contentH - viewport);
        scrollOffset = Math.min(scrollOffset, scrollMax);
        scrollBar.setVisible(scrollMax > 0);
    }

    private int scrollKnobHeight() {
        int pad = LabLayout.SETTINGS_PAD;
        int bottomY = getSizeHeight() - pad - LabLayout.SETTINGS_BTN_H;
        int viewport = Math.max(1, bottomY - pad);
        int contentH = Math.max(1, pad + rows.size() * ROW_STRIDE);
        return Math.max(LabLayout.KNOB_MIN_H, viewport * viewport / contentH);
    }

    private void relayoutFields() {
        int pad = LabLayout.SETTINGS_PAD;
        int cardX = pad;
        int cardW = getSizeWidth() - pad * 2;
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            int rowY = rowY(i) - scrollOffset;
            row.control.setSelfPosition(new Position(controlX(cardX, cardW, pad, row),
                    rowY + (LabLayout.CARD_H - row.control.getSizeHeight()) / 2));
        }
    }

    private void ensureUnitWidths() {
        if (unitsMeasured) {
            return;
        }
        unitsMeasured = true;
        experienceUnit.width = Minecraft.getInstance().font.width(experienceUnit.text);
        cookingTimeUnit.width = Minecraft.getInstance().font.width(cookingTimeUnit.text);
        processingTimeUnit.width = Minecraft.getInstance().font.width(processingTimeUnit.text);
        percentUnit.width = Minecraft.getInstance().font.width(percentUnit.text);
        relayoutFields();
    }

    private int controlWidth(Widget control) {
        if (control == shapelessToggle || control == keepHeldItemToggle || control == acceptMirroredToggle) {
            return LabToggleSwitchWidget.DEFAULT_WIDTH;
        }
        if (control == heatCycleButton) {
            return CYCLE_W;
        }
        return FIELD_W;
    }

    private int controlX(int cardX, int cardW, int pad, FieldRow row) {
        return row.control == shapelessToggle || row.control == heatCycleButton
                || row.control == keepHeldItemToggle || row.control == acceptMirroredToggle
                ? cardX + cardW - pad - controlWidth(row.control)
                : fieldXFor(cardX, cardW, pad, row.unit);
    }

    private int fieldXFor(int cardX, int cardW, int pad, UnitLabel unit) {
        return cardX + cardW - pad - FIELD_W - 4 - (unit == null ? 0 : unit.width);
    }

    private void drawCard(GuiGraphics g, int mx, int my, int cardX, int panelY, int cardW,
            int rowY, TextTexture label, UnitLabel unit, int controlW, ItemStackTexture icon) {
        int cardY = panelY + rowY;
        CARD_TEXTURE.draw(g, mx, my, cardX, cardY, cardW, LabLayout.CARD_H);
        int pad = LabLayout.SETTINGS_PAD;
        int unitW = unit == null ? 0 : unit.width;
        int iconW = icon == null ? 0 : 16 + 4;
        int labelW = cardW - pad * 2 - controlW - 4 - (unitW > 0 ? unitW + 4 : 0) - iconW;
        if (icon != null) {
            icon.draw(g, mx, my, cardX + pad, cardY + (LabLayout.CARD_H - 16) / 2, 16, 16);
        }
        if (unitW > 0) {
            unit.tex.setWidth(unitW);
            unit.tex.draw(g, mx, my, cardX + cardW - pad - unitW, cardY, unitW, LabLayout.CARD_H);
        }
        label.draw(g, mx, my, cardX + pad + iconW, cardY, labelW, LabLayout.CARD_H);
    }

    private static int rowY(int row) {
        return LabLayout.SETTINGS_PAD + row * ROW_STRIDE;
    }

    private static TextTexture rowLabel(String key, int width) {
        return new TextTexture(Component.translatable(key).getString(), LabColors.TEXT_PRIMARY)
                .setWidth(width)
                .setType(TextTexture.TextType.LEFT_HIDE);
    }

    private void cycleHeat() {
        heatRequirement = HeatRequirement.cycle(heatRequirement);
        heatCycleButton.setLabel(heatLabelText(heatRequirement));
    }

    private static String heatLabelText(HeatRequirement heat) {
        return Component.translatable(switch (heat) {
            case NONE -> LabGuiKeys.LAB_RECIPE_HEAT_NONE;
            case HEATED -> LabGuiKeys.LAB_RECIPE_HEAT_HEATED;
            case SUPERHEATED -> LabGuiKeys.LAB_RECIPE_HEAT_SUPERHEATED;
        }).getString();
    }

    private static TextFieldWidget numberField(int x, int y, int w, Supplier<String> supplier,
            Consumer<String> responder, String initial, long min, long max) {
        TextFieldWidget field = new TextFieldWidget(x, y, w, FIELD_H, supplier, responder);
        field.setNumbersOnly(min, max);
        configureField(field, initial);
        return field;
    }

    private static TextFieldWidget numberField(int x, int y, int w, Supplier<String> supplier,
            Consumer<String> responder, String initial) {
        TextFieldWidget field = LabNumberFieldWidget.create(x, y, w, FIELD_H, supplier, responder);
        configureField(field, initial);
        return field;
    }

    private static void configureField(TextFieldWidget field, String initial) {
        field.setClientSideWidget();
        field.setMaxStringLength(4);
        field.setBordered(false);
        field.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        field.setTextColor(LabColors.TEXT_PRIMARY);
        field.setCurrentString(initial);
    }

    private static float clampChance(float chance) {
        return Math.max(0f, Math.min(1f, chance));
    }

    private static float parseFloat(String text, float fallback) {
        try {
            return Float.parseFloat(text.replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String formatFloat(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return Float.toString(value);
    }

    private static final class UnitLabel {
        final TextTexture tex;
        final String text;
        int width;

        UnitLabel(String key) {
            text = Component.translatable(key).getString();
            tex = new TextTexture(text, LabColors.TEXT_MUTED);
        }
    }

    public record OutputRow(ItemStack icon, Supplier<Float> chanceSupplier, Consumer<Float> chanceSetter) {
    }

    private record FieldRow(TextTexture label, UnitLabel unit, Widget control, ItemStackTexture icon) {
    }
}
