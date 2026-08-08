package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabRowCardSettingsWidget;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;
import com.abo47.kubejslab.item.model.LabItemAction;
import com.abo47.kubejslab.item.model.LabItemField;
import com.abo47.kubejslab.item.model.LabItemFieldValues;


public final class LabItemSettingsWidget extends LabRowCardSettingsWidget {
    private static final List<String> TYPES = List.of("basic", "sword", "pickaxe", "axe", "shovel", "hoe", "shears",
            "helmet", "chestplate", "leggings", "boots", "music_disc");
    private static final List<String> TOOL_TIERS = List.of("", "wood", "stone", "gold", "iron", "diamond", "netherite");
    private static final List<String> ARMOR_TIERS = List.of("", "leather", "chain", "gold", "iron", "diamond", "netherite");
    private static final List<String> RARITIES = List.of("common", "uncommon", "rare", "epic");
    private static final List<String> OPERATIONS = List.of("addition", "multiply_base", "multiply_total");
    private static final List<String> BEHAVIORS = List.of("none", "cancel", "give", "damage");

    private final LabOptionDropdownWidget typeDropdown;
    private final TextFieldWidget nameField;
    private final TextFieldWidget textureField;
    private final LabActionButton texturePickButton;
    private final LabOptionDropdownWidget rarityDropdown;
    private final TextFieldWidget maxStackField;
    private final TextFieldWidget maxDamageField;
    private final TextFieldWidget burnTimeField;
    private final LabToggleSwitchWidget glowToggle;
    private final LabToggleSwitchWidget fireResistantToggle;
    private final TextFieldWidget containerItemField;
    private final TextFieldWidget tooltipField;
    private final TextFieldWidget tagsField;
    private final TextFieldWidget foodHungerField;
    private final TextFieldWidget foodSaturationField;
    private final LabToggleSwitchWidget foodMeatToggle;
    private final LabToggleSwitchWidget foodFastToEatToggle;
    private final LabToggleSwitchWidget foodAlwaysEdibleToggle;
    private final TextFieldWidget foodEffectField;
    private final TextFieldWidget foodEffectDurationField;
    private final TextFieldWidget foodEffectAmplifierField;
    private final TextFieldWidget foodEffectChanceField;
    private final LabOptionDropdownWidget toolTierDropdown;
    private final TextFieldWidget attackDamageField;
    private final TextFieldWidget attackSpeedField;
    private final TextFieldWidget digSpeedField;
    private final LabOptionDropdownWidget armorTierField;
    private final TextFieldWidget armorProtectionField;
    private final TextFieldWidget armorToughnessField;
    private final TextFieldWidget armorKnockbackField;
    private final TextFieldWidget tierUsesField;
    private final TextFieldWidget tierSpeedField;
    private final TextFieldWidget tierAttackDamageBonusField;
    private final TextFieldWidget tierLevelField;
    private final TextFieldWidget tierEnchantValueField;
    private final TextFieldWidget tierRepairIngredientField;
    private final TextFieldWidget tierDurabilityMultiplierField;
    private final TextFieldWidget tierProtectionsField;
    private final TextFieldWidget tierEquipSoundField;
    private final TextFieldWidget tierToughnessField;
    private final TextFieldWidget tierKnockbackResistanceField;
    private final TextFieldWidget attributeIdField;
    private final TextFieldWidget attributeNameField;
    private final TextFieldWidget attributeAmountField;
    private final LabOptionDropdownWidget attributeOperationDropdown;
    private final LabOptionDropdownWidget behaviorDropdown;
    private final TextFieldWidget behaviorItemField;
    private final TextFieldWidget behaviorDamageField;
    private final LabToggleSwitchWidget hideCreativeToggle;
    private final LabToggleSwitchWidget removeRecipesToggle;
    private final LabToggleSwitchWidget hideViewerToggle;

    private final TextTexture typeLabel;
    private final TextTexture nameLabel;
    private final TextTexture textureLabel;
    private final TextTexture texturePickLabel;
    private final TextTexture rarityLabel;
    private final TextTexture maxStackLabel;
    private final TextTexture maxDamageLabel;
    private final TextTexture burnTimeLabel;
    private final TextTexture glowLabel;
    private final TextTexture fireResistantLabel;
    private final TextTexture containerItemLabel;
    private final TextTexture tooltipLabel;
    private final TextTexture tagsLabel;
    private final TextTexture foodHungerLabel;
    private final TextTexture foodSaturationLabel;
    private final TextTexture foodMeatLabel;
    private final TextTexture foodFastToEatLabel;
    private final TextTexture foodAlwaysEdibleLabel;
    private final TextTexture foodEffectLabel;
    private final TextTexture foodEffectDurationLabel;
    private final TextTexture foodEffectAmplifierLabel;
    private final TextTexture foodEffectChanceLabel;
    private final TextTexture toolTierLabel;
    private final TextTexture attackDamageLabel;
    private final TextTexture attackSpeedLabel;
    private final TextTexture digSpeedLabel;
    private final TextTexture armorTierLabel;
    private final TextTexture armorProtectionLabel;
    private final TextTexture armorToughnessLabel;
    private final TextTexture armorKnockbackLabel;
    private final TextTexture tierUsesLabel;
    private final TextTexture tierSpeedLabel;
    private final TextTexture tierAttackDamageBonusLabel;
    private final TextTexture tierLevelLabel;
    private final TextTexture tierEnchantValueLabel;
    private final TextTexture tierRepairIngredientLabel;
    private final TextTexture tierDurabilityMultiplierLabel;
    private final TextTexture tierProtectionsLabel;
    private final TextTexture tierEquipSoundLabel;
    private final TextTexture tierToughnessLabel;
    private final TextTexture tierKnockbackResistanceLabel;
    private final TextTexture attributeIdLabel;
    private final TextTexture attributeNameLabel;
    private final TextTexture attributeAmountLabel;
    private final TextTexture attributeOperationLabel;
    private final TextTexture behaviorLabel;
    private final TextTexture behaviorItemLabel;
    private final TextTexture behaviorDamageLabel;
    private final TextTexture hideCreativeLabel;
    private final TextTexture removeRecipesLabel;
    private final TextTexture hideViewerLabel;

    private boolean glow;
    private boolean fireResistant;
    private boolean foodMeat;
    private boolean foodFastToEat;
    private boolean foodAlwaysEdible;
    private boolean hideCreative;
    private boolean removeRecipes;
    private boolean hideViewer;
    private String name = "";
    private String texture = "";
    private String containerItem = "";
    private String tooltip = "";
    private String tags = "";
    private String foodEffect = "";
    private String toolTier = "";
    private String armorTier = "";
    private String tierRepairIngredient = "";
    private String tierProtections = "";
    private String tierEquipSound = "";
    private String attributeId = "";
    private String attributeName = "";
    private String attributeOperation = "addition";
    private String behavior = "none";
    private String behaviorItem = "";
    private String maxStackText = "64";
    private String maxDamageText = "0";
    private String burnTimeText = "0";
    private String foodHungerText = "0";
    private String foodSaturationText = "0";
    private String foodEffectDurationText = "0";
    private String foodEffectAmplifierText = "0";
    private String foodEffectChanceText = "0";
    private String attackDamageText = "0";
    private String attackSpeedText = "0";
    private String digSpeedText = "0";
    private String armorProtectionText = "0";
    private String armorToughnessText = "0";
    private String armorKnockbackText = "0";
    private String tierUsesText = "0";
    private String tierSpeedText = "0";
    private String tierAttackDamageBonusText = "0";
    private String tierLevelText = "0";
    private String tierEnchantValueText = "0";
    private String tierDurabilityMultiplierText = "0";
    private String tierToughnessText = "0";
    private String tierKnockbackResistanceText = "0";
    private String attributeAmountText = "0";
    private String behaviorDamageText = "0";

    private List<LabItemField> fields = List.of();
    private Runnable onTexturePick;

    public LabItemSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatable(LabGuiKeys.LAB_ITEM_CLEAR).getString(),
                Component.translatable(LabGuiKeys.LAB_ITEM_SAVE).getString());

        int pad = 6;
        int labelW = w - pad * 2 - CONTROL_W - 4;

        typeLabel = rowLabel(LabGuiKeys.LAB_ITEM_TYPE, labelW);
        nameLabel = rowLabel(LabGuiKeys.LAB_ITEM_NAME, labelW);
        textureLabel = rowLabel(LabGuiKeys.LAB_ITEM_TEXTURE, labelW);
        texturePickLabel = rowLabel(LabGuiKeys.LAB_ITEM_TEXTURE_PICK, labelW);
        rarityLabel = rowLabel(LabGuiKeys.LAB_ITEM_RARITY, labelW);
        maxStackLabel = rowLabel(LabGuiKeys.LAB_ITEM_MAX_STACK, labelW);
        maxDamageLabel = rowLabel(LabGuiKeys.LAB_ITEM_MAX_DAMAGE, labelW);
        burnTimeLabel = rowLabel(LabGuiKeys.LAB_ITEM_BURN_TIME, labelW);
        glowLabel = rowLabel(LabGuiKeys.LAB_ITEM_GLOW, labelW);
        fireResistantLabel = rowLabel(LabGuiKeys.LAB_ITEM_FIRE_RESISTANT, labelW);
        containerItemLabel = rowLabel(LabGuiKeys.LAB_ITEM_CONTAINER_ITEM, labelW);
        tooltipLabel = rowLabel(LabGuiKeys.LAB_ITEM_TOOLTIP, labelW);
        tagsLabel = rowLabel(LabGuiKeys.LAB_ITEM_TAGS, labelW);
        foodHungerLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_HUNGER, labelW);
        foodSaturationLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_SATURATION, labelW);
        foodMeatLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_MEAT, labelW);
        foodFastToEatLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_FAST_TO_EAT, labelW);
        foodAlwaysEdibleLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_ALWAYS_EDIBLE, labelW);
        foodEffectLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_EFFECT, labelW);
        foodEffectDurationLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_EFFECT_DURATION, labelW);
        foodEffectAmplifierLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_EFFECT_AMPLIFIER, labelW);
        foodEffectChanceLabel = rowLabel(LabGuiKeys.LAB_ITEM_FOOD_EFFECT_CHANCE, labelW);
        toolTierLabel = rowLabel(LabGuiKeys.LAB_ITEM_TOOL_TIER, labelW);
        attackDamageLabel = rowLabel(LabGuiKeys.LAB_ITEM_ATTACK_DAMAGE, labelW);
        attackSpeedLabel = rowLabel(LabGuiKeys.LAB_ITEM_ATTACK_SPEED, labelW);
        digSpeedLabel = rowLabel(LabGuiKeys.LAB_ITEM_DIG_SPEED, labelW);
        armorTierLabel = rowLabel(LabGuiKeys.LAB_ITEM_ARMOR_TIER, labelW);
        armorProtectionLabel = rowLabel(LabGuiKeys.LAB_ITEM_ARMOR_PROTECTION, labelW);
        armorToughnessLabel = rowLabel(LabGuiKeys.LAB_ITEM_ARMOR_TOUGHNESS, labelW);
        armorKnockbackLabel = rowLabel(LabGuiKeys.LAB_ITEM_ARMOR_KNOCKBACK, labelW);
        tierUsesLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_USES, labelW);
        tierSpeedLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_SPEED, labelW);
        tierAttackDamageBonusLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_ATTACK_DAMAGE_BONUS, labelW);
        tierLevelLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_LEVEL, labelW);
        tierEnchantValueLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_ENCHANT, labelW);
        tierRepairIngredientLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_REPAIR, labelW);
        tierDurabilityMultiplierLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_DURABILITY, labelW);
        tierProtectionsLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_PROTECTIONS, labelW);
        tierEquipSoundLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_EQUIP_SOUND, labelW);
        tierToughnessLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_TOUGHNESS, labelW);
        tierKnockbackResistanceLabel = rowLabel(LabGuiKeys.LAB_ITEM_TIER_KNOCKBACK, labelW);
        attributeIdLabel = rowLabel(LabGuiKeys.LAB_ITEM_ATTRIBUTE_ID, labelW);
        attributeNameLabel = rowLabel(LabGuiKeys.LAB_ITEM_ATTRIBUTE_NAME, labelW);
        attributeAmountLabel = rowLabel(LabGuiKeys.LAB_ITEM_ATTRIBUTE_AMOUNT, labelW);
        attributeOperationLabel = rowLabel(LabGuiKeys.LAB_ITEM_ATTRIBUTE_OPERATION, labelW);
        behaviorLabel = rowLabel(LabGuiKeys.LAB_ITEM_BEHAVIOR, labelW);
        behaviorItemLabel = rowLabel(LabGuiKeys.LAB_ITEM_BEHAVIOR_ITEM, labelW);
        behaviorDamageLabel = rowLabel(LabGuiKeys.LAB_ITEM_BEHAVIOR_DAMAGE_AMOUNT, labelW);
        hideCreativeLabel = rowLabel(LabGuiKeys.LAB_ITEM_DISABLE_CREATIVE, labelW);
        removeRecipesLabel = rowLabel(LabGuiKeys.LAB_ITEM_DISABLE_RECIPES, labelW);
        hideViewerLabel = rowLabel(LabGuiKeys.LAB_ITEM_DISABLE_VIEWER, labelW);

        typeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        typeDropdown.setOptions(TYPES);
        typeDropdown.setOnSelect(value -> rebuildRowsByType());
        addWidget(typeDropdown);
        addPopupDropdown(typeDropdown);

        nameField = commitField(this::commitName);
        addWidget(nameField);

        textureField = commitField(this::commitTexture);
        addWidget(textureField);

        texturePickButton = new LabActionButton(0, 0, CONTROL_W, FIELD_H,
                Component.translatable(LabGuiKeys.LAB_ITEM_TEXTURE_PICK).getString(), () -> {
            if (onTexturePick != null) onTexturePick.run();
        });
        addWidget(texturePickButton);

        rarityDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        rarityDropdown.setOptions(RARITIES);
        rarityDropdown.setOnSelect(value -> {});
        addWidget(rarityDropdown);
        addPopupDropdown(rarityDropdown);

        maxStackField = numberField(0, 0, () -> maxStackText, value -> maxStackText = value, maxStackText, 3);
        addWidget(maxStackField);

        maxDamageField = numberField(0, 0, () -> maxDamageText, value -> maxDamageText = value, maxDamageText);
        addWidget(maxDamageField);

        burnTimeField = numberField(0, 0, () -> burnTimeText, value -> burnTimeText = value, burnTimeText);
        addWidget(burnTimeField);

        glowToggle = new LabToggleSwitchWidget(0, 0, () -> glow, value -> glow = value, null);
        addWidget(glowToggle);

        fireResistantToggle = new LabToggleSwitchWidget(0, 0, () -> fireResistant, value -> fireResistant = value, null);
        addWidget(fireResistantToggle);

        containerItemField = commitField(this::commitContainerItem);
        addWidget(containerItemField);

        tooltipField = commitField(this::commitTooltip);
        addWidget(tooltipField);

        tagsField = commitField(this::commitTags);
        addWidget(tagsField);

        foodHungerField = numberField(0, 0, () -> foodHungerText, value -> foodHungerText = value, foodHungerText);
        addWidget(foodHungerField);

        foodSaturationField = numberField(0, 0, () -> foodSaturationText, value -> foodSaturationText = value,
                foodSaturationText);
        addWidget(foodSaturationField);

        foodMeatToggle = new LabToggleSwitchWidget(0, 0, () -> foodMeat, value -> foodMeat = value, null);
        addWidget(foodMeatToggle);

        foodFastToEatToggle = new LabToggleSwitchWidget(0, 0, () -> foodFastToEat, value -> foodFastToEat = value, null);
        addWidget(foodFastToEatToggle);

        foodAlwaysEdibleToggle = new LabToggleSwitchWidget(0, 0, () -> foodAlwaysEdible, value -> foodAlwaysEdible = value, null);
        addWidget(foodAlwaysEdibleToggle);

        foodEffectField = commitField(this::commitFoodEffect);
        addWidget(foodEffectField);

        foodEffectDurationField = numberField(0, 0, () -> foodEffectDurationText, value -> foodEffectDurationText = value,
                foodEffectDurationText);
        addWidget(foodEffectDurationField);

        foodEffectAmplifierField = numberField(0, 0, () -> foodEffectAmplifierText, value -> foodEffectAmplifierText = value,
                foodEffectAmplifierText);
        addWidget(foodEffectAmplifierField);

        foodEffectChanceField = numberField(0, 0, () -> foodEffectChanceText, value -> foodEffectChanceText = value,
                foodEffectChanceText);
        addWidget(foodEffectChanceField);

        toolTierDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        toolTierDropdown.setOptions(TOOL_TIERS);
        toolTierDropdown.setSelected("");
        addWidget(toolTierDropdown);
        addPopupDropdown(toolTierDropdown);
        attackDamageField = numberField(0, 0, () -> attackDamageText, value -> attackDamageText = value, attackDamageText);
        addWidget(attackDamageField);

        attackSpeedField = numberField(0, 0, () -> attackSpeedText, value -> attackSpeedText = value, attackSpeedText);
        addWidget(attackSpeedField);

        digSpeedField = numberField(0, 0, () -> digSpeedText, value -> digSpeedText = value, digSpeedText);
        addWidget(digSpeedField);

        armorTierField = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        armorTierField.setOptions(ARMOR_TIERS);
        armorTierField.setOnSelect(value -> {});
        addWidget(armorTierField);
        addPopupDropdown(armorTierField);

        armorProtectionField = numberField(0, 0, () -> armorProtectionText, value -> armorProtectionText = value,
                armorProtectionText);
        addWidget(armorProtectionField);

        armorToughnessField = numberField(0, 0, () -> armorToughnessText, value -> armorToughnessText = value,
                armorToughnessText);
        addWidget(armorToughnessField);

        armorKnockbackField = numberField(0, 0, () -> armorKnockbackText, value -> armorKnockbackText = value,
                armorKnockbackText);
        addWidget(armorKnockbackField);

        tierUsesField = numberField(0, 0, () -> tierUsesText, value -> tierUsesText = value, tierUsesText);
        addWidget(tierUsesField);

        tierSpeedField = numberField(0, 0, () -> tierSpeedText, value -> tierSpeedText = value, tierSpeedText);
        addWidget(tierSpeedField);

        tierAttackDamageBonusField = numberField(0, 0, () -> tierAttackDamageBonusText,
                value -> tierAttackDamageBonusText = value, tierAttackDamageBonusText);
        addWidget(tierAttackDamageBonusField);

        tierLevelField = numberField(0, 0, () -> tierLevelText, value -> tierLevelText = value, tierLevelText);
        addWidget(tierLevelField);

        tierEnchantValueField = numberField(0, 0, () -> tierEnchantValueText, value -> tierEnchantValueText = value,
                tierEnchantValueText);
        addWidget(tierEnchantValueField);

        tierRepairIngredientField = commitField(this::commitTierRepairIngredient);
        addWidget(tierRepairIngredientField);

        tierDurabilityMultiplierField = numberField(0, 0, () -> tierDurabilityMultiplierText,
                value -> tierDurabilityMultiplierText = value, tierDurabilityMultiplierText);
        addWidget(tierDurabilityMultiplierField);

        tierProtectionsField = commitField(this::commitTierProtections);
        addWidget(tierProtectionsField);

        tierEquipSoundField = commitField(this::commitTierEquipSound);
        addWidget(tierEquipSoundField);

        tierToughnessField = numberField(0, 0, () -> tierToughnessText, value -> tierToughnessText = value,
                tierToughnessText);
        addWidget(tierToughnessField);

        tierKnockbackResistanceField = numberField(0, 0, () -> tierKnockbackResistanceText,
                value -> tierKnockbackResistanceText = value, tierKnockbackResistanceText);
        addWidget(tierKnockbackResistanceField);

        attributeIdField = commitField(this::commitAttributeId);
        addWidget(attributeIdField);

        attributeNameField = commitField(this::commitAttributeName);
        addWidget(attributeNameField);

        attributeAmountField = numberField(0, 0, () -> attributeAmountText, value -> attributeAmountText = value,
                attributeAmountText);
        addWidget(attributeAmountField);

        attributeOperationDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        attributeOperationDropdown.setOptions(OPERATIONS);
        attributeOperationDropdown.setOnSelect(value -> {});
        addWidget(attributeOperationDropdown);
        addPopupDropdown(attributeOperationDropdown);

        behaviorDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        behaviorDropdown.setOptions(BEHAVIORS);
        behaviorDropdown.setOnSelect(value -> {
            behavior = value;
            rebuildRows();
        });
        addWidget(behaviorDropdown);
        addPopupDropdown(behaviorDropdown);

        behaviorItemField = commitField(this::commitBehaviorItem);
        addWidget(behaviorItemField);

        behaviorDamageField = numberField(0, 0, () -> behaviorDamageText, value -> behaviorDamageText = value,
                behaviorDamageText);
        addWidget(behaviorDamageField);

        hideCreativeToggle = new LabToggleSwitchWidget(0, 0, () -> hideCreative, value -> hideCreative = value, null);
        addWidget(hideCreativeToggle);

        removeRecipesToggle = new LabToggleSwitchWidget(0, 0, () -> removeRecipes, value -> removeRecipes = value, null);
        addWidget(removeRecipesToggle);

        hideViewerToggle = new LabToggleSwitchWidget(0, 0, () -> hideViewer, value -> hideViewer = value, null);
        addWidget(hideViewerToggle);

        typeByDefault();
    }

    private void typeByDefault() {
        typeDropdown.setSelected(TYPES.get(0));
    }

    private void commitName(String value) {
        if (value != null) name = value;
    }

    private void commitTexture(String value) {
        if (value != null) texture = value;
    }

    private void commitContainerItem(String value) {
        if (value != null) containerItem = value;
    }

    private void commitTooltip(String value) {
        if (value != null) tooltip = value;
    }

    private void commitTags(String value) {
        if (value != null) tags = value;
    }

    private void commitFoodEffect(String value) {
        if (value != null) foodEffect = value;
    }

    private void commitTierRepairIngredient(String value) {
        if (value != null) tierRepairIngredient = value;
    }

    private void commitTierProtections(String value) {
        if (value != null) tierProtections = value;
    }

    private void commitTierEquipSound(String value) {
        if (value != null) tierEquipSound = value;
    }

    private void commitAttributeId(String value) {
        if (value != null) attributeId = value;
    }

    private void commitAttributeName(String value) {
        if (value != null) attributeName = value;
    }

    private void commitBehaviorItem(String value) {
        if (value != null) behaviorItem = value;
    }

    private void refreshToolTierOptions() {
        List<String> merged = new ArrayList<>(TOOL_TIERS);
        for (String id : LabItemStates.customTierIds(false)) {
            if (!merged.contains(id)) merged.add(id);
        }
        if (toolTier != null && !toolTier.isBlank() && !merged.contains(toolTier)) merged.add(toolTier);
        toolTierDropdown.setOptions(merged);
        toolTierDropdown.setSelected(toolTier);
    }

    private void refreshArmorTierOptions() {
        List<String> merged = new ArrayList<>(ARMOR_TIERS);
        for (String id : LabItemStates.customTierIds(true)) {
            if (!merged.contains(id)) merged.add(id);
        }
        if (armorTier != null && !armorTier.isBlank() && !merged.contains(armorTier)) merged.add(armorTier);
        armorTierField.setOptions(merged);
        armorTierField.setSelected(armorTier);
    }

    private static TextFieldWidget commitField(Consumer<String> onCommit) {
        LabCommitFieldWidget field = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }

    public void setOnTexturePick(Runnable onTexturePick) {
        this.onTexturePick = onTexturePick;
    }

    public void setType(String type) {
        typeDropdown.setSelected(type);
        setTypeDependentVisibility(isToolType(type), isArmorType(type));
        rebuildRows();
    }

    public String getType() {
        return typeDropdown.getSelected() == null ? "basic" : typeDropdown.getSelected();
    }

    public void setFields(List<LabItemField> fields) {
        this.fields = fields;
        typeDropdown.setVisible(fields.contains(LabItemField.TYPE));
        nameField.setVisible(fields.contains(LabItemField.DISPLAY_NAME));
        textureField.setVisible(fields.contains(LabItemField.TEXTURE));
        texturePickButton.setVisible(fields.contains(LabItemField.TEXTURE));
        rarityDropdown.setVisible(fields.contains(LabItemField.RARITY));
        maxStackField.setVisible(fields.contains(LabItemField.MAX_STACK));
        maxDamageField.setVisible(fields.contains(LabItemField.MAX_DAMAGE));
        burnTimeField.setVisible(fields.contains(LabItemField.BURN_TIME));
        glowToggle.setVisible(fields.contains(LabItemField.GLOW));
        fireResistantToggle.setVisible(fields.contains(LabItemField.FIRE_RESISTANT));
        containerItemField.setVisible(fields.contains(LabItemField.CONTAINER_ITEM));
        tooltipField.setVisible(fields.contains(LabItemField.TOOLTIP));
        tagsField.setVisible(fields.contains(LabItemField.TAGS));
        foodHungerField.setVisible(fields.contains(LabItemField.FOOD_HUNGER));
        foodSaturationField.setVisible(fields.contains(LabItemField.FOOD_SATURATION));
        foodMeatToggle.setVisible(fields.contains(LabItemField.FOOD_MEAT));
        foodFastToEatToggle.setVisible(fields.contains(LabItemField.FOOD_FAST_TO_EAT));
        foodAlwaysEdibleToggle.setVisible(fields.contains(LabItemField.FOOD_ALWAYS_EDIBLE));
        foodEffectField.setVisible(fields.contains(LabItemField.FOOD_EFFECT));
        foodEffectDurationField.setVisible(fields.contains(LabItemField.FOOD_EFFECT_DURATION));
        foodEffectAmplifierField.setVisible(fields.contains(LabItemField.FOOD_EFFECT_AMPLIFIER));
        foodEffectChanceField.setVisible(fields.contains(LabItemField.FOOD_EFFECT_CHANCE));
        toolTierDropdown.setVisible(fields.contains(LabItemField.TOOL_TIER));
        attackDamageField.setVisible(fields.contains(LabItemField.ATTACK_DAMAGE_BASELINE));
        attackSpeedField.setVisible(fields.contains(LabItemField.SPEED_BASELINE));
        digSpeedField.setVisible(fields.contains(LabItemField.DIG_SPEED));
        armorTierField.setVisible(fields.contains(LabItemField.ARMOR_TIER));
        armorProtectionField.setVisible(fields.contains(LabItemField.ARMOR_PROTECTION));
        armorToughnessField.setVisible(fields.contains(LabItemField.ARMOR_TOUGHNESS));
        armorKnockbackField.setVisible(fields.contains(LabItemField.ARMOR_KNOCKBACK));
        tierUsesField.setVisible(fields.contains(LabItemField.TIER_USES));
        tierSpeedField.setVisible(fields.contains(LabItemField.TIER_SPEED));
        tierAttackDamageBonusField.setVisible(fields.contains(LabItemField.TIER_ATTACK_DAMAGE_BONUS));
        tierLevelField.setVisible(fields.contains(LabItemField.TIER_LEVEL));
        tierEnchantValueField.setVisible(fields.contains(LabItemField.TIER_ENCHANT_VALUE));
        tierRepairIngredientField.setVisible(fields.contains(LabItemField.TIER_REPAIR_INGREDIENT));
        tierDurabilityMultiplierField.setVisible(fields.contains(LabItemField.TIER_DURABILITY_MULTIPLIER));
        tierProtectionsField.setVisible(fields.contains(LabItemField.TIER_PROTECTIONS));
        tierEquipSoundField.setVisible(fields.contains(LabItemField.TIER_EQUIP_SOUND));
        tierToughnessField.setVisible(fields.contains(LabItemField.TIER_TOUGHNESS));
        tierKnockbackResistanceField.setVisible(fields.contains(LabItemField.TIER_KNOCKBACK_RESISTANCE));
        attributeIdField.setVisible(fields.contains(LabItemField.ATTRIBUTE_ID));
        attributeNameField.setVisible(fields.contains(LabItemField.ATTRIBUTE_NAME));
        attributeAmountField.setVisible(fields.contains(LabItemField.ATTRIBUTE_AMOUNT));
        attributeOperationDropdown.setVisible(fields.contains(LabItemField.ATTRIBUTE_OPERATION));
        behaviorDropdown.setVisible(fields.contains(LabItemField.BEHAVIOR));
        behaviorItemField.setVisible(fields.contains(LabItemField.BEHAVIOR_ITEM));
        behaviorDamageField.setVisible(fields.contains(LabItemField.BEHAVIOR_DAMAGE));
        hideCreativeToggle.setVisible(fields.contains(LabItemField.DISABLE_CREATIVE_HIDE));
        removeRecipesToggle.setVisible(fields.contains(LabItemField.DISABLE_RECIPE_REMOVAL));
        hideViewerToggle.setVisible(fields.contains(LabItemField.DISABLE_VIEWER_HIDE));
        boolean tool = isToolType(getType());
        boolean armor = isArmorType(getType());
        setTypeDependentVisibility(tool, armor);
        resetScroll();
        rebuildRows();
    }

    private void setTypeDependentVisibility(boolean tool, boolean armor) {
        // tool-only
        toolTierDropdown.setVisible(tool && fields.contains(LabItemField.TOOL_TIER));
        attackDamageField.setVisible(tool && fields.contains(LabItemField.ATTACK_DAMAGE_BASELINE));
        attackSpeedField.setVisible(tool && fields.contains(LabItemField.SPEED_BASELINE));
        digSpeedField.setVisible(tool && fields.contains(LabItemField.DIG_SPEED));
        tierUsesField.setVisible(tool && fields.contains(LabItemField.TIER_USES));
        tierSpeedField.setVisible(tool && fields.contains(LabItemField.TIER_SPEED));
        tierAttackDamageBonusField.setVisible(tool && fields.contains(LabItemField.TIER_ATTACK_DAMAGE_BONUS));
        tierLevelField.setVisible(tool && fields.contains(LabItemField.TIER_LEVEL));
        tierEnchantValueField.setVisible(tool && fields.contains(LabItemField.TIER_ENCHANT_VALUE));
        tierRepairIngredientField.setVisible(tool && fields.contains(LabItemField.TIER_REPAIR_INGREDIENT));
        // armor-only
        armorTierField.setVisible(armor && fields.contains(LabItemField.ARMOR_TIER));
        armorProtectionField.setVisible(armor && fields.contains(LabItemField.ARMOR_PROTECTION));
        armorToughnessField.setVisible(armor && fields.contains(LabItemField.ARMOR_TOUGHNESS));
        armorKnockbackField.setVisible(armor && fields.contains(LabItemField.ARMOR_KNOCKBACK));
        tierDurabilityMultiplierField.setVisible(armor && fields.contains(LabItemField.TIER_DURABILITY_MULTIPLIER));
        tierProtectionsField.setVisible(armor && fields.contains(LabItemField.TIER_PROTECTIONS));
        tierEquipSoundField.setVisible(armor && fields.contains(LabItemField.TIER_EQUIP_SOUND));
        tierToughnessField.setVisible(armor && fields.contains(LabItemField.TIER_TOUGHNESS));
        tierKnockbackResistanceField.setVisible(armor && fields.contains(LabItemField.TIER_KNOCKBACK_RESISTANCE));
        boolean showBehavior = behaviorDropdown.isVisible() && !behavior.equals("none");
        behaviorItemField.setVisible(showBehavior && fields.contains(LabItemField.BEHAVIOR_ITEM));
        behaviorDamageField.setVisible(showBehavior && fields.contains(LabItemField.BEHAVIOR_DAMAGE));
    }

    private void rebuildRowsByType() {
        String current = getType();
        setTypeDependentVisibility(isToolType(current), isArmorType(current));
        rebuildRows();
    }

    private static boolean isToolType(String type) {
        return List.of("sword", "pickaxe", "axe", "shovel", "hoe", "shears").contains(type);
    }

    private static boolean isArmorType(String type) {
        return List.of("helmet", "chestplate", "leggings", "boots").contains(type);
    }

    private void rebuildRows() {
        List<FieldRow> rows = new ArrayList<>();
        for (LabItemField field : fields) {
            if (isVisible(field)) rows.add(fieldRow(field));
        }
        setRows(rows);
    }

    private boolean isVisible(LabItemField field) {
        return switch (field) {
            case TOOL_TIER, ATTACK_DAMAGE_BASELINE, SPEED_BASELINE, DIG_SPEED, TIER_USES, TIER_SPEED,
                    TIER_ATTACK_DAMAGE_BONUS, TIER_LEVEL, TIER_ENCHANT_VALUE, TIER_REPAIR_INGREDIENT ->
                    isToolType(getType());
            case ARMOR_TIER, ARMOR_PROTECTION, ARMOR_TOUGHNESS, ARMOR_KNOCKBACK, TIER_DURABILITY_MULTIPLIER,
                    TIER_PROTECTIONS, TIER_EQUIP_SOUND, TIER_TOUGHNESS, TIER_KNOCKBACK_RESISTANCE ->
                    isArmorType(getType());
            case BEHAVIOR_ITEM, BEHAVIOR_DAMAGE -> !behavior.equals("none");
            default -> true;
        };
    }

    private FieldRow fieldRow(LabItemField field) {
        return switch (field) {
            case TYPE -> new FieldRow(typeLabel, typeDropdown, null);
            case DISPLAY_NAME -> new FieldRow(nameLabel, nameField, null);
            case TEXTURE -> new FieldRow(textureLabel, textureField, null);
            case RARITY -> new FieldRow(rarityLabel, rarityDropdown, null);
            case MAX_STACK -> new FieldRow(maxStackLabel, maxStackField, null);
            case MAX_DAMAGE -> new FieldRow(maxDamageLabel, maxDamageField, null);
            case BURN_TIME -> new FieldRow(burnTimeLabel, burnTimeField, null);
            case GLOW -> new FieldRow(glowLabel, glowToggle, null);
            case FIRE_RESISTANT -> new FieldRow(fireResistantLabel, fireResistantToggle, null);
            case CONTAINER_ITEM -> new FieldRow(containerItemLabel, containerItemField, null);
            case TOOLTIP -> new FieldRow(tooltipLabel, tooltipField, null);
            case TAGS -> new FieldRow(tagsLabel, tagsField, null);
            case FOOD_HUNGER -> new FieldRow(foodHungerLabel, foodHungerField, null);
            case FOOD_SATURATION -> new FieldRow(foodSaturationLabel, foodSaturationField, null);
            case FOOD_MEAT -> new FieldRow(foodMeatLabel, foodMeatToggle, null);
            case FOOD_FAST_TO_EAT -> new FieldRow(foodFastToEatLabel, foodFastToEatToggle, null);
            case FOOD_ALWAYS_EDIBLE -> new FieldRow(foodAlwaysEdibleLabel, foodAlwaysEdibleToggle, null);
            case FOOD_EFFECT -> new FieldRow(foodEffectLabel, foodEffectField, null);
            case FOOD_EFFECT_DURATION -> new FieldRow(foodEffectDurationLabel, foodEffectDurationField, null);
            case FOOD_EFFECT_AMPLIFIER -> new FieldRow(foodEffectAmplifierLabel, foodEffectAmplifierField, null);
            case FOOD_EFFECT_CHANCE -> new FieldRow(foodEffectChanceLabel, foodEffectChanceField, null);
            case TOOL_TIER -> new FieldRow(toolTierLabel, toolTierDropdown, null);
            case ATTACK_DAMAGE_BASELINE -> new FieldRow(attackDamageLabel, attackDamageField, null);
            case SPEED_BASELINE -> new FieldRow(attackSpeedLabel, attackSpeedField, null);
            case DIG_SPEED -> new FieldRow(digSpeedLabel, digSpeedField, null);
            case ARMOR_TIER -> new FieldRow(armorTierLabel, armorTierField, null);
            case ARMOR_PROTECTION -> new FieldRow(armorProtectionLabel, armorProtectionField, null);
            case ARMOR_TOUGHNESS -> new FieldRow(armorToughnessLabel, armorToughnessField, null);
            case ARMOR_KNOCKBACK -> new FieldRow(armorKnockbackLabel, armorKnockbackField, null);
            case TIER_USES -> new FieldRow(tierUsesLabel, tierUsesField, null);
            case TIER_SPEED -> new FieldRow(tierSpeedLabel, tierSpeedField, null);
            case TIER_ATTACK_DAMAGE_BONUS -> new FieldRow(tierAttackDamageBonusLabel, tierAttackDamageBonusField, null);
            case TIER_LEVEL -> new FieldRow(tierLevelLabel, tierLevelField, null);
            case TIER_ENCHANT_VALUE -> new FieldRow(tierEnchantValueLabel, tierEnchantValueField, null);
            case TIER_REPAIR_INGREDIENT -> new FieldRow(tierRepairIngredientLabel, tierRepairIngredientField, null);
            case TIER_DURABILITY_MULTIPLIER -> new FieldRow(tierDurabilityMultiplierLabel, tierDurabilityMultiplierField, null);
            case TIER_PROTECTIONS -> new FieldRow(tierProtectionsLabel, tierProtectionsField, null);
            case TIER_EQUIP_SOUND -> new FieldRow(tierEquipSoundLabel, tierEquipSoundField, null);
            case TIER_TOUGHNESS -> new FieldRow(tierToughnessLabel, tierToughnessField, null);
            case TIER_KNOCKBACK_RESISTANCE -> new FieldRow(tierKnockbackResistanceLabel, tierKnockbackResistanceField, null);
            case ATTRIBUTE_ID -> new FieldRow(attributeIdLabel, attributeIdField, null);
            case ATTRIBUTE_NAME -> new FieldRow(attributeNameLabel, attributeNameField, null);
            case ATTRIBUTE_AMOUNT -> new FieldRow(attributeAmountLabel, attributeAmountField, null);
            case ATTRIBUTE_OPERATION -> new FieldRow(attributeOperationLabel, attributeOperationDropdown, null);
            case BEHAVIOR -> new FieldRow(behaviorLabel, behaviorDropdown, null);
            case BEHAVIOR_ITEM -> new FieldRow(behaviorItemLabel, behaviorItemField, null);
            case BEHAVIOR_DAMAGE -> new FieldRow(behaviorDamageLabel, behaviorDamageField, null);
            case DISABLE_CREATIVE_HIDE -> new FieldRow(hideCreativeLabel, hideCreativeToggle, null);
            case DISABLE_RECIPE_REMOVAL -> new FieldRow(removeRecipesLabel, removeRecipesToggle, null);
            case DISABLE_VIEWER_HIDE -> new FieldRow(hideViewerLabel, hideViewerToggle, null);
            default -> throw new IllegalStateException("Unexpected item field: " + field);
        };
    }

    public LabItemFieldValues getValues() {
        return new LabItemFieldValues(
                name,
                texture,
                rarityDropdown.getSelected() == null ? "" : rarityDropdown.getSelected(),
                parseInt(maxStackText, 64),
                parseInt(maxDamageText, 0),
                parseInt(burnTimeText, 0),
                glow,
                fireResistant,
                containerItem,
                tooltip,
                tags,
                parseInt(foodHungerText, 0),
                parseFloat(foodSaturationText, 0f),
                foodMeat,
                foodFastToEat,
                foodAlwaysEdible,
                foodEffect,
                parseInt(foodEffectDurationText, 0),
                parseInt(foodEffectAmplifierText, 0),
                clampChance(parseFloat(foodEffectChanceText, 0f) / 100f),
                toolTierDropdown.getSelected() == null ? "" : toolTierDropdown.getSelected(),
                parseFloat(attackDamageText, 0f),
                parseFloat(attackSpeedText, 0f),
                parseFloat(digSpeedText, 0f),
                armorTierField.getSelected() == null ? "" : armorTierField.getSelected(),
                parseInt(armorProtectionText, 0),
                parseFloat(armorToughnessText, 0f),
                parseFloat(armorKnockbackText, 0f),
                parseInt(tierUsesText, 0),
                parseFloat(tierSpeedText, 0f),
                parseFloat(tierAttackDamageBonusText, 0f),
                parseInt(tierLevelText, 0),
                parseInt(tierEnchantValueText, 0),
                tierRepairIngredient,
                parseFloat(tierDurabilityMultiplierText, 0f),
                tierProtections,
                tierEquipSound,
                parseFloat(tierToughnessText, 0f),
                parseFloat(tierKnockbackResistanceText, 0f),
                attributeId,
                attributeName,
                parseFloat(attributeAmountText, 0f),
                attributeOperationDropdown.getSelected() == null ? "" : attributeOperationDropdown.getSelected(),
                behaviorItem,
                parseInt(behaviorDamageText, 0));
    }

    public void applyValues(LabItemFieldValues values) {
        if (values == null) return;
        name = values.displayName();
        texture = values.texture();
        rarityDropdown.setSelected(values.rarity() == null ? "" : values.rarity());
        maxStackText = Integer.toString(values.maxStack());
        maxDamageText = Integer.toString(values.maxDamage());
        burnTimeText = Integer.toString(values.burnTime());
        glow = values.glow();
        fireResistant = values.fireResistant();
        containerItem = values.containerItem();
        tooltip = values.tooltip();
        tags = values.tags();
        foodHungerText = Integer.toString(values.foodHunger());
        foodSaturationText = formatFloat(values.foodSaturation());
        foodMeat = values.foodMeat();
        foodFastToEat = values.foodFastToEat();
        foodAlwaysEdible = values.foodAlwaysEdible();
        foodEffect = values.foodEffect();
        foodEffectDurationText = Integer.toString(values.foodEffectDuration());
        foodEffectAmplifierText = Integer.toString(values.foodEffectAmplifier());
        foodEffectChanceText = formatFloat(values.foodEffectChance() * 100f);
        toolTier = values.toolTier();
        attackDamageText = formatFloat(values.attackDamageBaseline());
        attackSpeedText = formatFloat(values.speedBaseline());
        digSpeedText = formatFloat(values.digSpeed());
        armorTier = values.armorTier();
        armorProtectionText = Integer.toString(values.armorProtection());
        armorToughnessText = formatFloat(values.armorToughness());
        armorKnockbackText = formatFloat(values.armorKnockback());
        tierUsesText = Integer.toString(values.tierUses());
        tierSpeedText = formatFloat(values.tierSpeed());
        tierAttackDamageBonusText = formatFloat(values.tierAttackDamageBonus());
        tierLevelText = Integer.toString(values.tierLevel());
        tierEnchantValueText = Integer.toString(values.tierEnchantValue());
        tierRepairIngredient = values.tierRepairIngredient();
        tierDurabilityMultiplierText = formatFloat(values.tierDurabilityMultiplier());
        tierProtections = values.tierProtections();
        tierEquipSound = values.tierEquipSound();
        tierToughnessText = formatFloat(values.tierToughness());
        tierKnockbackResistanceText = formatFloat(values.tierKnockbackResistance());
        attributeId = values.attributeId();
        attributeName = values.attributeName();
        attributeAmountText = formatFloat(values.attributeAmount());
        attributeOperationDropdown.setSelected(values.attributeOperation() == null ? "" : values.attributeOperation());
        behaviorItem = values.behaviorItem();
        behaviorDamageText = Integer.toString(values.behaviorDamage());

        nameField.setCurrentString(name);
        textureField.setCurrentString(texture);
        containerItemField.setCurrentString(containerItem);
        tooltipField.setCurrentString(tooltip);
        tagsField.setCurrentString(tags);
        foodEffectField.setCurrentString(foodEffect);
        tierRepairIngredientField.setCurrentString(tierRepairIngredient);
        tierProtectionsField.setCurrentString(tierProtections);
        tierEquipSoundField.setCurrentString(tierEquipSound);
        attributeIdField.setCurrentString(attributeId);
        attributeNameField.setCurrentString(attributeName);
        behaviorItemField.setCurrentString(behaviorItem);
        refreshToolTierOptions();
        refreshArmorTierOptions();
        rebuildRows();
    }

    public List<String> getTags() {
        List<String> result = new ArrayList<>();
        for (String tag : tags.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isBlank()) result.add(trimmed);
        }
        return result;
    }

    public List<LabItemAction> getActions() {
        List<LabItemAction> result = new ArrayList<>();
        switch (behavior) {
            case "cancel" -> result.add(LabItemAction.CANCEL_USE);
            case "give" -> {
                if (!behaviorItem.isBlank()) result.add(LabItemAction.GIVE_ITEM);
            }
            case "damage" -> {
                if (parseInt(behaviorDamageText, 0) > 0) result.add(LabItemAction.DAMAGE_ITEM);
            }
            default -> {}
        }
        if (hideCreative) result.add(LabItemAction.HIDE_CREATIVE_TAB);
        if (removeRecipes) result.add(LabItemAction.REMOVE_RECIPES);
        if (hideViewer) result.add(LabItemAction.HIDE_VIEWER);
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

    public static List<String> types() {
        return TYPES;
    }

    public void setTextureValue(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        texture = relativePath;
        textureField.setCurrentString(relativePath);
    }

    public void applyActions(List<LabItemAction> actions) {
        behavior = "none";
        hideCreative = false;
        removeRecipes = false;
        hideViewer = false;
        for (LabItemAction action : actions) {
            switch (action) {
                case CANCEL_USE -> behavior = "cancel";
                case GIVE_ITEM -> behavior = "give";
                case DAMAGE_ITEM -> behavior = "damage";
                case HIDE_CREATIVE_TAB -> hideCreative = true;
                case REMOVE_RECIPES -> removeRecipes = true;
                case HIDE_VIEWER -> hideViewer = true;
            }
        }
        behaviorDropdown.setSelected(behavior);
        rebuildRows();
    }

    public List<LabItemField> fullFields() {
        return List.of(LabItemField.values());
    }

    public List<LabItemField> builtInFields() {
        return List.of(
                LabItemField.MAX_STACK, LabItemField.MAX_DAMAGE, LabItemField.BURN_TIME, LabItemField.RARITY,
                LabItemField.FIRE_RESISTANT, LabItemField.CONTAINER_ITEM,
                LabItemField.FOOD_HUNGER, LabItemField.FOOD_SATURATION, LabItemField.FOOD_MEAT,
                LabItemField.FOOD_FAST_TO_EAT, LabItemField.FOOD_ALWAYS_EDIBLE,
                LabItemField.FOOD_EFFECT, LabItemField.FOOD_EFFECT_DURATION, LabItemField.FOOD_EFFECT_AMPLIFIER,
                LabItemField.FOOD_EFFECT_CHANCE,
                LabItemField.ATTACK_DAMAGE_BASELINE, LabItemField.SPEED_BASELINE, LabItemField.DIG_SPEED,
                LabItemField.ARMOR_PROTECTION, LabItemField.ARMOR_TOUGHNESS, LabItemField.ARMOR_KNOCKBACK,
                LabItemField.TIER_USES, LabItemField.TIER_SPEED, LabItemField.TIER_ATTACK_DAMAGE_BONUS,
                LabItemField.TIER_LEVEL, LabItemField.TIER_ENCHANT_VALUE, LabItemField.TIER_REPAIR_INGREDIENT,
                LabItemField.BEHAVIOR, LabItemField.BEHAVIOR_ITEM, LabItemField.BEHAVIOR_DAMAGE,
                LabItemField.DISABLE_CREATIVE_HIDE, LabItemField.DISABLE_RECIPE_REMOVAL, LabItemField.DISABLE_VIEWER_HIDE);
    }
}