package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import com.abo47.kubejslab.block.model.LabBlockAction;
import com.abo47.kubejslab.block.model.LabBlockField;
import com.abo47.kubejslab.block.model.LabBlockFieldValues;
import com.abo47.kubejslab.block.runtime.LabBlockService;
import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabRowCardSettingsWidget;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;


public final class LabBlockSettingsWidget extends LabRowCardSettingsWidget {
    private final LabOptionDropdownWidget typeDropdown;
    private final TextFieldWidget nameField;
    private final LabActionButton textureAllButton;
    private final LabActionButton textureTopButton;
    private final LabActionButton textureBottomButton;
    private final LabActionButton textureSidesButton;
    private final TextFieldWidget hardnessField;
    private final TextFieldWidget resistanceField;
    private final LabToggleSwitchWidget unbreakableToggle;
    private final TextFieldWidget lightLevelField;
    private final LabOptionDropdownWidget soundTypeDropdown;
    private final LabToggleSwitchWidget requiresToolToggle;
    private final LabToggleSwitchWidget noCollisionToggle;
    private final LabToggleSwitchWidget waterloggedToggle;
    private final LabToggleSwitchWidget noDropsToggle;
    private final LabToggleSwitchWidget notSolidToggle;
    private final LabToggleSwitchWidget opaqueToggle;
    private final TextFieldWidget slipperinessField;
    private final TextFieldWidget speedFactorField;
    private final TextFieldWidget jumpFactorField;
    private final TextFieldWidget tagsField;
    private final LabToggleSwitchWidget hideCreativeToggle;
    private final LabToggleSwitchWidget removeRecipesToggle;

    private final TextTexture typeLabel;
    private final TextTexture nameLabel;
    private final TextTexture textureAllLabel;
    private final TextTexture textureTopLabel;
    private final TextTexture textureBottomLabel;
    private final TextTexture textureSidesLabel;
    private final TextTexture hardnessLabel;
    private final TextTexture resistanceLabel;
    private final TextTexture unbreakableLabel;
    private final TextTexture lightLevelLabel;
    private final TextTexture soundTypeLabel;
    private final TextTexture requiresToolLabel;
    private final TextTexture noCollisionLabel;
    private final TextTexture waterloggedLabel;
    private final TextTexture noDropsLabel;
    private final TextTexture notSolidLabel;
    private final TextTexture opaqueLabel;
    private final TextTexture slipperinessLabel;
    private final TextTexture speedFactorLabel;
    private final TextTexture jumpFactorLabel;
    private final TextTexture tagsLabel;
    private final TextTexture hideCreativeLabel;
    private final TextTexture removeRecipesLabel;

    private boolean unbreakable;
    private boolean requiresTool;
    private boolean noCollision;
    private boolean waterlogged;
    private boolean noDrops;
    private boolean notSolid;
    private boolean opaque = true;
    private boolean hideCreative;
    private boolean removeRecipes;
    private String name = "";
    private String textureAll = "";
    private String textureTop = "";
    private String textureBottom = "";
    private String textureSides = "";
    private String tags = "";
    private String soundType = "";
    private String hardnessText = formatFloat(LabBlockFieldValues.DEFAULT_HARDNESS);
    private String resistanceText = formatFloat(LabBlockFieldValues.DEFAULT_RESISTANCE);
    private String lightLevelText = "0";
    private String slipperinessText = "0";
    private String speedFactorText = "0";
    private String jumpFactorText = "0";

    private List<LabBlockField> fields = List.of();
    private Consumer<LabBlockField> onTexturePick;

    public LabBlockSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatable(LabGuiKeys.LAB_BLOCK_CLEAR).getString(),
                Component.translatable(LabGuiKeys.LAB_BLOCK_SAVE).getString());

        int pad = 6;
        int labelW = w - pad * 2 - CONTROL_W - 4;

        typeLabel = rowLabel(LabGuiKeys.LAB_BLOCK_TYPE, labelW);
        nameLabel = rowLabel(LabGuiKeys.LAB_BLOCK_NAME, labelW);
        textureAllLabel = rowLabel(LabGuiKeys.LAB_BLOCK_TEXTURE_ALL, labelW);
        textureTopLabel = rowLabel(LabGuiKeys.LAB_BLOCK_TEXTURE_TOP, labelW);
        textureBottomLabel = rowLabel(LabGuiKeys.LAB_BLOCK_TEXTURE_BOTTOM, labelW);
        textureSidesLabel = rowLabel(LabGuiKeys.LAB_BLOCK_TEXTURE_SIDES, labelW);
        hardnessLabel = rowLabel(LabGuiKeys.LAB_BLOCK_HARDNESS, labelW);
        resistanceLabel = rowLabel(LabGuiKeys.LAB_BLOCK_RESISTANCE, labelW);
        unbreakableLabel = rowLabel(LabGuiKeys.LAB_BLOCK_UNBREAKABLE, labelW);
        lightLevelLabel = rowLabel(LabGuiKeys.LAB_BLOCK_LIGHT_LEVEL, labelW);
        soundTypeLabel = rowLabel(LabGuiKeys.LAB_BLOCK_SOUND_TYPE, labelW);
        requiresToolLabel = rowLabel(LabGuiKeys.LAB_BLOCK_REQUIRES_TOOL, labelW);
        noCollisionLabel = rowLabel(LabGuiKeys.LAB_BLOCK_NO_COLLISION, labelW);
        waterloggedLabel = rowLabel(LabGuiKeys.LAB_BLOCK_WATERLOGGED, labelW);
        noDropsLabel = rowLabel(LabGuiKeys.LAB_BLOCK_NO_DROPS, labelW);
        notSolidLabel = rowLabel(LabGuiKeys.LAB_BLOCK_NOT_SOLID, labelW);
        opaqueLabel = rowLabel(LabGuiKeys.LAB_BLOCK_OPAQUE, labelW);
        slipperinessLabel = rowLabel(LabGuiKeys.LAB_BLOCK_SLIPPERINESS, labelW);
        speedFactorLabel = rowLabel(LabGuiKeys.LAB_BLOCK_SPEED_FACTOR, labelW);
        jumpFactorLabel = rowLabel(LabGuiKeys.LAB_BLOCK_JUMP_FACTOR, labelW);
        tagsLabel = rowLabel(LabGuiKeys.LAB_BLOCK_TAGS, labelW);
        hideCreativeLabel = rowLabel(LabGuiKeys.LAB_BLOCK_DISABLE_CREATIVE, labelW);
        removeRecipesLabel = rowLabel(LabGuiKeys.LAB_BLOCK_DISABLE_RECIPES, labelW);

        typeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        typeDropdown.setOptions(LabBlockService.TYPES);
        addWidget(typeDropdown);
        addPopupDropdown(typeDropdown);

        nameField = commitField(this::commitName);
        addWidget(nameField);

        textureAllButton = pickButton(LabBlockField.TEXTURE_ALL);
        textureTopButton = pickButton(LabBlockField.TEXTURE_TOP);
        textureBottomButton = pickButton(LabBlockField.TEXTURE_BOTTOM);
        textureSidesButton = pickButton(LabBlockField.TEXTURE_SIDES);

        hardnessField = numberField(0, 0, () -> hardnessText, value -> hardnessText = value, hardnessText);
        addWidget(hardnessField);

        resistanceField = numberField(0, 0, () -> resistanceText, value -> resistanceText = value, resistanceText);
        addWidget(resistanceField);

        unbreakableToggle = new LabToggleSwitchWidget(0, 0, () -> unbreakable, value -> {
            unbreakable = value;
            rebuildRows();
        }, null);
        addWidget(unbreakableToggle);

        lightLevelField = numberField(0, 0, () -> lightLevelText, value -> lightLevelText = value,
                lightLevelText, 2);
        addWidget(lightLevelField);

        soundTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        soundTypeDropdown.setOptions(List.of(LabBlockService.soundTypes()));
        soundTypeDropdown.setSelected("");
        addWidget(soundTypeDropdown);
        addPopupDropdown(soundTypeDropdown);

        requiresToolToggle = new LabToggleSwitchWidget(0, 0, () -> requiresTool, value -> requiresTool = value, null);
        addWidget(requiresToolToggle);

        noCollisionToggle = new LabToggleSwitchWidget(0, 0, () -> noCollision, value -> noCollision = value, null);
        addWidget(noCollisionToggle);

        waterloggedToggle = new LabToggleSwitchWidget(0, 0, () -> waterlogged, value -> waterlogged = value, null);
        addWidget(waterloggedToggle);

        noDropsToggle = new LabToggleSwitchWidget(0, 0, () -> noDrops, value -> noDrops = value, null);
        addWidget(noDropsToggle);

        notSolidToggle = new LabToggleSwitchWidget(0, 0, () -> notSolid, value -> notSolid = value, null);
        addWidget(notSolidToggle);

        opaqueToggle = new LabToggleSwitchWidget(0, 0, () -> opaque, value -> opaque = value, null);
        addWidget(opaqueToggle);

        slipperinessField = numberField(0, 0, () -> slipperinessText, value -> slipperinessText = value,
                slipperinessText);
        addWidget(slipperinessField);

        speedFactorField = numberField(0, 0, () -> speedFactorText, value -> speedFactorText = value,
                speedFactorText);
        addWidget(speedFactorField);

        jumpFactorField = numberField(0, 0, () -> jumpFactorText, value -> jumpFactorText = value, jumpFactorText);
        addWidget(jumpFactorField);

        tagsField = commitField(this::commitTags);
        addWidget(tagsField);

        hideCreativeToggle = new LabToggleSwitchWidget(0, 0, () -> hideCreative, value -> hideCreative = value, null);
        addWidget(hideCreativeToggle);

        removeRecipesToggle = new LabToggleSwitchWidget(0, 0, () -> removeRecipes, value -> removeRecipes = value, null);
        addWidget(removeRecipesToggle);
    }

    private LabActionButton pickButton(LabBlockField field) {
        LabActionButton button = new LabActionButton(0, 0, CONTROL_W, FIELD_H,
                Component.translatable(LabGuiKeys.LAB_ITEM_TEXTURE_PICK).getString(), () -> {
            if (onTexturePick != null) onTexturePick.accept(field);
        });
        addWidget(button);
        return button;
    }

    private void commitName(String value) {
        if (value != null) name = value;
    }

    private void commitTags(String value) {
        if (value != null) tags = value;
    }

    private static TextFieldWidget commitField(Consumer<String> onCommit) {
        LabCommitFieldWidget field = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }

    public void setOnTexturePick(Consumer<LabBlockField> onTexturePick) {
        this.onTexturePick = onTexturePick;
    }

    public void setType(String type) {
        typeDropdown.setSelected(type);
        rebuildRows();
    }

    public String getType() {
        return typeDropdown.getSelected() == null ? "basic" : typeDropdown.getSelected();
    }

    public void setFields(List<LabBlockField> fields) {
        this.fields = fields;
        rebuildRows();
    }

    private boolean perFaceTextures() {
        return "basic".equals(getType()) || "cardinal".equals(getType());
    }

    private boolean isVisible(LabBlockField field) {
        return switch (field) {
            case TEXTURE_ALL -> !"detector".equals(getType());
            case TEXTURE_TOP, TEXTURE_BOTTOM, TEXTURE_SIDES -> perFaceTextures();
            case HARDNESS, RESISTANCE -> !unbreakable;
            default -> true;
        };
    }

    private void rebuildRows() {
        List<FieldRow> rows = new ArrayList<>();
        for (LabBlockField field : fields) {
            if (!isVisible(field)) {
                continue;
            }
            FieldRow row = fieldRow(field);
            if (row != null && row.control() != null) {
                row.control().setHoverTooltips(List.of(Component.translatable(LabBlockTooltips.key(field))));
            }
            if (row != null) {
                rows.add(row);
            }
        }
        resetScroll();
        setRows(rows);
    }

    private FieldRow fieldRow(LabBlockField field) {
        return switch (field) {
            case TYPE -> new FieldRow(typeLabel, typeDropdown, null);
            case DISPLAY_NAME -> new FieldRow(nameLabel, nameField, null);
            case TEXTURE_ALL -> new FieldRow(textureAllLabel, textureAllButton, null);
            case TEXTURE_TOP -> new FieldRow(textureTopLabel, textureTopButton, null);
            case TEXTURE_BOTTOM -> new FieldRow(textureBottomLabel, textureBottomButton, null);
            case TEXTURE_SIDES -> new FieldRow(textureSidesLabel, textureSidesButton, null);
            case HARDNESS -> new FieldRow(hardnessLabel, hardnessField, null);
            case RESISTANCE -> new FieldRow(resistanceLabel, resistanceField, null);
            case UNBREAKABLE -> new FieldRow(unbreakableLabel, unbreakableToggle, null);
            case LIGHT_LEVEL -> new FieldRow(lightLevelLabel, lightLevelField, null);
            case SOUND_TYPE -> new FieldRow(soundTypeLabel, soundTypeDropdown, null);
            case REQUIRES_TOOL -> new FieldRow(requiresToolLabel, requiresToolToggle, null);
            case NO_COLLISION -> new FieldRow(noCollisionLabel, noCollisionToggle, null);
            case WATERLOGGED -> new FieldRow(waterloggedLabel, waterloggedToggle, null);
            case NO_DROPS -> new FieldRow(noDropsLabel, noDropsToggle, null);
            case NOT_SOLID -> new FieldRow(notSolidLabel, notSolidToggle, null);
            case OPAQUE -> new FieldRow(opaqueLabel, opaqueToggle, null);
            case SLIPPERINESS -> new FieldRow(slipperinessLabel, slipperinessField, null);
            case SPEED_FACTOR -> new FieldRow(speedFactorLabel, speedFactorField, null);
            case JUMP_FACTOR -> new FieldRow(jumpFactorLabel, jumpFactorField, null);
            case TAGS -> new FieldRow(tagsLabel, tagsField, null);
            case DISABLE_CREATIVE_HIDE -> new FieldRow(hideCreativeLabel, hideCreativeToggle, null);
            case DISABLE_RECIPE_REMOVAL -> new FieldRow(removeRecipesLabel, removeRecipesToggle, null);
        };
    }

    public LabBlockFieldValues getValues() {
        return new LabBlockFieldValues(
                name,
                textureAll,
                textureTop,
                textureBottom,
                textureSides,
                parseFloat(hardnessText, LabBlockFieldValues.DEFAULT_HARDNESS),
                parseFloat(resistanceText, LabBlockFieldValues.DEFAULT_RESISTANCE),
                unbreakable,
                parseInt(lightLevelText, 0),
                soundTypeDropdown.getSelected() == null ? "" : soundTypeDropdown.getSelected(),
                requiresTool,
                noCollision,
                waterlogged,
                noDrops,
                notSolid,
                opaque,
                parseFloat(slipperinessText, 0f),
                parseFloat(speedFactorText, 0f),
                parseFloat(jumpFactorText, 0f),
                tags);
    }

    public void applyValues(LabBlockFieldValues values) {
        if (values == null) return;
        name = values.displayName();
        textureAll = values.textureAll();
        textureTop = values.textureTop();
        textureBottom = values.textureBottom();
        textureSides = values.textureSides();
        hardnessText = formatFloat(values.hardness());
        resistanceText = formatFloat(values.resistance());
        unbreakable = values.unbreakable();
        lightLevelText = Integer.toString(values.lightLevel());
        soundType = values.soundType();
        requiresTool = values.requiresTool();
        noCollision = values.noCollision();
        waterlogged = values.waterlogged();
        noDrops = values.noDrops();
        notSolid = values.notSolid();
        opaque = values.opaque();
        slipperinessText = formatFloat(values.slipperiness());
        speedFactorText = formatFloat(values.speedFactor());
        jumpFactorText = formatFloat(values.jumpFactor());
        tags = values.tags();

        nameField.setCurrentString(name);
        tagsField.setCurrentString(tags);
        hardnessField.setCurrentString(hardnessText);
        resistanceField.setCurrentString(resistanceText);
        lightLevelField.setCurrentString(lightLevelText);
        slipperinessField.setCurrentString(slipperinessText);
        speedFactorField.setCurrentString(speedFactorText);
        jumpFactorField.setCurrentString(jumpFactorText);
        applyTextureButtonLabel(textureAllButton, textureAll);
        applyTextureButtonLabel(textureTopButton, textureTop);
        applyTextureButtonLabel(textureBottomButton, textureBottom);
        applyTextureButtonLabel(textureSidesButton, textureSides);
        soundTypeDropdown.setSelected(soundType);
        rebuildRows();
    }

    private static void applyTextureButtonLabel(LabActionButton button, String relativePath) {
        int slash = relativePath.lastIndexOf('/');
        button.setLabel(slash < 0 ? relativePath : relativePath.substring(slash + 1));
    }

    public List<String> getTags() {
        List<String> result = new ArrayList<>();
        for (String tag : tags.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isBlank()) result.add(trimmed);
        }
        return result;
    }

    public void applyTags(List<String> tags) {
        StringBuilder joined = new StringBuilder();
        for (String tag : tags) {
            if (joined.length() > 0) joined.append(',');
            joined.append(tag);
        }
        this.tags = joined.toString();
        tagsField.setCurrentString(this.tags);
    }

    public List<LabBlockAction> getActions() {
        List<LabBlockAction> result = new ArrayList<>();
        if (hideCreative) result.add(LabBlockAction.HIDE_CREATIVE_TAB);
        if (removeRecipes) result.add(LabBlockAction.REMOVE_RECIPES);
        return result;
    }

    public void applyActions(List<LabBlockAction> actions) {
        hideCreative = false;
        removeRecipes = false;
        for (LabBlockAction action : actions) {
            switch (action) {
                case HIDE_CREATIVE_TAB -> hideCreative = true;
                case REMOVE_RECIPES -> removeRecipes = true;
            }
        }
        rebuildRows();
    }

    public List<LabBlockField> fullFields() {
        return List.of(LabBlockField.values());
    }

    public List<LabBlockField> builtInFields() {
        return List.of(
                LabBlockField.HARDNESS,
                LabBlockField.RESISTANCE,
                LabBlockField.UNBREAKABLE,
                LabBlockField.LIGHT_LEVEL,
                LabBlockField.SOUND_TYPE,
                LabBlockField.REQUIRES_TOOL,
                LabBlockField.NO_COLLISION,
                LabBlockField.SLIPPERINESS,
                LabBlockField.SPEED_FACTOR,
                LabBlockField.JUMP_FACTOR,
                LabBlockField.DISABLE_CREATIVE_HIDE,
                LabBlockField.DISABLE_RECIPE_REMOVAL);
    }

    public void setTextureValue(LabBlockField field, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        switch (field) {
            case TEXTURE_ALL -> textureAll = relativePath;
            case TEXTURE_TOP -> textureTop = relativePath;
            case TEXTURE_BOTTOM -> textureBottom = relativePath;
            case TEXTURE_SIDES -> textureSides = relativePath;
            default -> {
                return;
            }
        }
        applyTextureButtonLabel(switch (field) {
            case TEXTURE_ALL -> textureAllButton;
            case TEXTURE_TOP -> textureTopButton;
            case TEXTURE_BOTTOM -> textureBottomButton;
            default -> textureSidesButton;
        }, relativePath);
    }

    public String getAllTexture() {
        return textureAll;
    }
}
