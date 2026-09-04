package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import java.util.Set;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import com.abo47.kubejslab.block.model.BlockAction;
import com.abo47.kubejslab.block.model.BlockField;
import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.block.runtime.BlockService;
import com.abo47.kubejslab.client.ui.items.ItemKeys;
import com.abo47.kubejslab.client.ui.widgets.ActionButton;
import com.abo47.kubejslab.client.ui.widgets.CommitField;
import com.abo47.kubejslab.client.ui.widgets.OptionDropdownWidget;
import com.abo47.kubejslab.client.ui.widgets.RowCardSettings;
import com.abo47.kubejslab.client.ui.widgets.ToggleSwitchWidget;


public final class BlockSettingsWidget extends RowCardSettings {
    private final OptionDropdownWidget typeDropdown;
    private final TextFieldWidget nameField;
    private final ActionButton textureAllButton;
    private final ActionButton textureTopButton;
    private final ActionButton textureBottomButton;
    private final ActionButton textureSidesButton;
    private final TextFieldWidget hardnessField;
    private final TextFieldWidget resistanceField;
    private final ToggleSwitchWidget unbreakableToggle;
    private final TextFieldWidget lightLevelField;
    private final OptionDropdownWidget soundTypeDropdown;
    private final ToggleSwitchWidget requiresToolToggle;
    private final ToggleSwitchWidget noCollisionToggle;
    private final ToggleSwitchWidget waterloggedToggle;
    private final ToggleSwitchWidget noDropsToggle;
    private final ToggleSwitchWidget notSolidToggle;
    private final ToggleSwitchWidget opaqueToggle;
    private final TextFieldWidget slipperinessField;
    private final TextFieldWidget speedFactorField;
    private final TextFieldWidget jumpFactorField;
    private final TextFieldWidget tagsField;
    private final OptionDropdownWidget creativeTabDropdown;
    private final TextFieldWidget lootItemField;
    private final TextFieldWidget lootCountMinField;
    private final TextFieldWidget lootCountMaxField;
    private final TextFieldWidget lootChanceField;
    private final ActionButton dustPickButton;
    private final OptionDropdownWidget blockSetTypeDropdown;
    private final OptionDropdownWidget woodTypeDropdown;
    private final ToggleSwitchWidget hideCreativeToggle;
    private final ToggleSwitchWidget removeRecipesToggle;
    private final ToggleSwitchWidget hideViewerToggle;

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
    private final TextTexture creativeTabLabel;
    private final TextTexture lootItemLabel;
    private final TextTexture lootCountMinLabel;
    private final TextTexture lootCountMaxLabel;
    private final TextTexture lootChanceLabel;
    private final TextTexture dustColorLabel;
    private final TextTexture blockSetTypeLabel;
    private final TextTexture woodTypeLabel;
    private final TextTexture hideCreativeLabel;
    private final TextTexture removeRecipesLabel;
    private final TextTexture hideViewerLabel;

    private boolean unbreakable;
    private boolean requiresTool;
    private boolean noCollision;
    private boolean waterlogged;
    private boolean noDrops;
    private boolean notSolid;
    private boolean opaque = true;
    private boolean hideCreative;
    private boolean removeRecipes;
    private boolean hideViewer;
    private String name = "";
    private String textureAll = "";
    private String textureTop = "";
    private String textureBottom = "";
    private String textureSides = "";
    private String tags = "";
    private String soundType = "";
    private String creativeTab = "";
    private String lootItem = "";
    private String dustColor = "";
    private String blockSetType = "";
    private String woodType = "";
    private String hardnessText = formatFloat(BlockFieldValues.DEFAULT_HARDNESS);
    private String resistanceText = formatFloat(BlockFieldValues.DEFAULT_RESISTANCE);
    private String lightLevelText = "0";
    private String slipperinessText = "0";
    private String speedFactorText = "0";
    private String jumpFactorText = "0";
    private String lootCountMinText = "1";
    private String lootCountMaxText = "1";
    private String lootChanceText = "100";

    private List<BlockField> fields = List.of();
    private Consumer<BlockField> onTexturePick;
    private boolean builtInOnly;

    private static final Set<BlockField> BUILTIN_MODIFIABLE = Set.of(
            BlockField.HARDNESS, BlockField.RESISTANCE, BlockField.UNBREAKABLE,
            BlockField.LIGHT_LEVEL, BlockField.REQUIRES_TOOL, BlockField.NO_COLLISION,
            BlockField.SLIPPERINESS, BlockField.SPEED_FACTOR, BlockField.JUMP_FACTOR,
            BlockField.DISABLE_CREATIVE_HIDE, BlockField.DISABLE_RECIPE_REMOVAL);

    public BlockSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatable(BlockKeys.BLOCK_CLEAR).getString(),
                Component.translatable(BlockKeys.BLOCK_SAVE).getString());

        int pad = 6;
        int labelW = w - pad * 2 - CONTROL_W - 4;

        typeLabel = rowLabel(BlockKeys.BLOCK_TYPE, labelW);
        nameLabel = rowLabel(BlockKeys.BLOCK_NAME, labelW);
        textureAllLabel = rowLabel(BlockKeys.BLOCK_TEXTURE_ALL, labelW);
        textureTopLabel = rowLabel(BlockKeys.BLOCK_TEXTURE_TOP, labelW);
        textureBottomLabel = rowLabel(BlockKeys.BLOCK_TEXTURE_BOTTOM, labelW);
        textureSidesLabel = rowLabel(BlockKeys.BLOCK_TEXTURE_SIDES, labelW);
        hardnessLabel = rowLabel(BlockKeys.BLOCK_HARDNESS, labelW);
        resistanceLabel = rowLabel(BlockKeys.BLOCK_RESISTANCE, labelW);
        unbreakableLabel = rowLabel(BlockKeys.BLOCK_UNBREAKABLE, labelW);
        lightLevelLabel = rowLabel(BlockKeys.BLOCK_LIGHT_LEVEL, labelW);
        soundTypeLabel = rowLabel(BlockKeys.BLOCK_SOUND_TYPE, labelW);
        requiresToolLabel = rowLabel(BlockKeys.BLOCK_REQUIRES_TOOL, labelW);
        noCollisionLabel = rowLabel(BlockKeys.BLOCK_NO_COLLISION, labelW);
        waterloggedLabel = rowLabel(BlockKeys.BLOCK_WATERLOGGED, labelW);
        noDropsLabel = rowLabel(BlockKeys.BLOCK_NO_DROPS, labelW);
        notSolidLabel = rowLabel(BlockKeys.BLOCK_NOT_SOLID, labelW);
        opaqueLabel = rowLabel(BlockKeys.BLOCK_OPAQUE, labelW);
        slipperinessLabel = rowLabel(BlockKeys.BLOCK_SLIPPERINESS, labelW);
        speedFactorLabel = rowLabel(BlockKeys.BLOCK_SPEED_FACTOR, labelW);
        jumpFactorLabel = rowLabel(BlockKeys.BLOCK_JUMP_FACTOR, labelW);
        tagsLabel = rowLabel(BlockKeys.BLOCK_TAGS, labelW);
        creativeTabLabel = rowLabel(BlockKeys.BLOCK_CREATIVE_TAB, labelW);
        lootItemLabel = rowLabel(BlockKeys.BLOCK_LOOT_ITEM, labelW);
        lootCountMinLabel = rowLabel(BlockKeys.BLOCK_LOOT_COUNT_MIN, labelW);
        lootCountMaxLabel = rowLabel(BlockKeys.BLOCK_LOOT_COUNT_MAX, labelW);
        lootChanceLabel = rowLabel(BlockKeys.BLOCK_LOOT_CHANCE, labelW);
        dustColorLabel = rowLabel(BlockKeys.BLOCK_DUST_COLOR, labelW);
        blockSetTypeLabel = rowLabel(BlockKeys.BLOCK_SET_TYPE, labelW);
        woodTypeLabel = rowLabel(BlockKeys.BLOCK_WOOD_TYPE, labelW);
        hideCreativeLabel = rowLabel(BlockKeys.BLOCK_DISABLE_CREATIVE, labelW);
        removeRecipesLabel = rowLabel(BlockKeys.BLOCK_DISABLE_RECIPES, labelW);
        hideViewerLabel = rowLabel(BlockKeys.BLOCK_DISABLE_VIEWER, labelW);

        typeDropdown = new OptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        typeDropdown.setOptions(BlockService.TYPES);
        addWidget(typeDropdown);
        addPopupDropdown(typeDropdown);

        nameField = commitField(this::commitName);
        addWidget(nameField);

        textureAllButton = pickButton(BlockField.TEXTURE_ALL);
        textureTopButton = pickButton(BlockField.TEXTURE_TOP);
        textureBottomButton = pickButton(BlockField.TEXTURE_BOTTOM);
        textureSidesButton = pickButton(BlockField.TEXTURE_SIDES);
        dustPickButton = pickButton(BlockField.DUST_COLOR);

        hardnessField = numberField(0, 0, () -> hardnessText, value -> hardnessText = value, hardnessText);
        addWidget(hardnessField);

        resistanceField = numberField(0, 0, () -> resistanceText, value -> resistanceText = value, resistanceText);
        addWidget(resistanceField);

        unbreakableToggle = new ToggleSwitchWidget(0, 0, () -> unbreakable, value -> {
            unbreakable = value;
            rebuildRows();
        }, null);
        addWidget(unbreakableToggle);

        lightLevelField = numberField(0, 0, () -> lightLevelText, value -> lightLevelText = value,
                lightLevelText, 2);
        addWidget(lightLevelField);

        soundTypeDropdown = new OptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        soundTypeDropdown.setOptions(List.of(BlockService.soundTypes()));
        soundTypeDropdown.setSelected("");
        addWidget(soundTypeDropdown);
        addPopupDropdown(soundTypeDropdown);

        requiresToolToggle = new ToggleSwitchWidget(0, 0, () -> requiresTool, value -> requiresTool = value, null);
        addWidget(requiresToolToggle);

        noCollisionToggle = new ToggleSwitchWidget(0, 0, () -> noCollision, value -> noCollision = value, null);
        addWidget(noCollisionToggle);

        waterloggedToggle = new ToggleSwitchWidget(0, 0, () -> waterlogged, value -> waterlogged = value, null);
        addWidget(waterloggedToggle);

        noDropsToggle = new ToggleSwitchWidget(0, 0, () -> noDrops, value -> {
            noDrops = value;
            rebuildRows();
        }, null);
        addWidget(noDropsToggle);

        notSolidToggle = new ToggleSwitchWidget(0, 0, () -> notSolid, value -> notSolid = value, null);
        addWidget(notSolidToggle);

        opaqueToggle = new ToggleSwitchWidget(0, 0, () -> opaque, value -> opaque = value, null);
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

        creativeTabDropdown = new OptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        creativeTabDropdown.setOptions(List.of(BlockService.creativeTabs()));
        creativeTabDropdown.setLabelMapper(tabId -> {
            int colon = tabId.indexOf(':');
            return colon < 0 ? tabId : tabId.substring(colon + 1);
        });
        creativeTabDropdown.setSelected("");
        addWidget(creativeTabDropdown);
        addPopupDropdown(creativeTabDropdown);

        lootItemField = commitField(value -> {
            if (value != null) lootItem = value;
        });
        addWidget(lootItemField);

        lootCountMinField = numberField(0, 0, () -> lootCountMinText, value -> lootCountMinText = value,
                lootCountMinText, 2);
        addWidget(lootCountMinField);

        lootCountMaxField = numberField(0, 0, () -> lootCountMaxText, value -> lootCountMaxText = value,
                lootCountMaxText, 2);
        addWidget(lootCountMaxField);

        lootChanceField = numberField(0, 0, () -> lootChanceText, value -> lootChanceText = value,
                lootChanceText, 3);
        addWidget(lootChanceField);

        blockSetTypeDropdown = new OptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        blockSetTypeDropdown.setOptions(List.of(BlockService.blockSetTypes()));
        blockSetTypeDropdown.setSelected("");
        addWidget(blockSetTypeDropdown);
        addPopupDropdown(blockSetTypeDropdown);

        woodTypeDropdown = new OptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        woodTypeDropdown.setOptions(List.of(BlockService.woodTypes()));
        woodTypeDropdown.setSelected("oak");
        addWidget(woodTypeDropdown);
        addPopupDropdown(woodTypeDropdown);

        hideCreativeToggle = new ToggleSwitchWidget(0, 0, () -> hideCreative, value -> hideCreative = value, null);
        addWidget(hideCreativeToggle);

        removeRecipesToggle = new ToggleSwitchWidget(0, 0, () -> removeRecipes, value -> removeRecipes = value, null);
        addWidget(removeRecipesToggle);

        hideViewerToggle = new ToggleSwitchWidget(0, 0, () -> hideViewer, value -> hideViewer = value, null);
        addWidget(hideViewerToggle);
    }

    private ActionButton pickButton(BlockField field) {
        ActionButton button = new ActionButton(0, 0, CONTROL_W, FIELD_H,
                Component.translatable(ItemKeys.ITEM_TEXTURE_PICK).getString(), () -> {
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
        CommitField field = new CommitField(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }

    public void setOnTexturePick(Consumer<BlockField> onTexturePick) {
        this.onTexturePick = onTexturePick;
    }

    public void setType(String type) {
        typeDropdown.setSelected(type);
        rebuildRows();
    }

    public String getType() {
        return typeDropdown.getSelected() == null ? "basic" : typeDropdown.getSelected();
    }

    public void setBuiltInOnly(boolean builtInOnly) {
        this.builtInOnly = builtInOnly;
        rebuildRows();
    }

    public void setFields(List<BlockField> fields) {
        this.fields = fields;
        rebuildRows();
    }

    private boolean perFaceTextures() {
        return "basic".equals(getType()) || "cardinal".equals(getType());
    }

    private boolean isDisabled(BlockField field) {
        return switch (field) {
            case TEXTURE_ALL -> "detector".equals(getType());
            case TEXTURE_TOP, TEXTURE_BOTTOM, TEXTURE_SIDES -> !perFaceTextures();
            case HARDNESS, RESISTANCE -> unbreakable;
            case CREATIVE_TAB -> builtInOnly;
            case LOOT_ITEM, LOOT_COUNT_MIN, LOOT_COUNT_MAX, LOOT_CHANCE -> builtInOnly || noDrops;
            case DUST_COLOR -> builtInOnly || !"falling".equals(getType());
            case BLOCK_SET_TYPE -> builtInOnly
                    || !("button".equals(getType()) || "pressure_plate".equals(getType()));
            case WOOD_TYPE -> builtInOnly || !"fence_gate".equals(getType());
            default -> builtInOnly && !BUILTIN_MODIFIABLE.contains(field);
        };
    }

    private void rebuildRows() {
        syncCommitFields();
        List<FieldRow> rows = new ArrayList<>();
        for (BlockField field : fields) {
            FieldRow row = new FieldRow(fieldLabel(field), fieldControl(field), null, isDisabled(field));
            if (row.control() != null) {
                row.control().setHoverTooltips(List.of(Component.translatable(BlockTooltips.key(field))));
            }
            rows.add(row);
        }
        setRows(rows);
    }

    private TextTexture fieldLabel(BlockField field) {
        return switch (field) {
            case TYPE -> typeLabel;
            case DISPLAY_NAME -> nameLabel;
            case TEXTURE_ALL -> textureAllLabel;
            case TEXTURE_TOP -> textureTopLabel;
            case TEXTURE_BOTTOM -> textureBottomLabel;
            case TEXTURE_SIDES -> textureSidesLabel;
            case HARDNESS -> hardnessLabel;
            case RESISTANCE -> resistanceLabel;
            case UNBREAKABLE -> unbreakableLabel;
            case LIGHT_LEVEL -> lightLevelLabel;
            case SOUND_TYPE -> soundTypeLabel;
            case REQUIRES_TOOL -> requiresToolLabel;
            case NO_COLLISION -> noCollisionLabel;
            case WATERLOGGED -> waterloggedLabel;
            case NO_DROPS -> noDropsLabel;
            case NOT_SOLID -> notSolidLabel;
            case OPAQUE -> opaqueLabel;
            case SLIPPERINESS -> slipperinessLabel;
            case SPEED_FACTOR -> speedFactorLabel;
            case JUMP_FACTOR -> jumpFactorLabel;
            case TAGS -> tagsLabel;
            case CREATIVE_TAB -> creativeTabLabel;
            case LOOT_ITEM -> lootItemLabel;
            case LOOT_COUNT_MIN -> lootCountMinLabel;
            case LOOT_COUNT_MAX -> lootCountMaxLabel;
            case LOOT_CHANCE -> lootChanceLabel;
            case DUST_COLOR -> dustColorLabel;            case BLOCK_SET_TYPE -> blockSetTypeLabel;
            case WOOD_TYPE -> woodTypeLabel;
            case DISABLE_CREATIVE_HIDE -> hideCreativeLabel;
            case DISABLE_RECIPE_REMOVAL -> removeRecipesLabel;
            case DISABLE_VIEWER_HIDE -> hideViewerLabel;
        };
    }

    private com.lowdragmc.lowdraglib.gui.widget.Widget fieldControl(BlockField field) {
        return switch (field) {
            case TYPE -> typeDropdown;
            case DISPLAY_NAME -> nameField;
            case TEXTURE_ALL -> textureAllButton;
            case TEXTURE_TOP -> textureTopButton;
            case TEXTURE_BOTTOM -> textureBottomButton;
            case TEXTURE_SIDES -> textureSidesButton;
            case HARDNESS -> hardnessField;
            case RESISTANCE -> resistanceField;
            case UNBREAKABLE -> unbreakableToggle;
            case LIGHT_LEVEL -> lightLevelField;
            case SOUND_TYPE -> soundTypeDropdown;
            case REQUIRES_TOOL -> requiresToolToggle;
            case NO_COLLISION -> noCollisionToggle;
            case WATERLOGGED -> waterloggedToggle;
            case NO_DROPS -> noDropsToggle;
            case NOT_SOLID -> notSolidToggle;
            case OPAQUE -> opaqueToggle;
            case SLIPPERINESS -> slipperinessField;
            case SPEED_FACTOR -> speedFactorField;
            case JUMP_FACTOR -> jumpFactorField;
            case TAGS -> tagsField;
            case CREATIVE_TAB -> creativeTabDropdown;
            case LOOT_ITEM -> lootItemField;
            case LOOT_COUNT_MIN -> lootCountMinField;
            case LOOT_COUNT_MAX -> lootCountMaxField;
            case LOOT_CHANCE -> lootChanceField;
            case DUST_COLOR -> dustPickButton;
            case BLOCK_SET_TYPE -> blockSetTypeDropdown;
            case WOOD_TYPE -> woodTypeDropdown;
            case DISABLE_CREATIVE_HIDE -> hideCreativeToggle;
            case DISABLE_RECIPE_REMOVAL -> removeRecipesToggle;
            case DISABLE_VIEWER_HIDE -> hideViewerToggle;
        };
    }

    private void syncCommitFields() {
        String liveName = nameField != null && nameField.getRawCurrentString() != null ? nameField.getRawCurrentString().trim() : null;
        if (liveName != null) name = liveName;
        String liveTags = tagsField != null && tagsField.getRawCurrentString() != null ? tagsField.getRawCurrentString().trim() : null;
        if (liveTags != null) tags = liveTags;
        String liveLoot = lootItemField != null && lootItemField.getRawCurrentString() != null ? lootItemField.getRawCurrentString().trim() : null;
        if (liveLoot != null) lootItem = liveLoot;
        String liveHardness = hardnessField != null && hardnessField.getRawCurrentString() != null ? hardnessField.getRawCurrentString().trim() : null;
        if (liveHardness != null && !liveHardness.isBlank()) hardnessText = liveHardness;
        String liveResistance = resistanceField != null && resistanceField.getRawCurrentString() != null ? resistanceField.getRawCurrentString().trim() : null;
        if (liveResistance != null && !liveResistance.isBlank()) resistanceText = liveResistance;
        String liveLight = lightLevelField != null && lightLevelField.getRawCurrentString() != null ? lightLevelField.getRawCurrentString().trim() : null;
        if (liveLight != null && !liveLight.isBlank()) lightLevelText = liveLight;
        String liveSlipperiness = slipperinessField != null && slipperinessField.getRawCurrentString() != null ? slipperinessField.getRawCurrentString().trim() : null;
        if (liveSlipperiness != null && !liveSlipperiness.isBlank()) slipperinessText = liveSlipperiness;
        String liveSpeed = speedFactorField != null && speedFactorField.getRawCurrentString() != null ? speedFactorField.getRawCurrentString().trim() : null;
        if (liveSpeed != null && !liveSpeed.isBlank()) speedFactorText = liveSpeed;
        String liveJump = jumpFactorField != null && jumpFactorField.getRawCurrentString() != null ? jumpFactorField.getRawCurrentString().trim() : null;
        if (liveJump != null && !liveJump.isBlank()) jumpFactorText = liveJump;
        String liveMin = lootCountMinField != null && lootCountMinField.getRawCurrentString() != null ? lootCountMinField.getRawCurrentString().trim() : null;
        if (liveMin != null && !liveMin.isBlank()) lootCountMinText = liveMin;
        String liveMax = lootCountMaxField != null && lootCountMaxField.getRawCurrentString() != null ? lootCountMaxField.getRawCurrentString().trim() : null;
        if (liveMax != null && !liveMax.isBlank()) lootCountMaxText = liveMax;
        String liveChance = lootChanceField != null && lootChanceField.getRawCurrentString() != null ? lootChanceField.getRawCurrentString().trim() : null;
        if (liveChance != null && !liveChance.isBlank()) lootChanceText = liveChance;
    }

    public BlockFieldValues getValues() {
        syncCommitFields();
        return new BlockFieldValues(
                name,
                textureAll,
                textureTop,
                textureBottom,
                textureSides,
                parseFloat(hardnessText, BlockFieldValues.DEFAULT_HARDNESS),
                parseFloat(resistanceText, BlockFieldValues.DEFAULT_RESISTANCE),
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
                tags,
                creativeTabDropdown.getSelected() == null ? "" : creativeTabDropdown.getSelected(),
                lootItem,
                parseInt(lootCountMinText, 0),
                parseInt(lootCountMaxText, 0),
                parseFloat(lootChanceText, 100f),
                dustColor,
                blockSetTypeDropdown.getSelected() == null ? "" : blockSetTypeDropdown.getSelected(),
                woodTypeDropdown.getSelected() == null ? "" : woodTypeDropdown.getSelected());
    }

    public void applyValues(BlockFieldValues values) {
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
        creativeTab = values.creativeTab();
        lootItem = values.lootItem();
        lootCountMinText = Integer.toString(values.lootCountMin());
        lootCountMaxText = Integer.toString(values.lootCountMax());
        lootChanceText = formatFloat(values.lootChance());
        dustColor = values.dustColor();
        blockSetType = values.blockSetType();
        woodType = values.woodType();

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
        creativeTabDropdown.setSelected(creativeTab);
        lootItemField.setCurrentString(lootItem);
        lootCountMinField.setCurrentString(lootCountMinText);
        lootCountMaxField.setCurrentString(lootCountMaxText);
        lootChanceField.setCurrentString(lootChanceText);
        applyDustButtonLabel();
        blockSetTypeDropdown.setSelected(blockSetType);
        woodTypeDropdown.setSelected(woodType.isBlank() ? "oak" : woodType);
        rebuildRows();
    }

    private static void applyTextureButtonLabel(ActionButton button, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            button.setLabel(Component.translatable(ItemKeys.ITEM_TEXTURE_PICK).getString());
            return;
        }
        int slash = relativePath.lastIndexOf('/');
        button.setLabel(relativePath.substring(slash + 1));
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

    public List<BlockAction> getActions() {
        List<BlockAction> result = new ArrayList<>();
        if (hideCreative) result.add(BlockAction.HIDE_CREATIVE_TAB);
        if (removeRecipes) result.add(BlockAction.REMOVE_RECIPES);
        if (hideViewer) result.add(BlockAction.HIDE_VIEWER);
        return result;
    }

    public void applyActions(List<BlockAction> actions) {
        hideCreative = false;
        removeRecipes = false;
        hideViewer = false;
        for (BlockAction action : actions) {
            switch (action) {
                case HIDE_CREATIVE_TAB -> hideCreative = true;
                case REMOVE_RECIPES -> removeRecipes = true;
                case HIDE_VIEWER -> hideViewer = true;
            }
        }
        rebuildRows();
    }

    public List<BlockField> fullFields() {
        return List.of(BlockField.values());
    }

    public void setTextureValue(BlockField field, String relativePath) {
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

    public String getDustColor() {
        return dustColor;
    }

    public void setDustValue(String hex) {
        if (hex == null || hex.isBlank()) {
            return;
        }
        dustColor = hex.trim().toUpperCase(java.util.Locale.ROOT);
        applyDustButtonLabel();
    }

    private void applyDustButtonLabel() {
        if (dustColor.isBlank()) {
            dustPickButton.setLabel(Component.translatable(ItemKeys.ITEM_TEXTURE_PICK).getString());
        } else {
            dustPickButton.setLabel(dustColor);
        }
    }
}
