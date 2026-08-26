package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabRowCardSettingsWidget;
import com.abo47.kubejslab.client.ui.base.LabSearchDropdownWidget;
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
    private final LabSearchDropdownWidget foodEffectDropdown;
    private final TextFieldWidget foodHungerField;
    private final TextFieldWidget foodSaturationField;
    private final LabToggleSwitchWidget foodMeatToggle;
    private final LabToggleSwitchWidget foodFastToEatToggle;
    private final LabToggleSwitchWidget foodAlwaysEdibleToggle;
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
        typeDropdown.setOnSelect(value -> rebuildRows());
        addWidget(typeDropdown);
        addPopupDropdown(typeDropdown);

        nameField = commitField(this::commitName);
        addWidget(nameField);

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

        foodEffectDropdown = new LabSearchDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        foodEffectDropdown.setOptions(BuiltInRegistries.MOB_EFFECT.keySet().stream()
                .map(ResourceLocation::toString).sorted(String.CASE_INSENSITIVE_ORDER).toList());
        addWidget(foodEffectDropdown);
        addPopupDropdown(foodEffectDropdown);

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

    private void commitContainerItem(String value) {
        if (value != null) containerItem = value;
    }

    private void commitTooltip(String value) {
        if (value != null) tooltip = value;
    }

    private void commitTags(String value) {
        if (value != null) tags = value;
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
        rebuildRows();
    }

    public String getType() {
        return typeDropdown.getSelected() == null ? "basic" : typeDropdown.getSelected();
    }

    public void setFields(List<LabItemField> fields) {
        this.fields = fields;
        rebuildRows();
    }

    private boolean isDisabled(LabItemField field) {
        return switch (field) {
            case TOOL_TIER, ATTACK_DAMAGE_BASELINE, SPEED_BASELINE, DIG_SPEED, TIER_USES, TIER_SPEED,
                    TIER_ATTACK_DAMAGE_BONUS, TIER_LEVEL, TIER_ENCHANT_VALUE, TIER_REPAIR_INGREDIENT ->
                    !isToolType(getType());
            case ARMOR_TIER, ARMOR_PROTECTION, ARMOR_TOUGHNESS, ARMOR_KNOCKBACK, TIER_DURABILITY_MULTIPLIER,
                    TIER_PROTECTIONS, TIER_EQUIP_SOUND, TIER_TOUGHNESS, TIER_KNOCKBACK_RESISTANCE ->
                    !isArmorType(getType());
            case BEHAVIOR_ITEM, BEHAVIOR_DAMAGE -> behavior.equals("none");
            default -> false;
        };
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
            FieldRow row = new FieldRow(rowLabelFor(field), fieldControl(field), null, isDisabled(field));
            if (row.control() != null) {
                row.control().setHoverTooltips(List.of(Component.translatable(LabItemTooltips.key(field))));
            }
            rows.add(row);
        }
        resetScroll();
        setRows(rows);
    }

    private TextTexture rowLabelFor(LabItemField field) {
        return switch (field) {
            case TYPE -> typeLabel;
            case DISPLAY_NAME -> nameLabel;
            case TEXTURE -> textureLabel;
            case RARITY -> rarityLabel;
            case MAX_STACK -> maxStackLabel;
            case MAX_DAMAGE -> maxDamageLabel;
            case BURN_TIME -> burnTimeLabel;
            case GLOW -> glowLabel;
            case FIRE_RESISTANT -> fireResistantLabel;
            case CONTAINER_ITEM -> containerItemLabel;
            case TOOLTIP -> tooltipLabel;
            case TAGS -> tagsLabel;
            case FOOD_HUNGER -> foodHungerLabel;
            case FOOD_SATURATION -> foodSaturationLabel;
            case FOOD_MEAT -> foodMeatLabel;
            case FOOD_FAST_TO_EAT -> foodFastToEatLabel;
            case FOOD_ALWAYS_EDIBLE -> foodAlwaysEdibleLabel;
            case FOOD_EFFECT -> foodEffectLabel;
            case FOOD_EFFECT_DURATION -> foodEffectDurationLabel;
            case FOOD_EFFECT_AMPLIFIER -> foodEffectAmplifierLabel;
            case FOOD_EFFECT_CHANCE -> foodEffectChanceLabel;
            case TOOL_TIER -> toolTierLabel;
            case ATTACK_DAMAGE_BASELINE -> attackDamageLabel;
            case SPEED_BASELINE -> attackSpeedLabel;
            case DIG_SPEED -> digSpeedLabel;
            case ARMOR_TIER -> armorTierLabel;
            case ARMOR_PROTECTION -> armorProtectionLabel;
            case ARMOR_TOUGHNESS -> armorToughnessLabel;
            case ARMOR_KNOCKBACK -> armorKnockbackLabel;
            case TIER_USES -> tierUsesLabel;
            case TIER_SPEED -> tierSpeedLabel;
            case TIER_ATTACK_DAMAGE_BONUS -> tierAttackDamageBonusLabel;
            case TIER_LEVEL -> tierLevelLabel;
            case TIER_ENCHANT_VALUE -> tierEnchantValueLabel;
            case TIER_REPAIR_INGREDIENT -> tierRepairIngredientLabel;
            case TIER_DURABILITY_MULTIPLIER -> tierDurabilityMultiplierLabel;
            case TIER_PROTECTIONS -> tierProtectionsLabel;
            case TIER_EQUIP_SOUND -> tierEquipSoundLabel;
            case TIER_TOUGHNESS -> tierToughnessLabel;
            case TIER_KNOCKBACK_RESISTANCE -> tierKnockbackResistanceLabel;
            case ATTRIBUTE_ID -> attributeIdLabel;
            case ATTRIBUTE_NAME -> attributeNameLabel;
            case ATTRIBUTE_AMOUNT -> attributeAmountLabel;
            case ATTRIBUTE_OPERATION -> attributeOperationLabel;
            case BEHAVIOR -> behaviorLabel;
            case BEHAVIOR_ITEM -> behaviorItemLabel;
            case BEHAVIOR_DAMAGE -> behaviorDamageLabel;
            case DISABLE_CREATIVE_HIDE -> hideCreativeLabel;
            case DISABLE_RECIPE_REMOVAL -> removeRecipesLabel;
            case DISABLE_VIEWER_HIDE -> hideViewerLabel;
        };
    }

    private com.lowdragmc.lowdraglib.gui.widget.Widget fieldControl(LabItemField field) {
        return switch (field) {
            case TYPE -> typeDropdown;
            case DISPLAY_NAME -> nameField;
            case TEXTURE -> texturePickButton;
            case RARITY -> rarityDropdown;
            case MAX_STACK -> maxStackField;
            case MAX_DAMAGE -> maxDamageField;
            case BURN_TIME -> burnTimeField;
            case GLOW -> glowToggle;
            case FIRE_RESISTANT -> fireResistantToggle;
            case CONTAINER_ITEM -> containerItemField;
            case TOOLTIP -> tooltipField;
            case TAGS -> tagsField;
            case FOOD_HUNGER -> foodHungerField;
            case FOOD_SATURATION -> foodSaturationField;
            case FOOD_MEAT -> foodMeatToggle;
            case FOOD_FAST_TO_EAT -> foodFastToEatToggle;
            case FOOD_ALWAYS_EDIBLE -> foodAlwaysEdibleToggle;
            case FOOD_EFFECT -> foodEffectDropdown;
            case FOOD_EFFECT_DURATION -> foodEffectDurationField;
            case FOOD_EFFECT_AMPLIFIER -> foodEffectAmplifierField;
            case FOOD_EFFECT_CHANCE -> foodEffectChanceField;
            case TOOL_TIER -> toolTierDropdown;
            case ATTACK_DAMAGE_BASELINE -> attackDamageField;
            case SPEED_BASELINE -> attackSpeedField;
            case DIG_SPEED -> digSpeedField;
            case ARMOR_TIER -> armorTierField;
            case ARMOR_PROTECTION -> armorProtectionField;
            case ARMOR_TOUGHNESS -> armorToughnessField;
            case ARMOR_KNOCKBACK -> armorKnockbackField;
            case TIER_USES -> tierUsesField;
            case TIER_SPEED -> tierSpeedField;
            case TIER_ATTACK_DAMAGE_BONUS -> tierAttackDamageBonusField;
            case TIER_LEVEL -> tierLevelField;
            case TIER_ENCHANT_VALUE -> tierEnchantValueField;
            case TIER_REPAIR_INGREDIENT -> tierRepairIngredientField;
            case TIER_DURABILITY_MULTIPLIER -> tierDurabilityMultiplierField;
            case TIER_PROTECTIONS -> tierProtectionsField;
            case TIER_EQUIP_SOUND -> tierEquipSoundField;
            case TIER_TOUGHNESS -> tierToughnessField;
            case TIER_KNOCKBACK_RESISTANCE -> tierKnockbackResistanceField;
            case ATTRIBUTE_ID -> attributeIdField;
            case ATTRIBUTE_NAME -> attributeNameField;
            case ATTRIBUTE_AMOUNT -> attributeAmountField;
            case ATTRIBUTE_OPERATION -> attributeOperationDropdown;
            case BEHAVIOR -> behaviorDropdown;
            case BEHAVIOR_ITEM -> behaviorItemField;
            case BEHAVIOR_DAMAGE -> behaviorDamageField;
            case DISABLE_CREATIVE_HIDE -> hideCreativeToggle;
            case DISABLE_RECIPE_REMOVAL -> removeRecipesToggle;
            case DISABLE_VIEWER_HIDE -> hideViewerToggle;
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
                foodEffectDropdown.getSelected(),
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
        foodEffectDropdown.setSelected(values.foodEffect());
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
        containerItemField.setCurrentString(containerItem);
        tooltipField.setCurrentString(tooltip);
        tagsField.setCurrentString(tags);
        foodEffectDurationField.setCurrentString(foodEffectDurationText);
        tierRepairIngredientField.setCurrentString(tierRepairIngredient);
        tierProtectionsField.setCurrentString(tierProtections);
        tierEquipSoundField.setCurrentString(tierEquipSound);
        attributeIdField.setCurrentString(attributeId);
        attributeNameField.setCurrentString(attributeName);
        behaviorItemField.setCurrentString(behaviorItem);
        texturePickButton.setLabel(fileName(texture));
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
        texturePickButton.setLabel(fileName(relativePath));
    }

    public String getTexture() {
        return texture;
    }

    private static String fileName(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return Component.translatable(LabGuiKeys.LAB_ITEM_TEXTURE_PICK).getString();
        }
        int slash = relativePath.lastIndexOf('/');
        return slash < 0 ? relativePath : relativePath.substring(slash + 1);
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
}
