package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabRowCardSettingsWidget;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;
import com.abo47.kubejslab.loot.model.LabLootAction;
import com.abo47.kubejslab.loot.model.LabLootField;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootSettingsWidget extends LabRowCardSettingsWidget {
    private static final List<String> LOOT_TYPES = List.of(
            LabLootService.LOOT_TYPE_BLOCK,
            LabLootService.LOOT_TYPE_ENTITY,
            LabLootService.LOOT_TYPE_CHEST,
            LabLootService.LOOT_TYPE_FISHING,
            LabLootService.LOOT_TYPE_GIFT,
            LabLootService.LOOT_TYPE_GENERIC);
    private static final List<String> ROLLS_TYPES = List.of("constant", "uniform", "binomial");
    private static final List<String> ENTRY_TYPES = List.of("item", "tag", "empty", "loot_table");
    private static final List<String> COUNT_TYPES = List.of("constant", "uniform");

    private final LabOptionDropdownWidget lootTypeDropdown;
    private final TextFieldWidget targetIdField;
    private final TextFieldWidget customIdField;
    private final LabOptionDropdownWidget poolRollsTypeDropdown;
    private final TextFieldWidget poolRollsValueField;
    private final TextFieldWidget poolRollsMinField;
    private final TextFieldWidget poolRollsMaxField;
    private final TextFieldWidget poolRollsNField;
    private final TextFieldWidget poolRollsPField;
    private final LabOptionDropdownWidget entryTypeDropdown;
    private final TextFieldWidget entryItemField;
    private final TextFieldWidget entryTagField;
    private final TextFieldWidget entryLootTableField;
    private final LabOptionDropdownWidget entryCountTypeDropdown;
    private final TextFieldWidget entryCountValueField;
    private final TextFieldWidget entryCountMinField;
    private final TextFieldWidget entryCountMaxField;
    private final TextFieldWidget entryWeightField;
    private final TextFieldWidget entryQualityField;
    private final LabToggleSwitchWidget poolSurvivesExplosionToggle;
    private final TextFieldWidget poolRandomChanceField;
    private final LabToggleSwitchWidget poolKilledByPlayerToggle;
    private final LabToggleSwitchWidget poolFurnaceSmeltToggle;
    private final LabToggleSwitchWidget poolLootingEnchantToggle;
    private final TextFieldWidget poolLootingCountField;
    private final TextFieldWidget poolLootingLimitField;

    private String targetId = "";
    private String customId = "";
    private String poolRollsValueText = "1";
    private String poolRollsMinText = "0";
    private String poolRollsMaxText = "0";
    private String poolRollsNText = "1";
    private String poolRollsPText = "0.5";
    private String entryItem = "";
    private String entryTag = "";
    private String entryLootTable = "";
    private String entryCountValueText = "1";
    private String entryCountMinText = "0";
    private String entryCountMaxText = "0";
    private String entryWeightText = "1";
    private String entryQualityText = "0";
    private boolean poolSurvivesExplosion = true;
    private String poolRandomChanceText = "1";
    private boolean poolKilledByPlayer = false;
    private boolean poolFurnaceSmelt = false;
    private boolean poolLootingEnchant = false;
    private String poolLootingCountText = "0";
    private String poolLootingLimitText = "0";

    private List<LabLootField> fields = List.of();

    public LabLootSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatable(LabGuiKeys.LAB_LOOT_CLEAR).getString(),
                Component.translatable(LabGuiKeys.LAB_LOOT_SAVE).getString());

        int pad = 6;
        int labelW = w - pad * 2 - CONTROL_W - 4;

        lootTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        lootTypeDropdown.setOptions(LOOT_TYPES);
        lootTypeDropdown.setOnSelect(value -> rebuildRows());
        addWidget(lootTypeDropdown);
        addPopupDropdown(lootTypeDropdown);

        targetIdField = commitField(this::commitTargetId);
        addWidget(targetIdField);

        customIdField = commitField(this::commitCustomId);
        addWidget(customIdField);

        poolRollsTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        poolRollsTypeDropdown.setOptions(ROLLS_TYPES);
        poolRollsTypeDropdown.setOnSelect(value -> rebuildRows());
        addWidget(poolRollsTypeDropdown);
        addPopupDropdown(poolRollsTypeDropdown);

        poolRollsValueField = numberField(0, 0, () -> poolRollsValueText, v -> poolRollsValueText = v,
                poolRollsValueText);
        addWidget(poolRollsValueField);

        poolRollsMinField = numberField(0, 0, () -> poolRollsMinText, v -> poolRollsMinText = v, poolRollsMinText);
        addWidget(poolRollsMinField);

        poolRollsMaxField = numberField(0, 0, () -> poolRollsMaxText, v -> poolRollsMaxText = v, poolRollsMaxText);
        addWidget(poolRollsMaxField);

        poolRollsNField = numberField(0, 0, () -> poolRollsNText, v -> poolRollsNText = v, poolRollsNText);
        addWidget(poolRollsNField);

        poolRollsPField = numberField(0, 0, () -> poolRollsPText, v -> poolRollsPText = v, poolRollsPText);
        addWidget(poolRollsPField);

        entryTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        entryTypeDropdown.setOptions(ENTRY_TYPES);
        entryTypeDropdown.setOnSelect(value -> rebuildRows());
        addWidget(entryTypeDropdown);
        addPopupDropdown(entryTypeDropdown);

        entryItemField = commitField(this::commitEntryItem);
        addWidget(entryItemField);

        entryTagField = commitField(this::commitEntryTag);
        addWidget(entryTagField);

        entryLootTableField = commitField(this::commitEntryLootTable);
        addWidget(entryLootTableField);

        entryCountTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        entryCountTypeDropdown.setOptions(COUNT_TYPES);
        entryCountTypeDropdown.setOnSelect(value -> rebuildRows());
        addWidget(entryCountTypeDropdown);
        addPopupDropdown(entryCountTypeDropdown);

        entryCountValueField = numberField(0, 0, () -> entryCountValueText, v -> entryCountValueText = v,
                entryCountValueText);
        addWidget(entryCountValueField);

        entryCountMinField = numberField(0, 0, () -> entryCountMinText, v -> entryCountMinText = v, entryCountMinText);
        addWidget(entryCountMinField);

        entryCountMaxField = numberField(0, 0, () -> entryCountMaxText, v -> entryCountMaxText = v, entryCountMaxText);
        addWidget(entryCountMaxField);

        entryWeightField = numberField(0, 0, () -> entryWeightText, v -> entryWeightText = v, entryWeightText);
        addWidget(entryWeightField);

        entryQualityField = numberField(0, 0, () -> entryQualityText, v -> entryQualityText = v, entryQualityText);
        addWidget(entryQualityField);

        poolSurvivesExplosionToggle = new LabToggleSwitchWidget(0, 0,
                () -> poolSurvivesExplosion, v -> poolSurvivesExplosion = v, null);
        addWidget(poolSurvivesExplosionToggle);

        poolRandomChanceField = numberField(0, 0, () -> poolRandomChanceText, v -> poolRandomChanceText = v,
                poolRandomChanceText, 4);
        addWidget(poolRandomChanceField);

        poolKilledByPlayerToggle = new LabToggleSwitchWidget(0, 0,
                () -> poolKilledByPlayer, v -> poolKilledByPlayer = v, null);
        addWidget(poolKilledByPlayerToggle);

        poolFurnaceSmeltToggle = new LabToggleSwitchWidget(0, 0,
                () -> poolFurnaceSmelt, v -> poolFurnaceSmelt = v, null);
        addWidget(poolFurnaceSmeltToggle);

        poolLootingEnchantToggle = new LabToggleSwitchWidget(0, 0,
                () -> poolLootingEnchant, v -> poolLootingEnchant = v, null);
        addWidget(poolLootingEnchantToggle);

        poolLootingCountField = numberField(0, 0, () -> poolLootingCountText, v -> poolLootingCountText = v,
                poolLootingCountText, 4);
        addWidget(poolLootingCountField);

        poolLootingLimitField = numberField(0, 0, () -> poolLootingLimitText, v -> poolLootingLimitText = v,
                poolLootingLimitText);
        addWidget(poolLootingLimitField);
    }

    private void commitTargetId(String value) {
        targetId = value;
    }

    private void commitCustomId(String value) {
        customId = value;
    }

    private void commitEntryItem(String value) {
        entryItem = value;
    }

    private void commitEntryTag(String value) {
        entryTag = value;
    }

    private void commitEntryLootTable(String value) {
        entryLootTable = value;
    }

    public void setLootType(String type) {
        lootTypeDropdown.setSelected(type);
    }

    public String getLootType() {
        String sel = lootTypeDropdown.getSelected();
        return sel == null || sel.isBlank() ? LabLootService.LOOT_TYPE_BLOCK : sel;
    }

    public void setFields(List<LabLootField> fields) {
        this.fields = fields == null ? List.of() : fields;
        rebuildRows();
    }

    private void rebuildRows() {
        List<FieldRow> rows = new ArrayList<>();
        String lootType = getLootType();
        String rollsType = poolRollsTypeDropdown.getSelected() == null ? "constant"
                : poolRollsTypeDropdown.getSelected();
        String entryType = entryTypeDropdown.getSelected() == null ? "item" : entryTypeDropdown.getSelected();
        String countType = entryCountTypeDropdown.getSelected() == null ? "constant"
                : entryCountTypeDropdown.getSelected();

        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_TYPE).getString(), LabColors.TEXT_PRIMARY),
                lootTypeDropdown, null));
        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_TARGET_ID).getString(), LabColors.TEXT_PRIMARY),
                targetIdField, null));
        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_CUSTOM_ID).getString(), LabColors.TEXT_PRIMARY),
                customIdField, null));

        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_POOL_ROLLS_TYPE).getString(),
                        LabColors.TEXT_PRIMARY),
                poolRollsTypeDropdown, null));
        if ("uniform".equals(rollsType)) {
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_POOL_ROLLS_MIN).getString(),
                            LabColors.TEXT_PRIMARY),
                    poolRollsMinField, null));
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_POOL_ROLLS_MAX).getString(),
                            LabColors.TEXT_PRIMARY),
                    poolRollsMaxField, null));
        } else if ("binomial".equals(rollsType)) {
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_POOL_ROLLS_N).getString(),
                            LabColors.TEXT_PRIMARY),
                    poolRollsNField, null));
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_POOL_ROLLS_P).getString(),
                            LabColors.TEXT_PRIMARY),
                    poolRollsPField, null));
        } else {
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_POOL_ROLLS_VALUE).getString(),
                            LabColors.TEXT_PRIMARY),
                    poolRollsValueField, null));
        }
        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_TYPE).getString(), LabColors.TEXT_PRIMARY),
                entryTypeDropdown, null));
        switch (entryType) {
            case "tag" -> rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_TAG).getString(),
                            LabColors.TEXT_PRIMARY),
                    entryTagField, null));
            case "loot_table" -> rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_LOOT_TABLE).getString(),
                            LabColors.TEXT_PRIMARY),
                    entryLootTableField, null));
            case "empty" -> {
            }
            default -> rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_ITEM).getString(),
                            LabColors.TEXT_PRIMARY),
                    entryItemField, null));
        }
        if (!"empty".equals(entryType) && !"tag".equals(entryType)) {
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_COUNT_TYPE).getString(),
                            LabColors.TEXT_PRIMARY),
                    entryCountTypeDropdown, null));
            if ("uniform".equals(countType)) {
                rows.add(new FieldRow(
                        new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_COUNT_MIN).getString(),
                                LabColors.TEXT_PRIMARY),
                        entryCountMinField, null));
                rows.add(new FieldRow(
                        new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_COUNT_MAX).getString(),
                                LabColors.TEXT_PRIMARY),
                        entryCountMaxField, null));
            } else {
                rows.add(new FieldRow(
                        new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_COUNT_VALUE).getString(),
                                LabColors.TEXT_PRIMARY),
                        entryCountValueField, null));
            }
        }
        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_WEIGHT).getString(), LabColors.TEXT_PRIMARY),
                entryWeightField, null));
        if ("item".equals(entryType)) {
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_ENTRY_QUALITY).getString(),
                            LabColors.TEXT_PRIMARY),
                    entryQualityField, null));
        }

        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_SURVIVES_EXPLOSION).getString(),
                        LabColors.TEXT_PRIMARY),
                poolSurvivesExplosionToggle, null));
        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_RANDOM_CHANCE).getString(),
                        LabColors.TEXT_PRIMARY),
                poolRandomChanceField, null));
        if (LabLootService.LOOT_TYPE_ENTITY.equals(lootType)) {
            rows.add(new FieldRow(
                    new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_KILLED_BY_PLAYER).getString(),
                            LabColors.TEXT_PRIMARY),
                    poolKilledByPlayerToggle, null));
        }
        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_FURNACE_SMELT).getString(),
                        LabColors.TEXT_PRIMARY),
                poolFurnaceSmeltToggle, null));
        rows.add(new FieldRow(
                new TextTexture(Component.translatable(LabGuiKeys.LAB_LOOT_LOOTING_ENCHANT).getString(),
                        LabColors.TEXT_PRIMARY),
                poolLootingEnchantToggle, null));

        setRows(rows);
    }

    public LabLootFieldValues getValues() {
        String rollsType = poolRollsTypeDropdown.getSelected() == null ? "constant"
                : poolRollsTypeDropdown.getSelected();
        String entryType = entryTypeDropdown.getSelected() == null ? "item" : entryTypeDropdown.getSelected();
        String countType = entryCountTypeDropdown.getSelected() == null ? "constant"
                : entryCountTypeDropdown.getSelected();
        return new LabLootFieldValues(
                targetId,
                customId,
                rollsType,
                parseFloat(poolRollsValueText, 1f),
                parseFloat(poolRollsMinText, 0f),
                parseFloat(poolRollsMaxText, 0f),
                parseInt(poolRollsNText, 1),
                parseFloat(poolRollsPText, 0.5f),
                entryType,
                entryItem,
                entryTag,
                entryLootTable,
                countType,
                parseFloat(entryCountValueText, 1f),
                parseFloat(entryCountMinText, 0f),
                parseFloat(entryCountMaxText, 0f),
                parseInt(entryWeightText, 1),
                parseInt(entryQualityText, 0),
                poolSurvivesExplosion,
                clampChance(parseFloat(poolRandomChanceText, 1f)),
                poolKilledByPlayer,
                poolFurnaceSmelt,
                poolLootingEnchant,
                parseFloat(poolLootingCountText, 0f),
                parseInt(poolLootingLimitText, 0));
    }

    public List<String> getTags() {
        return List.of();
    }

    public List<LabLootAction> getActions() {
        return List.of();
    }

    public void applyValues(LabLootFieldValues v) {
        targetId = v.targetId();
        customId = v.customId();
        poolRollsTypeDropdown.setSelected(v.poolRollsType());
        poolRollsValueText = formatFloat(v.poolRollsValue());
        poolRollsMinText = formatFloat(v.poolRollsMin());
        poolRollsMaxText = formatFloat(v.poolRollsMax());
        poolRollsNText = Integer.toString(v.poolRollsN());
        poolRollsPText = formatFloat(v.poolRollsP());
        entryTypeDropdown.setSelected(v.entryType());
        entryItem = v.entryItem();
        entryTag = v.entryTag();
        entryLootTable = v.entryLootTable();
        entryCountTypeDropdown.setSelected(v.entryCountType());
        entryCountValueText = formatFloat(v.entryCountValue());
        entryCountMinText = formatFloat(v.entryCountMin());
        entryCountMaxText = formatFloat(v.entryCountMax());
        entryWeightText = Integer.toString(v.entryWeight());
        entryQualityText = Integer.toString(v.entryQuality());
        poolSurvivesExplosion = v.poolSurvivesExplosion();
        poolRandomChanceText = formatFloat(v.poolRandomChance());
        poolKilledByPlayer = v.poolKilledByPlayer();
        poolFurnaceSmelt = v.poolFurnaceSmelt();
        poolLootingEnchant = v.poolLootingEnchant();
        poolLootingCountText = formatFloat(v.poolLootingCount());
        poolLootingLimitText = Integer.toString(v.poolLootingLimit());

        targetIdField.setCurrentString(targetId);
        customIdField.setCurrentString(customId);
        poolRollsValueField.setCurrentString(poolRollsValueText);
        poolRollsMinField.setCurrentString(poolRollsMinText);
        poolRollsMaxField.setCurrentString(poolRollsMaxText);
        poolRollsNField.setCurrentString(poolRollsNText);
        poolRollsPField.setCurrentString(poolRollsPText);
        entryItemField.setCurrentString(entryItem);
        entryTagField.setCurrentString(entryTag);
        entryLootTableField.setCurrentString(entryLootTable);
        entryCountValueField.setCurrentString(entryCountValueText);
        entryCountMinField.setCurrentString(entryCountMinText);
        entryCountMaxField.setCurrentString(entryCountMaxText);
        entryWeightField.setCurrentString(entryWeightText);
        entryQualityField.setCurrentString(entryQualityText);
        poolRandomChanceField.setCurrentString(poolRandomChanceText);
        poolLootingCountField.setCurrentString(poolLootingCountText);
        poolLootingLimitField.setCurrentString(poolLootingLimitText);

        rebuildRows();
    }

    public void setClearHandler(Runnable r) {
        setOnClear(r);
    }

    public void setSaveHandler(Runnable r) {
        setOnSave(r);
    }

    private static TextFieldWidget commitField(Consumer<String> onCommit) {
        LabCommitFieldWidget field = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }
}
