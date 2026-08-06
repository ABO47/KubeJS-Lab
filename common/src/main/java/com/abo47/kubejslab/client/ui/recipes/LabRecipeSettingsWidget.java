package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
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
    private final TextTexture shapelessLabel;
    private final TextTexture experienceLabel;
    private final TextTexture cookingTimeLabel;
    private final TextTexture countLabel;
    private final TextTexture processingTimeLabel;
    private final TextTexture heatRequirementLabel;
    private final TextTexture keepHeldItemLabel;
    private final UnitLabel experienceUnit;
    private final UnitLabel cookingTimeUnit;
    private final UnitLabel processingTimeUnit;
    private final LabActionButton clearButton;
    private final LabActionButton saveButton;

    private boolean shapeless;
    private String experienceText = formatFloat(LabRecipeFieldValues.defaults().experience());
    private String cookingTimeText = Integer.toString(LabRecipeFieldValues.defaults().cookingTime());
    private String countText = Integer.toString(LabRecipeFieldValues.defaults().count());
    private String processingTimeText = Integer.toString(LabRecipeFieldValues.defaults().processingTime());
    private HeatRequirement heatRequirement = HeatRequirement.NONE;
    private boolean keepHeldItem;
    private List<LabRecipeField> fields = List.of();
    private List<FieldRow> rows = List.of();
    private boolean unitsMeasured;
    private Runnable onClear;
    private Runnable onSave;

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

        experienceUnit = new UnitLabel(LabGuiKeys.LAB_RECIPE_UNIT_XP);
        cookingTimeUnit = new UnitLabel(LabGuiKeys.LAB_RECIPE_UNIT_TICKS);
        processingTimeUnit = new UnitLabel(LabGuiKeys.LAB_RECIPE_UNIT_TICKS);

        shapelessToggle = new LabToggleSwitchWidget(
                0, 0,
                () -> shapeless,
                value -> shapeless = value,
                null);
        addWidget(shapelessToggle);

        experienceField = numberField(0, 0, FIELD_W, () -> experienceText,
                value -> experienceText = value, experienceText, 0f, 100f);
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
        shapelessToggle.setVisible(fields.contains(LabRecipeField.SHAPELESS));
        experienceField.setVisible(fields.contains(LabRecipeField.EXPERIENCE));
        cookingTimeField.setVisible(fields.contains(LabRecipeField.COOKING_TIME));
        countField.setVisible(fields.contains(LabRecipeField.COUNT));
        processingTimeField.setVisible(fields.contains(LabRecipeField.PROCESSING_TIME));
        heatCycleButton.setVisible(fields.contains(LabRecipeField.HEAT_REQUIREMENT));
        keepHeldItemToggle.setVisible(fields.contains(LabRecipeField.KEEP_HELD_ITEM));
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
                keepHeldItem);
    }

    public void applyValues(LabRecipeFieldValues values) {
        shapeless = values.shapeless();
        experienceText = formatFloat(values.experience());
        cookingTimeText = Integer.toString(values.cookingTime());
        countText = Integer.toString(values.count());
        processingTimeText = Integer.toString(values.processingTime());
        heatRequirement = values.heatRequirement();
        keepHeldItem = values.keepHeldItem();
        experienceField.setCurrentString(experienceText);
        cookingTimeField.setCurrentString(cookingTimeText);
        countField.setCurrentString(countText);
        processingTimeField.setCurrentString(processingTimeText);
        heatCycleButton.setLabel(heatLabelText(heatRequirement));
    }

    public void setOnClear(Runnable onClear) {
        this.onClear = onClear;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        ensureUnitWidths();
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int pad = LabLayout.SETTINGS_PAD;
        int cardX = x + pad;
        int cardW = w - pad * 2;
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            drawCard(g, mx, my, cardX, y, cardW, i, row.label, row.unit, controlWidth(row.control));
        }
        super.drawInBackground(g, mx, my, pt);
    }

    private void rebuildRows() {
        List<FieldRow> built = new ArrayList<>(fields.size());
        for (LabRecipeField field : fields) {
            switch (field) {
                case SHAPELESS -> built.add(new FieldRow(shapelessLabel, null, shapelessToggle));
                case EXPERIENCE -> built.add(new FieldRow(experienceLabel, experienceUnit, experienceField));
                case COOKING_TIME -> built.add(new FieldRow(cookingTimeLabel, cookingTimeUnit, cookingTimeField));
                case COUNT -> built.add(new FieldRow(countLabel, null, countField));
                case PROCESSING_TIME ->
                        built.add(new FieldRow(processingTimeLabel, processingTimeUnit, processingTimeField));
                case HEAT_REQUIREMENT -> built.add(new FieldRow(heatRequirementLabel, null, heatCycleButton));
                case KEEP_HELD_ITEM -> built.add(new FieldRow(keepHeldItemLabel, null, keepHeldItemToggle));
            }
        }
        rows = built;
        relayoutFields();
    }

    private void relayoutFields() {
        int pad = LabLayout.SETTINGS_PAD;
        int cardX = pad;
        int cardW = getSizeWidth() - pad * 2;
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            int rowY = rowY(i);
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
        relayoutFields();
    }

    private int controlWidth(Widget control) {
        if (control == shapelessToggle || control == keepHeldItemToggle) {
            return LabToggleSwitchWidget.DEFAULT_WIDTH;
        }
        if (control == heatCycleButton) {
            return CYCLE_W;
        }
        return FIELD_W;
    }

    private int controlX(int cardX, int cardW, int pad, FieldRow row) {
        return row.control == shapelessToggle || row.control == heatCycleButton || row.control == keepHeldItemToggle
                ? cardX + cardW - pad - controlWidth(row.control)
                : fieldXFor(cardX, cardW, pad, row.unit);
    }

    private int fieldXFor(int cardX, int cardW, int pad, UnitLabel unit) {
        return cardX + cardW - pad - FIELD_W - 4 - (unit == null ? 0 : unit.width);
    }

    private void drawCard(GuiGraphics g, int mx, int my, int cardX, int panelY, int cardW,
            int row, TextTexture label, UnitLabel unit, int controlW) {
        int cardY = panelY + rowY(row);
        CARD_TEXTURE.draw(g, mx, my, cardX, cardY, cardW, LabLayout.CARD_H);
        int pad = LabLayout.SETTINGS_PAD;
        int unitW = unit == null ? 0 : unit.width;
        int labelW = cardW - pad * 2 - controlW - 4 - (unitW > 0 ? unitW + 4 : 0);
        if (unitW > 0) {
            unit.tex.setWidth(unitW);
            unit.tex.draw(g, mx, my, cardX + cardW - pad - unitW, cardY, unitW, LabLayout.CARD_H);
        }
        label.draw(g, mx, my, cardX + pad, cardY, labelW, LabLayout.CARD_H);
    }

    private static int rowY(int row) {
        return row * ROW_STRIDE;
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
            Consumer<String> responder, String initial, float min, float max) {
        TextFieldWidget field = new TextFieldWidget(x, y, w, FIELD_H, supplier, responder);
        field.setNumbersOnly(min, max);
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

    private static float parseFloat(String text, float fallback) {
        try {
            return Float.parseFloat(text);
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

    private record FieldRow(TextTexture label, UnitLabel unit, Widget control) {
    }
}
