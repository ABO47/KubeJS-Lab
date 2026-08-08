package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabBlockSafeSlotWidget;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabRecipeTooltips;
import com.abo47.kubejslab.client.ui.base.LabRowCardSettingsWidget;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;
import com.abo47.kubejslab.client.ui.machines.LabSurfaceSlot;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;


public final class LabRecipeSettingsWidget extends LabRowCardSettingsWidget {
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
    private final LabCommitFieldWidget clocheRenderTypeCommitField;
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
    private final TextTexture newCategoryLabel;
    private final TextTexture clocheRenderTypeLabel;
    private final TextTexture customRenderTypeLabel;
    private final TextTexture clocheRenderBlockLabel;
    private final TextTexture fluidInputAmountLabel;
    private final TextTexture fluidOutputAmountLabel;
    private final TextTexture chanceLabel;

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
    private LabOptionDropdownWidget.DropdownRightClick moldContextRequester;
    private String blueprintCategoryText = "";
    private String clocheRenderType = "generic";
    private String clocheRenderBlockText = "";
    private String fluidInputAmountText = "0";
    private String fluidOutputAmountText = "0";
    private List<LabRecipeField> fields = List.of();
    private List<OutputRow> outputRows = List.of();
    private final List<TextFieldWidget> outputChanceFields = new ArrayList<>();
    private Runnable gridSizeListener;

    public LabRecipeSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatable(LabGuiKeys.LAB_RECIPE_CLEAR).getString(),
                Component.translatable(LabGuiKeys.LAB_RECIPE_SAVE).getString());

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
        newCategoryLabel = rowLabel(LabGuiKeys.LAB_RECIPE_NEW_CATEGORY, labelW);
        clocheRenderTypeLabel = rowLabel(LabGuiKeys.LAB_RECIPE_CLOCHE_RENDER_TYPE, labelW);
        customRenderTypeLabel = rowLabel(LabGuiKeys.LAB_RECIPE_CUSTOM_RENDER_TYPE, labelW);
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
        moldDropdown.setLabelMapper(mold -> {
            int colon = mold.indexOf(':');
            return colon >= 0 ? mold.substring(colon + 1) : mold;
        });
        moldDropdown.setOnItemRightClick((option, mx, my) -> {
            if (LabOptionLibrary.isCustomMold(option) && moldContextRequester != null) {
                moldContextRequester.onRightClick(option, mx, my);
            }
        });
        addWidget(moldDropdown);
        addPopupDropdown(moldDropdown);
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
        addPopupDropdown(blueprintCategoryDropdown);
        refreshBlueprintOptions();

        blueprintCategoryCommitField = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H,
                null, this::commitBlueprintCategory);
        configureCommit(blueprintCategoryCommitField);
        addWidget(blueprintCategoryCommitField);

        clocheRenderTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        clocheRenderTypeDropdown.setOnSelect(value -> clocheRenderType = value);
        addWidget(clocheRenderTypeDropdown);
        addPopupDropdown(clocheRenderTypeDropdown);
        refreshClocheRenderOptions();

        clocheRenderTypeCommitField = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H,
                null, this::commitRenderType);
        configureCommit(clocheRenderTypeCommitField);
        addWidget(clocheRenderTypeCommitField);

        clocheRenderBlockSlot = new LabBlockSafeSlotWidget(0, 0, CONTROL_W, FIELD_H);
        clocheRenderBlockSlot.setOnChange(() -> clocheRenderBlockText = clocheRenderBlockSlot.getBlockId());
        addWidget(clocheRenderBlockSlot);

        fluidInputAmountField = numberField(0, 0, () -> fluidInputAmountText,
                value -> fluidInputAmountText = value, fluidInputAmountText);
        addWidget(fluidInputAmountField);

        fluidOutputAmountField = numberField(0, 0, () -> fluidOutputAmountText,
                value -> fluidOutputAmountText = value, fluidOutputAmountText);
        addWidget(fluidOutputAmountField);
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
            clocheRenderType = "generic";
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
        clocheRenderTypeCommitField.setVisible(fields.contains(LabRecipeField.CLOCHE_RENDER_TYPE));
        clocheRenderBlockSlot.setVisible(fields.contains(LabRecipeField.CLOCHE_RENDER_BLOCK));
        fluidInputAmountField.setVisible(fields.contains(LabRecipeField.FLUID_INPUT_AMOUNT));
        fluidOutputAmountField.setVisible(fields.contains(LabRecipeField.FLUID_OUTPUT_AMOUNT));
        for (LabRecipeField field : fields) {
            controlFor(field).setHoverTooltips(LabRecipeTooltips.forField(field));
        }
        resetScroll();
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
        LabOptionLibrary.addCustomMold(value);
        if (!moldOptions.contains(value)) {
            moldOptions.add(value);
        }
        refreshMoldOptions();
    }

    private void refreshClocheRenderOptions() {
        List<String> merged = new ArrayList<>(LabOptionLibrary.clocheRenderTypes());
        if (!clocheRenderType.isBlank() && !merged.contains(clocheRenderType)) {
            merged.add(clocheRenderType);
        }
        clocheRenderTypeDropdown.setOptions(merged);
        clocheRenderTypeDropdown.setSelected(clocheRenderType);
    }

    private void commitRenderType(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        clocheRenderType = value;
        refreshClocheRenderOptions();
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

    public void setMoldContextRequester(LabOptionDropdownWidget.DropdownRightClick moldContextRequester) {
        this.moldContextRequester = moldContextRequester;
    }

    public void deleteCustomMold(String mold) {
        LabOptionLibrary.removeCustomMold(mold);
        moldOptions.remove(mold);
        if (moldText.equals(mold)) {
            moldText = "";
        }
        refreshMoldOptions();
    }

    public void deleteBlueprintCategory(String category) {
        LabBlueprintCategories.remove(category);
        blueprintOptions.remove(category);
        if (blueprintCategoryText.equals(category)) {
            blueprintCategoryText = "";
        }
        refreshBlueprintOptions();
    }

    public void consumeSurfaceSlots(List<LabSurfaceSlot> slots) {
        for (LabSurfaceSlot slot : slots) {
            if (slot.value().isBlank()) {
                continue;
            }
            switch (slot.tint()) {
                case BLUEPRINT -> {
                    if (!slot.value().equals(blueprintCategoryText)) {
                        blueprintCategoryText = slot.value();
                        refreshBlueprintOptions();
                    }
                }
                case MOLD -> {
                    if (!slot.value().equals(moldText)) {
                        moldText = slot.value();
                        refreshMoldOptions();
                    }
                }
                default -> {
                }
            }
        }
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
        refreshClocheRenderOptions();
        clocheRenderBlockSlot.setBlockId(clocheRenderBlockText);
    }

    public void setFluidOutputAmount(int amount) {
        fluidOutputAmountText = Integer.toString(Math.max(0, amount));
        fluidOutputAmountField.setCurrentString(fluidOutputAmountText);
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
                    built.add(new FieldRow(newCategoryLabel, blueprintCategoryCommitField, null));
                }
                case CLOCHE_RENDER_TYPE -> {
                    built.add(new FieldRow(clocheRenderTypeLabel, clocheRenderTypeDropdown, null));
                    built.add(new FieldRow(customRenderTypeLabel, clocheRenderTypeCommitField, null));
                }
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
        setRows(built);
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

    public record OutputRow(ItemStack icon, java.util.function.Supplier<Float> chanceSupplier,
            java.util.function.Consumer<Float> chanceSetter) {
    }
}