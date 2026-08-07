package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.List;
import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabBlockSafeSlotWidget;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabNumberFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabRecipeTooltips;
import com.abo47.kubejslab.client.ui.base.LabScrollBarWidget;
import com.abo47.kubejslab.client.ui.base.LabScrollMath;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;
import com.abo47.kubejslab.recipe.model.ClocheRenderType;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;


public final class LabRecipeSettingsWidget extends WidgetGroup {
    private static final IGuiTexture CARD_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);
    private static final int ROW_STRIDE = LabLayout.CARD_H + 4;
    private static final int FIELD_H = 15;
    private static final int CONTROL_W = 44;

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
    private final TextFieldWidget energyField;
    private final TextFieldWidget creosoteAmountField;
    private final List<String> moldOptions = new ArrayList<>(LabOptionLibrary.moldOptions());
    private final LabOptionDropdownWidget moldDropdown;
    private final LabCommitFieldWidget moldCommitField;
    private final List<String> blueprintOptions = new ArrayList<>(LabOptionLibrary.blueprintCategoryOptions());
    private final LabOptionDropdownWidget blueprintCategoryDropdown;
    private final LabCommitFieldWidget blueprintCategoryCommitField;
    private LabOptionDropdownWidget.DropdownRightClick categoryContextRequester;
    private final LabOptionDropdownWidget clocheRenderTypeDropdown;
    private final LabBlockSafeSlotWidget clocheRenderBlockSlot;
    private final TextFieldWidget fluidInputAmountField;
    private final TextFieldWidget fluidOutputAmountField;
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
    private final TextTexture energyLabel;
    private final TextTexture creosoteAmountLabel;
    private final TextTexture moldLabel;
    private final TextTexture blueprintCategoryLabel;
    private final TextTexture clocheRenderTypeLabel;
    private final TextTexture clocheRenderBlockLabel;
    private final TextTexture fluidInputAmountLabel;
    private final TextTexture fluidOutputAmountLabel;
    private final TextTexture chanceLabel;
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
    private String energyText = "0";
    private String creosoteAmountText = "0";
    private String moldText = "";
    private String blueprintCategoryText = "";
    private ClocheRenderType clocheRenderType = ClocheRenderType.GENERIC;
    private String clocheRenderBlockText = "";
    private String fluidInputAmountText = "0";
    private String fluidOutputAmountText = "0";
    private List<LabRecipeField> fields = List.of();
    private List<FieldRow> rows = List.of();
    private List<OutputRow> outputRows = List.of();
    private final List<TextFieldWidget> outputChanceFields = new ArrayList<>();
    private int scrollOffset;
    private int scrollMax;
    private boolean dragging;
    private Runnable onClear;
    private Runnable onSave;
    private Runnable gridSizeListener;

    public LabRecipeSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h);

        int pad = LabLayout.SETTINGS_PAD;
        int cardW = w - pad * 2;
        int labelW = cardW - pad * 2 - CONTROL_W - 4;

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
        energyLabel = rowLabel(LabGuiKeys.LAB_RECIPE_ENERGY, labelW);
        creosoteAmountLabel = rowLabel(LabGuiKeys.LAB_RECIPE_CREOSOTE_AMOUNT, labelW);
        moldLabel = rowLabel(LabGuiKeys.LAB_RECIPE_MOLD, labelW);
        blueprintCategoryLabel = rowLabel(LabGuiKeys.LAB_RECIPE_BLUEPRINT_CATEGORY, labelW);
        clocheRenderTypeLabel = rowLabel(LabGuiKeys.LAB_RECIPE_CLOCHE_RENDER_TYPE, labelW);
        clocheRenderBlockLabel = rowLabel(LabGuiKeys.LAB_RECIPE_CLOCHE_RENDER_BLOCK, labelW);
        fluidInputAmountLabel = rowLabel(LabGuiKeys.LAB_RECIPE_FLUID_INPUT_AMOUNT, labelW);
        fluidOutputAmountLabel = rowLabel(LabGuiKeys.LAB_RECIPE_FLUID_OUTPUT_AMOUNT, labelW);
        chanceLabel = rowLabel(LabGuiKeys.LAB_RECIPE_CHANCE, labelW);

        shapelessToggle = new LabToggleSwitchWidget(
                0, 0,
                () -> shapeless,
                value -> shapeless = value,
                null);
        addWidget(shapelessToggle);

        experienceField = numberField(0, 0, () -> experienceText, value -> experienceText = value, experienceText);
        addWidget(experienceField);

        cookingTimeField = numberField(0, 0, () -> cookingTimeText,
                value -> cookingTimeText = value, cookingTimeText);
        addWidget(cookingTimeField);

        countField = numberField(0, 0, () -> countText, value -> countText = value, countText);
        addWidget(countField);

        processingTimeField = numberField(0, 0, () -> processingTimeText,
                value -> processingTimeText = value, processingTimeText);
        addWidget(processingTimeField);

        heatCycleButton = new LabActionButton(0, 0, CONTROL_W, FIELD_H,
                heatLabelText(heatRequirement), this::cycleHeat);
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

        gridWidthField = numberField(0, 0, () -> gridWidthText,
                value -> {
                    gridWidthText = value;
                    notifyGridSize();
                },
                gridWidthText);
        addWidget(gridWidthField);

        gridHeightField = numberField(0, 0, () -> gridHeightText,
                value -> {
                    gridHeightText = value;
                    notifyGridSize();
                },
                gridHeightText);
        addWidget(gridHeightField);

        energyField = numberField(0, 0, () -> energyText, value -> energyText = value, energyText);
        addWidget(energyField);

        creosoteAmountField = numberField(0, 0, () -> creosoteAmountText,
                value -> creosoteAmountText = value, creosoteAmountText);
        addWidget(creosoteAmountField);

        moldDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        moldDropdown.setOnSelect(value -> moldText = value);
        addWidget(moldDropdown);
        refreshMoldOptions();

        moldCommitField = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H, null, this::commitMold);
        configureCommit(moldCommitField);
        addWidget(moldCommitField);

        blueprintCategoryDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        blueprintCategoryDropdown.setOnSelect(value -> blueprintCategoryText = value);
        blueprintCategoryDropdown.setOnItemRightClick((option, mx, my) -> {
            if (LabBlueprintCategories.isCustom(option) && categoryContextRequester != null) {
                categoryContextRequester.onRightClick(option, mx, my);
            }
        });
        addWidget(blueprintCategoryDropdown);
        refreshBlueprintOptions();

        blueprintCategoryCommitField = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H,
                null, this::commitBlueprintCategory);
        configureCommit(blueprintCategoryCommitField);
        addWidget(blueprintCategoryCommitField);

        clocheRenderTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        clocheRenderTypeDropdown.setOnSelect(value -> clocheRenderType = ClocheRenderType.byName(value));
        clocheRenderTypeDropdown.setOptions(LabOptionLibrary.clocheRenderTypes());
        addWidget(clocheRenderTypeDropdown);

        clocheRenderBlockSlot = new LabBlockSafeSlotWidget(0, 0, CONTROL_W, FIELD_H);
        clocheRenderBlockSlot.setOnChange(() -> clocheRenderBlockText = clocheRenderBlockSlot.getBlockId());
        addWidget(clocheRenderBlockSlot);

        fluidInputAmountField = numberField(0, 0, () -> fluidInputAmountText,
                value -> fluidInputAmountText = value, fluidInputAmountText);
        addWidget(fluidInputAmountField);

        fluidOutputAmountField = numberField(0, 0, () -> fluidOutputAmountText,
                value -> fluidOutputAmountText = value, fluidOutputAmountText);
        addWidget(fluidOutputAmountField);

        int btnH = LabLayout.SETTINGS_BTN_H;
        int bottomY = h - pad - btnH;
        int btnW = (cardW - LabLayout.SETTINGS_BTN_GAP) / 2;

        clearButton = new LabActionButton(LabLayout.SETTINGS_PAD, bottomY, btnW, btnH,
                Component.translatable(LabGuiKeys.LAB_RECIPE_CLEAR).getString(), () -> {
            if (onClear != null) onClear.run();
        });
        addWidget(clearButton);

        saveButton = new LabActionButton(LabLayout.SETTINGS_PAD + btnW + LabLayout.SETTINGS_BTN_GAP, bottomY, btnW,
                btnH,
                Component.translatable(LabGuiKeys.LAB_RECIPE_SAVE).getString(), () -> {
            if (onSave != null) onSave.run();
        });
        addWidget(saveButton);

        scrollBar = new LabScrollBarWidget(
                w - LabLayout.SCROLLBAR_W - 2, LabLayout.SETTINGS_PAD, LabLayout.SCROLLBAR_W, bottomY - LabLayout.SETTINGS_PAD,
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
        if (!fields.contains(LabRecipeField.CLOCHE_RENDER_TYPE)) {
            clocheRenderType = ClocheRenderType.GENERIC;
            clocheRenderTypeDropdown.setSelected("generic");
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
        energyField.setVisible(fields.contains(LabRecipeField.ENERGY));
        creosoteAmountField.setVisible(fields.contains(LabRecipeField.CREOSOTE_AMOUNT));
        moldDropdown.setVisible(fields.contains(LabRecipeField.MOLD));
        moldCommitField.setVisible(fields.contains(LabRecipeField.MOLD));
        blueprintCategoryDropdown.setVisible(fields.contains(LabRecipeField.BLUEPRINT_CATEGORY));
        blueprintCategoryCommitField.setVisible(fields.contains(LabRecipeField.BLUEPRINT_CATEGORY));
        clocheRenderTypeDropdown.setVisible(fields.contains(LabRecipeField.CLOCHE_RENDER_TYPE));
        clocheRenderBlockSlot.setVisible(fields.contains(LabRecipeField.CLOCHE_RENDER_BLOCK));
        fluidInputAmountField.setVisible(fields.contains(LabRecipeField.FLUID_INPUT_AMOUNT));
        fluidOutputAmountField.setVisible(fields.contains(LabRecipeField.FLUID_OUTPUT_AMOUNT));
        for (LabRecipeField field : fields) {
            controlFor(field).setHoverTooltips(LabRecipeTooltips.forField(field));
        }
        scrollOffset = 0;
        rebuildRows();
    }

    private Widget controlFor(LabRecipeField field) {
        return switch (field) {
            case SHAPELESS -> shapelessToggle;
            case EXPERIENCE -> experienceField;
            case COOKING_TIME -> cookingTimeField;
            case COUNT -> countField;
            case PROCESSING_TIME -> processingTimeField;
            case HEAT_REQUIREMENT -> heatCycleButton;
            case KEEP_HELD_ITEM -> keepHeldItemToggle;
            case ACCEPT_MIRRORED -> acceptMirroredToggle;
            case GRID_WIDTH -> gridWidthField;
            case GRID_HEIGHT -> gridHeightField;
            case ENERGY -> energyField;
            case CREOSOTE_AMOUNT -> creosoteAmountField;
            case MOLD -> moldDropdown;
            case BLUEPRINT_CATEGORY -> blueprintCategoryDropdown;
            case CLOCHE_RENDER_TYPE -> clocheRenderTypeDropdown;
            case CLOCHE_RENDER_BLOCK -> clocheRenderBlockSlot;
            case FLUID_INPUT_AMOUNT -> fluidInputAmountField;
            case FLUID_OUTPUT_AMOUNT -> fluidOutputAmountField;
        };
    }

    private void refreshMoldOptions() {
        List<String> merged = new ArrayList<>(moldOptions);
        if (!moldText.isEmpty() && !merged.contains(moldText)) {
            merged.add(moldText);
        }
        moldDropdown.setOptions(merged);
        moldDropdown.setSelected(moldText);
    }

    private void refreshBlueprintOptions() {
        List<String> merged = new ArrayList<>(LabOptionLibrary.blueprintCategoryOptions());
        if (!blueprintCategoryText.isBlank() && !merged.contains(blueprintCategoryText)) {
            merged.add(blueprintCategoryText);
        }
        blueprintCategoryDropdown.setOptions(merged);
        blueprintCategoryDropdown.setSelected(blueprintCategoryText);
    }

    private void commitMold(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        moldText = value;
        if (!moldOptions.contains(value)) {
            moldOptions.add(value);
        }
        refreshMoldOptions();
    }

    private void commitBlueprintCategory(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        blueprintCategoryText = value;
        LabBlueprintCategories.add(value);
        if (!blueprintOptions.contains(value)) {
            blueprintOptions.add(value);
        }
        refreshBlueprintOptions();
    }

    public void setCategoryContextRequester(LabOptionDropdownWidget.DropdownRightClick categoryContextRequester) {
        this.categoryContextRequester = categoryContextRequester;
    }

    public void deleteBlueprintCategory(String category) {
        LabBlueprintCategories.remove(category);
        blueprintOptions.remove(category);
        if (blueprintCategoryText.equals(category)) {
            blueprintCategoryText = "";
        }
        refreshBlueprintOptions();
    }

    public void setOutputRows(List<OutputRow> outputRows) {
        for (TextFieldWidget field : outputChanceFields) {
            removeWidget(field);
        }
        outputChanceFields.clear();
        this.outputRows = List.copyOf(outputRows);
        for (OutputRow row : outputRows) {
            String initial = formatFloat(row.chanceSupplier().get() * 100f);
            TextFieldWidget field = numberField(0, 0,
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
                Math.max(1, Math.min(9, parseInt(gridWidthText, defaults.gridWidth()))),
                Math.max(1, Math.min(9, parseInt(gridHeightText, defaults.gridHeight()))),
                Math.max(0, parseInt(energyText, defaults.energy())),
                Math.max(0, parseInt(creosoteAmountText, defaults.creosoteAmount())),
                moldText,
                blueprintCategoryText,
                fields.contains(LabRecipeField.CLOCHE_RENDER_TYPE) ? clocheRenderType : defaults.clocheRenderType(),
                fields.contains(LabRecipeField.CLOCHE_RENDER_BLOCK) ? clocheRenderBlockText
                        : defaults.clocheRenderBlock(),
                Math.max(0, parseInt(fluidInputAmountText, 0)),
                Math.max(0, parseInt(fluidOutputAmountText, 0)));
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
        gridWidthText = Integer.toString(values.gridWidth());
        gridHeightText = Integer.toString(values.gridHeight());
        energyText = Integer.toString(values.energy());
        creosoteAmountText = Integer.toString(values.creosoteAmount());
        moldText = values.mold();
        blueprintCategoryText = values.blueprintCategory();
        clocheRenderType = values.clocheRenderType();
        clocheRenderBlockText = values.clocheRenderBlock();
        fluidInputAmountText = Integer.toString(values.fluidInputAmount());
        fluidOutputAmountText = Integer.toString(values.fluidOutputAmount());

        experienceField.setCurrentString(experienceText);
        cookingTimeField.setCurrentString(cookingTimeText);
        countField.setCurrentString(countText);
        processingTimeField.setCurrentString(processingTimeText);
        gridWidthField.setCurrentString(gridWidthText);
        gridHeightField.setCurrentString(gridHeightText);
        energyField.setCurrentString(energyText);
        creosoteAmountField.setCurrentString(creosoteAmountText);
        fluidInputAmountField.setCurrentString(fluidInputAmountText);
        fluidOutputAmountField.setCurrentString(fluidOutputAmountText);
        heatCycleButton.setLabel(heatLabelText(heatRequirement));
        refreshMoldOptions();
        refreshBlueprintOptions();
        clocheRenderTypeDropdown.setSelected(clocheRenderType.name().toLowerCase());
        clocheRenderBlockSlot.setBlockId(clocheRenderBlockText);
    }

    public void setOnClear(Runnable onClear) {
        this.onClear = onClear;
    }

    public void setFluidOutputAmount(int amount) {
        fluidOutputAmountText = Integer.toString(Math.max(0, amount));
        fluidOutputAmountField.setCurrentString(fluidOutputAmountText);
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
            drawCard(g, mx, my, cardX, y, cardW, rowY, row.label,
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
        if (super.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
            return true;
        }
        if (isMouseOverElement(mouseX, mouseY) && scrollMax > 0) {
            int step = Math.max(8, ROW_STRIDE / 3);
            scrollOffset = LabScrollMath.wheel(scrollOffset, scrollMax, step, wheelDelta);
            relayoutFields();
            return true;
        }
        return false;
    }

    private void rebuildRows() {
        List<FieldRow> built = new ArrayList<>(fields.size() + outputRows.size());
        for (LabRecipeField field : fields) {
            switch (field) {
                case SHAPELESS -> built.add(new FieldRow(shapelessLabel, shapelessToggle, null));
                case EXPERIENCE -> built.add(new FieldRow(experienceLabel, experienceField, null));
                case COOKING_TIME -> built.add(new FieldRow(cookingTimeLabel, cookingTimeField, null));
                case COUNT -> built.add(new FieldRow(countLabel, countField, null));
                case PROCESSING_TIME ->
                        built.add(new FieldRow(processingTimeLabel, processingTimeField, null));
                case HEAT_REQUIREMENT -> built.add(new FieldRow(heatRequirementLabel, heatCycleButton, null));
                case KEEP_HELD_ITEM -> built.add(new FieldRow(keepHeldItemLabel, keepHeldItemToggle, null));
                case ACCEPT_MIRRORED ->
                        built.add(new FieldRow(acceptMirroredLabel, acceptMirroredToggle, null));
                case GRID_WIDTH -> built.add(new FieldRow(gridWidthLabel, gridWidthField, null));
                case GRID_HEIGHT -> built.add(new FieldRow(gridHeightLabel, gridHeightField, null));
                case ENERGY -> built.add(new FieldRow(energyLabel, energyField, null));
                case CREOSOTE_AMOUNT -> built.add(new FieldRow(creosoteAmountLabel, creosoteAmountField, null));
                case MOLD -> {
                    built.add(new FieldRow(moldLabel, moldDropdown, null));
                    built.add(new FieldRow(moldLabel, moldCommitField, null));
                }
                case BLUEPRINT_CATEGORY -> {
                    built.add(new FieldRow(blueprintCategoryLabel, blueprintCategoryDropdown, null));
                    built.add(new FieldRow(blueprintCategoryLabel, blueprintCategoryCommitField, null));
                }
                case CLOCHE_RENDER_TYPE ->
                        built.add(new FieldRow(clocheRenderTypeLabel, clocheRenderTypeDropdown, null));
                case CLOCHE_RENDER_BLOCK ->
                        built.add(new FieldRow(clocheRenderBlockLabel, clocheRenderBlockSlot, null));
                case FLUID_INPUT_AMOUNT ->
                        built.add(new FieldRow(fluidInputAmountLabel, fluidInputAmountField, null));
                case FLUID_OUTPUT_AMOUNT ->
                        built.add(new FieldRow(fluidOutputAmountLabel, fluidOutputAmountField, null));
            }
        }
        for (int i = 0; i < outputRows.size(); i++) {
            OutputRow row = outputRows.get(i);
            built.add(new FieldRow(chanceLabel, outputChanceFields.get(i), new ItemStackTexture(row.icon())));
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

    private int controlWidth(Widget control) {
        return CONTROL_W;
    }

    private int controlX(int cardX, int cardW, int pad, FieldRow row) {
        return cardX + cardW - pad - CONTROL_W - 4;
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

    private static TextFieldWidget numberField(int x, int y, Supplier<String> supplier,
            Consumer<String> responder, String initial) {
        return numberField(x, y, supplier, responder, initial, 6);
    }

    private static TextFieldWidget numberField(int x, int y, Supplier<String> supplier,
            Consumer<String> responder, String initial, int maxLength) {
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

    private static void configureCommit(LabCommitFieldWidget field) {
        field.setClientSideWidget();
        field.setMaxStringLength(40);
        field.setBordered(false);
        field.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        field.setTextColor(LabColors.TEXT_PRIMARY);
        field.setCurrentString("");
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

    public record OutputRow(ItemStack icon, Supplier<Float> chanceSupplier, Consumer<Float> chanceSetter) {
    }

    private record FieldRow(TextTexture label, Widget control, ItemStackTexture icon) {
    }
}