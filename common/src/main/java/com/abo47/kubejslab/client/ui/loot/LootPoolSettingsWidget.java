package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.picker.Pick;
import com.abo47.kubejslab.client.ui.picker.PickerEntries;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiGlow;
import com.abo47.kubejslab.client.ui.widgets.CommitField;
import com.abo47.kubejslab.client.ui.widgets.FieldRow;
import com.abo47.kubejslab.client.ui.widgets.OptionDropdownWidget;
import com.abo47.kubejslab.client.ui.widgets.PickTarget;
import com.abo47.kubejslab.client.ui.widgets.RowCardSettings;
import com.abo47.kubejslab.client.ui.widgets.ToggleSwitchWidget;
import com.abo47.kubejslab.loot.model.LootEntryValues;
import com.abo47.kubejslab.loot.model.LootField;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.runtime.LootService;
import com.abo47.kubejslab.workspace.ScriptEscaping;


public final class LootPoolSettingsWidget extends RowCardSettings {
    public static final int MAX_ENTRIES = 64;

    private static final List<String> ROLLS_TYPES = List.of("constant", "uniform", "binomial");
    private static final List<String> ENTRY_TYPES = List.of("item", "tag", "empty", "loot_table", "dynamic");
    private static final List<String> COUNT_TYPES = List.of("constant", "uniform");
    private static final List<String> TOOL_OPTIONS = List.of("none", "silk_touch", "fortune");
    private static final String GROUP_NONE = "none";
    private static final String GROUP_NEW = "new group";

    private final PoolState pool = new PoolState();
    private String lootType = LootService.LOOT_TYPE_BLOCK;
    private final List<Widget> dynamicWidgets = new ArrayList<>();
    private Pick pendingPick;
    private int selectedEntry;
    private Runnable entryListListener;

    private final class PickSlot extends Widget implements PickTarget {
        private final EntryState entry;

        PickSlot(EntryState entry) {
            super(0, 0, 15, 15);
            this.entry = entry;
            setClientSideWidget();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
                paintCarried(entry);
                paintPending(entry);
                return true;
            }
            return false;
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
            int x = getPositionX();
            int y = getPositionY();
            SlotWidget.ITEM_SLOT_TEXTURE.draw(g, mx, my, x, y, 15, 15);
            ItemStack icon = slotIcon(entry);
            if (!icon.isEmpty()) {
                new ItemStackTexture(icon).draw(g, mx, my, x + 1, y + 1, 13, 13);
            }
            if (pendingPick != null) {
                UiColors.bordered(0, UiColors.INTERACTIVE).draw(g, mx, my, x, y, 15, 15);
            } else if (isMouseOverElement(mx, my)) {
                UiGlow.drawGlow(g, mx, my, x, y, 15, 15);
            }
            setHoverTooltips(slotTips(entry));
        }
    }

    public boolean offerPick(Pick pick) {
        if (pick instanceof Pick.Item || pick instanceof Pick.Tag) {
            pendingPick = pick;
            return true;
        }
        return false;
    }

    public void clearPendingPick() {
        pendingPick = null;
    }

    private void paintCarried(EntryState entry) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.containerMenu == null) {
            return;
        }
        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) {
            return;
        }
        entry.item = BuiltInRegistries.ITEM.getKey(carried.getItem()).toString();
        entry.typeDropdown.setSelected("item");
        pendingPick = null;
        player.containerMenu.setCarried(ItemStack.EMPTY);
        rebuildRows();
    }

    private void paintPending(EntryState entry) {
        if (pendingPick == null || !pool.entries.contains(entry)) {
            return;
        }
        if (pendingPick instanceof Pick.Item item) {
            entry.item = BuiltInRegistries.ITEM.getKey(item.stack().getItem()).toString();
            entry.typeDropdown.setSelected("item");
        } else if (pendingPick instanceof Pick.Tag tag) {
            entry.tag = tag.tag().toString();
            entry.typeDropdown.setSelected("tag");
        } else {
            return;
        }
        pendingPick = null;
        rebuildRows();
    }

    private static ItemStack slotIcon(EntryState entry) {
        String entryType = entry.typeDropdown == null || entry.typeDropdown.getSelected() == null ? "item"
                : entry.typeDropdown.getSelected();
        if ("tag".equals(entryType)) {
            if (entry.tag.isBlank()) {
                return ItemStack.EMPTY;
            }
            ResourceLocation tagId = ResourceLocation.tryParse(entry.tag);
            return tagId == null ? ItemStack.EMPTY : PickerEntries.tagPreview(tagId);
        }
        if (entry.item.isBlank()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(entry.item);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    private List<Component> slotTips(EntryState entry) {
        String entryType = entry.typeDropdown == null || entry.typeDropdown.getSelected() == null ? "item"
                : entry.typeDropdown.getSelected();
        if ("tag".equals(entryType) && !entry.tag.isBlank()) {
            List<ItemStack> previews = PickerEntries.tagPreviews(ResourceLocation.tryParse(entry.tag));
            if (!previews.isEmpty()) {
                return List.of(Component.literal("#" + entry.tag),
                        Component.literal(previews.get(0).getHoverName().getString()));
            }
            return List.of(Component.literal("#" + entry.tag));
        }
        if (!"tag".equals(entryType)) {
            ItemStack icon = slotIcon(entry);
            if (!icon.isEmpty()) {
                return List.of(icon.getHoverName());
            }
        }
        return List.of(Component.translatable(LootKeys.LOOT_PICK_HINT));
    }

    private static final class EntryState {
        OptionDropdownWidget typeDropdown;
        OptionDropdownWidget countTypeDropdown;
        PickSlot pickSlot;
        TextFieldWidget tableField;
        TextFieldWidget countValueField;
        TextFieldWidget countMinField;
        TextFieldWidget countMaxField;
        TextFieldWidget weightField;
        TextFieldWidget qualityField;
        String item = "";
        String tag = "";
        String table = "";
        String countValueText = "1";
        String countMinText = "0";
        String countMaxText = "0";
        String weightText = "1";
        String qualityText = "0";
        List<String> conditionNotes = List.of();
        boolean entryKilledByPlayer = false;
        String chanceText = "100";
        String chanceLootingText = "0";
        int alternativeGroup = 0;
        boolean fortuneBonus = false;
        OptionDropdownWidget toolDropdown;
        ToggleSwitchWidget killedToggle;
        TextFieldWidget chanceField;
        TextFieldWidget chanceLootingField;
        ToggleSwitchWidget fortuneToggle;
        OptionDropdownWidget groupDropdown;
        TextFieldWidget dynamicField;
        ToggleSwitchWidget explosionToggle;
        ToggleSwitchWidget lootingBonusToggle;
        boolean lootingBonusOn = false;
        TextFieldWidget bonusMinField;
        TextFieldWidget bonusMaxField;
        TextFieldWidget bonusLimitField;
        TextFieldWidget extraConditionsField;
        TextFieldWidget extraFunctionsField;
        boolean explosionDecay = false;
        String bonusMinText = "0";
        String bonusMaxText = "0";
        String bonusLimitText = "0";
        String extraConditionsText = "";
        String extraFunctionsText = "";
    }

    private static final class PoolState {
        OptionDropdownWidget rollsTypeDropdown;
        TextFieldWidget rollsValueField;
        TextFieldWidget rollsMinField;
        TextFieldWidget rollsMaxField;
        TextFieldWidget rollsNField;
        TextFieldWidget rollsPField;
        ToggleSwitchWidget survivesExplosionToggle;
        TextFieldWidget randomChanceField;
        ToggleSwitchWidget killedByPlayerToggle;
        ToggleSwitchWidget furnaceSmeltToggle;
        ToggleSwitchWidget lootingEnchantToggle;
        TextFieldWidget lootingCountField;
        TextFieldWidget lootingLimitField;
        String rollsValueText = "1";
        String rollsMinText = "0";
        String rollsMaxText = "0";
        String rollsNText = "1";
        String rollsPText = "0.5";
        boolean survivesExplosion = true;
        String randomChanceText = "100";
        boolean killedByPlayer = false;
        boolean furnaceSmelt = false;
        boolean lootingEnchant = false;
        String lootingCountText = "0";
        String lootingLimitText = "0";
        float bonusRolls = 0f;
        List<String> poolConditionNotes = List.of();
        final List<EntryState> entries = new ArrayList<>();
    }

    public LootPoolSettingsWidget(int x, int y, int w, int h, String deleteLabel, String doneLabel) {
        super(x, y, w, h, deleteLabel, doneLabel);
        pool.entries.add(new EntryState());
        rebuildStateWidgets();
        rebuildRows();
    }

    public LootPoolValues getPoolValues() {
        syncLiveText();
        return buildPoolValues();
    }

    public void applyPool(LootPoolValues values, String lootType) {
        this.lootType = lootType == null || lootType.isBlank() ? LootService.LOOT_TYPE_BLOCK : lootType;
        selectedEntry = 0;
        pool.rollsValueText = ScriptEscaping.fmt(values.rollsValue());
        pool.rollsMinText = ScriptEscaping.fmt(values.rollsMin());
        pool.rollsMaxText = ScriptEscaping.fmt(values.rollsMax());
        pool.rollsNText = Integer.toString(values.rollsN());
        pool.rollsPText = ScriptEscaping.fmt(values.rollsP());
        pool.survivesExplosion = values.survivesExplosion();
        pool.randomChanceText = formatPercent(values.randomChance());
        pool.killedByPlayer = values.killedByPlayer();
        pool.furnaceSmelt = values.furnaceSmelt();
        pool.lootingEnchant = values.lootingEnchant();
        pool.lootingCountText = ScriptEscaping.fmt(values.lootingCount());
        pool.lootingLimitText = Integer.toString(values.lootingLimit());
        pool.bonusRolls = values.bonusRolls();
        pool.poolConditionNotes = values.poolConditionNotes();
        pool.entries.clear();
        List<LootEntryValues> entries = values.entries().isEmpty()
                ? List.of(LootEntryValues.defaults())
                : values.entries();
        int entryCount = Math.min(entries.size(), MAX_ENTRIES);
        for (int j = 0; j < entryCount; j++) {
            LootEntryValues e = entries.get(j);
            EntryState entry = new EntryState();
            entry.item = e.item();
            entry.tag = e.tag();
            entry.table = e.lootTable();
            entry.countValueText = ScriptEscaping.fmt(e.countValue());
            entry.countMinText = ScriptEscaping.fmt(e.countMin());
            entry.countMaxText = ScriptEscaping.fmt(e.countMax());
            entry.weightText = Integer.toString(e.weight());
            entry.qualityText = Integer.toString(e.quality());
            entry.conditionNotes = e.conditionNotes();
            entry.entryKilledByPlayer = e.entryKilledByPlayer();
            entry.chanceText = formatPercent(e.entryChance());
            entry.chanceLootingText = formatPercent(e.entryChanceLooting());
            entry.alternativeGroup = e.alternativeGroup();
            entry.fortuneBonus = e.fortuneBonus();
            entry.explosionDecay = e.explosionDecay();
            entry.lootingBonusOn = e.lootBonusMax() > 0f;
            entry.bonusMinText = ScriptEscaping.fmt(e.lootBonusMin());
            entry.bonusMaxText = ScriptEscaping.fmt(e.lootBonusMax());
            entry.bonusLimitText = Integer.toString(e.lootBonusLimit());
            entry.extraConditionsText = e.extraConditions();
            entry.extraFunctionsText = e.extraFunctions();
            pool.entries.add(entry);
        }
        rebuildStateWidgets();
        pool.rollsTypeDropdown.setSelected(values.rollsType());
        for (int j = 0; j < pool.entries.size(); j++) {
            EntryState entry = pool.entries.get(j);
            LootEntryValues e = entries.get(j);
            entry.typeDropdown.setSelected(e.type());
            entry.countTypeDropdown.setSelected(e.countType());
            entry.toolDropdown.setSelected(
                    e.toolRequirement().isBlank() ? GROUP_NONE : e.toolRequirement());
            entry.groupDropdown.setOptions(groupOptions(entry));
            entry.groupDropdown.setSelected(groupSelected(entry));
        }
        syncWidgetContents();
        rebuildRows();
    }

    public void setEntryListListener(Runnable r) {
        entryListListener = r;
    }

    public int getSelectedEntry() {
        return selectedEntry;
    }

    public int entryCount() {
        return pool.entries.size();
    }

    public void selectEntry(int index) {
        if (pool.entries.isEmpty()) {
            return;
        }
        selectedEntry = Math.max(0, Math.min(index, pool.entries.size() - 1));
        rebuildRows();
    }

    public ItemStack entryCardIcon(int index) {
        if (index < 0 || index >= pool.entries.size()) {
            return ItemStack.EMPTY;
        }
        return slotIcon(pool.entries.get(index));
    }

    public String entryCardName(int index) {
        if (index < 0 || index >= pool.entries.size()) {
            return "";
        }
        EntryState entry = pool.entries.get(index);
        String entryType = entry.typeDropdown == null || entry.typeDropdown.getSelected() == null ? "item"
                : entry.typeDropdown.getSelected();
        String base = switch (entryType) {
            case "tag" -> entry.tag.isBlank() ? "" : "#" + entry.tag;
            case "loot_table" -> shortId(entry.table);
            case "empty" -> "empty";
            default -> {
                ItemStack icon = slotIcon(entry);
                yield icon.isEmpty() ? entry.item : icon.getHoverName().getString();
            }
        };
        return entry.alternativeGroup > 0 && !base.isBlank()
                ? base + " " + I18n.get(LootKeys.LOOT_ENTRY_GROUP_SUFFIX)
                : base;
    }

    public String entryCardId(int index) {
        if (index < 0 || index >= pool.entries.size()) {
            return "";
        }
        EntryState entry = pool.entries.get(index);
        String entryType = entry.typeDropdown == null || entry.typeDropdown.getSelected() == null ? "item"
                : entry.typeDropdown.getSelected();
        return switch (entryType) {
            case "tag" -> entry.tag;
            case "loot_table" -> entry.table;
            case "empty" -> "";
            default -> entry.item;
        };
    }

    private void notifyEntryList() {
        if (entryListListener != null) {
            entryListListener.run();
        }
    }

    public void setOnDelete(Runnable r) {
        setOnClear(r);
    }

    public void setOnDone(Runnable r) {
        setOnSave(r);
    }

    private LootPoolValues buildPoolValues() {
        List<LootEntryValues> entryValues = new ArrayList<>();
        for (EntryState entry : pool.entries) {
            String entryType = entry.typeDropdown.getSelected() == null ? "item"
                    : entry.typeDropdown.getSelected();
            String countType = entry.countTypeDropdown.getSelected() == null ? "constant"
                    : entry.countTypeDropdown.getSelected();
            String toolSelected = entry.toolDropdown.getSelected();
            String toolRequirement = toolSelected == null || GROUP_NONE.equals(toolSelected) ? "" : toolSelected;
            entryValues.add(new LootEntryValues(
                    entryType,
                    entry.item,
                    entry.tag,
                    entry.table,
                    countType,
                    parseFloat(entry.countValueText, 1f),
                    parseFloat(entry.countMinText, 0f),
                    parseFloat(entry.countMaxText, 0f),
                    parseInt(entry.weightText, 1),
                    parseInt(entry.qualityText, 0),
                    entry.lootingBonusOn ? Math.max(0f, parseFloat(entry.bonusMinText, 0f)) : 0f,
                    entry.lootingBonusOn ? Math.max(0f, parseFloat(entry.bonusMaxText, 0f)) : 0f,
                    entry.conditionNotes,
                    toolRequirement,
                    entry.entryKilledByPlayer,
                    clampChance(parseFloat(entry.chanceText, 100f) / 100f),
                    Math.max(0f, parseFloat(entry.chanceLootingText, 0f) / 100f),
                    entry.alternativeGroup,
                    entry.fortuneBonus,
                    entry.lootingBonusOn ? parseInt(entry.bonusLimitText, 0) : 0,
                    entry.explosionDecay,
                    entry.extraConditionsText,
                    entry.extraFunctionsText));
        }
        if (entryValues.isEmpty()) {
            entryValues.add(LootEntryValues.defaults());
        }
        String rollsType = pool.rollsTypeDropdown.getSelected() == null ? "constant"
                : pool.rollsTypeDropdown.getSelected();
        return new LootPoolValues(
                rollsType,
                parseFloat(pool.rollsValueText, 1f),
                parseFloat(pool.rollsMinText, 0f),
                parseFloat(pool.rollsMaxText, 0f),
                parseInt(pool.rollsNText, 1),
                parseFloat(pool.rollsPText, 0.5f),
                pool.survivesExplosion,
                clampChance(parseFloat(pool.randomChanceText, 100f) / 100f),
                pool.killedByPlayer,
                pool.furnaceSmelt,
                pool.lootingEnchant,
                parseFloat(pool.lootingCountText, 0f),
                parseInt(pool.lootingLimitText, 0),
                entryValues,
                pool.bonusRolls,
                pool.poolConditionNotes);
    }

    private void track(Widget w) {
        addWidget(w);
        dynamicWidgets.add(w);
        if (w instanceof OptionDropdownWidget dropdown) {
            addPopupDropdown(dropdown);
        }
    }

    private void untrack(Widget w) {
        removeWidget(w);
        if (w instanceof OptionDropdownWidget dropdown) {
            removePopupDropdown(dropdown);
        }
    }

    private OptionDropdownWidget dropdown(List<String> options) {
        OptionDropdownWidget d = new OptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        d.setOptions(options);
        d.setOnSelect(value -> rebuildRows());
        track(d);
        return d;
    }

    private TextFieldWidget number(Supplier<String> supplier, Consumer<String> responder, String initial) {
        return number(supplier, responder, initial, 6);
    }

    private TextFieldWidget number(Supplier<String> supplier, Consumer<String> responder, String initial,
            int maxLength) {
        TextFieldWidget f = numberField(0, 0, supplier, responder, initial, maxLength);
        track(f);
        return f;
    }

    private TextFieldWidget commit(Consumer<String> onCommit) {
        TextFieldWidget f = commitField(onCommit);
        track(f);
        return f;
    }

    private ToggleSwitchWidget toggle(BooleanSupplier supplier, Consumer<Boolean> responder) {
        ToggleSwitchWidget t = new ToggleSwitchWidget(0, 0, supplier, responder, () -> rebuildRows());
        track(t);
        return t;
    }

    private void createPoolWidgets() {
        pool.rollsTypeDropdown = dropdown(ROLLS_TYPES);
        pool.rollsValueField = number(() -> pool.rollsValueText, v -> pool.rollsValueText = v,
                pool.rollsValueText);
        pool.rollsMinField = number(() -> pool.rollsMinText, v -> pool.rollsMinText = v,
                pool.rollsMinText);
        pool.rollsMaxField = number(() -> pool.rollsMaxText, v -> pool.rollsMaxText = v,
                pool.rollsMaxText);
        pool.rollsNField = number(() -> pool.rollsNText, v -> pool.rollsNText = v, pool.rollsNText);
        pool.rollsPField = number(() -> pool.rollsPText, v -> pool.rollsPText = v, pool.rollsPText);
        pool.survivesExplosionToggle = toggle(() -> pool.survivesExplosion, v -> pool.survivesExplosion = v);
        pool.randomChanceField = number(() -> pool.randomChanceText, v -> pool.randomChanceText = v,
                pool.randomChanceText, 5);
        pool.killedByPlayerToggle = toggle(() -> pool.killedByPlayer, v -> pool.killedByPlayer = v);
        pool.furnaceSmeltToggle = toggle(() -> pool.furnaceSmelt, v -> pool.furnaceSmelt = v);
        pool.lootingEnchantToggle = toggle(() -> pool.lootingEnchant, v -> pool.lootingEnchant = v);
        pool.lootingCountField = number(() -> pool.lootingCountText, v -> pool.lootingCountText = v,
                pool.lootingCountText, 4);
        pool.lootingLimitField = number(() -> pool.lootingLimitText, v -> pool.lootingLimitText = v,
                pool.lootingLimitText);
        for (EntryState entry : pool.entries) {
            createEntryWidgets(entry);
        }
    }

    private void createEntryWidgets(EntryState entry) {
        entry.typeDropdown = dropdown(ENTRY_TYPES);
        entry.pickSlot = new PickSlot(entry);
        track(entry.pickSlot);
        entry.tableField = commit(v -> entry.table = v);
        entry.countTypeDropdown = dropdown(COUNT_TYPES);
        entry.countValueField = number(() -> entry.countValueText, v -> entry.countValueText = v,
                entry.countValueText);
        entry.countMinField = number(() -> entry.countMinText, v -> entry.countMinText = v,
                entry.countMinText);
        entry.countMaxField = number(() -> entry.countMaxText, v -> entry.countMaxText = v,
                entry.countMaxText);
        entry.weightField = number(() -> entry.weightText, v -> entry.weightText = v, entry.weightText);
        entry.qualityField = number(() -> entry.qualityText, v -> entry.qualityText = v,
                entry.qualityText);
        entry.toolDropdown = dropdown(TOOL_OPTIONS);
        entry.toolDropdown.setLabelMapper(value -> GROUP_NONE.equals(value)
                ? I18n.get(LootKeys.LOOT_TOOL_NONE)
                : I18n.get("enchantment.minecraft." + value));
        entry.killedToggle = toggle(() -> entry.entryKilledByPlayer, v -> entry.entryKilledByPlayer = v);
        entry.chanceField = number(() -> entry.chanceText, v -> entry.chanceText = v, entry.chanceText, 5);
        entry.chanceLootingField = number(() -> entry.chanceLootingText, v -> entry.chanceLootingText = v,
                entry.chanceLootingText, 5);
        entry.fortuneToggle = toggle(() -> entry.fortuneBonus, v -> entry.fortuneBonus = v);
        entry.dynamicField = commit(v -> entry.item = v);
        entry.explosionToggle = toggle(() -> entry.explosionDecay, v -> entry.explosionDecay = v);
        entry.lootingBonusToggle = toggle(() -> entry.lootingBonusOn, v -> entry.lootingBonusOn = v);
        entry.bonusMinField = number(() -> entry.bonusMinText, v -> entry.bonusMinText = v,
                entry.bonusMinText);
        entry.bonusMaxField = number(() -> entry.bonusMaxText, v -> entry.bonusMaxText = v,
                entry.bonusMaxText);
        entry.bonusLimitField = number(() -> entry.bonusLimitText, v -> entry.bonusLimitText = v,
                entry.bonusLimitText, 4);
        entry.extraConditionsField = commit(v -> entry.extraConditionsText = v);
        entry.extraConditionsField.setMaxStringLength(2048);
        entry.extraFunctionsField = commit(v -> entry.extraFunctionsText = v);
        entry.extraFunctionsField.setMaxStringLength(2048);
        entry.groupDropdown = dropdown(groupOptions(entry));
        entry.groupDropdown.setLabelMapper(value -> {
            if (GROUP_NONE.equals(value)) {
                return I18n.get(LootKeys.LOOT_TOOL_NONE);
            }
            if (GROUP_NEW.equals(value)) {
                return I18n.get(LootKeys.LOOT_GROUP_NEW);
            }
            return value;
        });
        entry.groupDropdown.setOnSelect(value -> {
            if (GROUP_NONE.equals(value)) {
                entry.alternativeGroup = 0;
            } else if (GROUP_NEW.equals(value)) {
                entry.alternativeGroup = maxGroup() + 1;
            } else {
                entry.alternativeGroup = parseInt(value, 0);
            }
            rebuildRows();
        });
    }

    private List<String> groupOptions(EntryState entry) {
        List<String> options = new ArrayList<>();
        options.add(GROUP_NONE);
        for (int g = 1; g <= Math.max(maxGroup(), entry.alternativeGroup); g++) {
            options.add(Integer.toString(g));
        }
        options.add(GROUP_NEW);
        return options;
    }

    private int maxGroup() {
        int max = 0;
        for (EntryState entry : pool.entries) {
            max = Math.max(max, entry.alternativeGroup);
        }
        return max;
    }

    private static String groupSelected(EntryState entry) {
        return entry.alternativeGroup <= 0 ? GROUP_NONE : Integer.toString(entry.alternativeGroup);
    }

    private void rebuildStateWidgets() {
        for (Widget w : dynamicWidgets) {
            untrack(w);
        }
        dynamicWidgets.clear();
        createPoolWidgets();
        syncWidgetContents();
    }

    private void syncWidgetContents() {
        if (pool.rollsTypeDropdown != null && pool.rollsTypeDropdown.getSelected() == null) {
            pool.rollsTypeDropdown.setSelected("constant");
        }
        setText(pool.rollsValueField, pool.rollsValueText);
        setText(pool.rollsMinField, pool.rollsMinText);
        setText(pool.rollsMaxField, pool.rollsMaxText);
        setText(pool.rollsNField, pool.rollsNText);
        setText(pool.rollsPField, pool.rollsPText);
        setText(pool.randomChanceField, pool.randomChanceText);
        setText(pool.lootingCountField, pool.lootingCountText);
        setText(pool.lootingLimitField, pool.lootingLimitText);
        for (EntryState entry : pool.entries) {
            if (entry.typeDropdown != null && entry.typeDropdown.getSelected() == null) {
                entry.typeDropdown.setSelected("item");
            }
            if (entry.countTypeDropdown != null && entry.countTypeDropdown.getSelected() == null) {
                entry.countTypeDropdown.setSelected("constant");
            }
            setText(entry.tableField, entry.table);
            setText(entry.dynamicField, entry.item);
            setText(entry.countValueField, entry.countValueText);
            setText(entry.countMinField, entry.countMinText);
            setText(entry.countMaxField, entry.countMaxText);
            setText(entry.weightField, entry.weightText);
            setText(entry.qualityField, entry.qualityText);
            setText(entry.chanceField, entry.chanceText);
            setText(entry.chanceLootingField, entry.chanceLootingText);
            setText(entry.bonusMinField, entry.bonusMinText);
            setText(entry.bonusMaxField, entry.bonusMaxText);
            setText(entry.bonusLimitField, entry.bonusLimitText);
            setText(entry.extraConditionsField, entry.extraConditionsText);
            setText(entry.extraFunctionsField, entry.extraFunctionsText);
        }
    }

    private static void setText(TextFieldWidget field, String value) {
        if (field != null) {
            field.setCurrentString(value == null ? "" : value);
        }
    }

    public void addEntry() {
        if (pool.entries.size() >= MAX_ENTRIES) {
            return;
        }
        syncLiveText();
        EntryState entry = new EntryState();
        pool.entries.add(entry);
        selectedEntry = pool.entries.size() - 1;
        createEntryWidgets(entry);
        syncWidgetContents();
        rebuildRows();
    }

    public void removeEntryAt(int index) {
        if (pool.entries.size() <= 1) {
            return;
        }
        if (index < 0 || index >= pool.entries.size()) {
            return;
        }
        syncLiveText();
        pool.entries.remove(index);
        selectedEntry = Math.max(0, Math.min(selectedEntry, pool.entries.size() - 1));
        rebuildStateWidgets();
        rebuildRows();
    }

    private void syncLiveText() {
        for (EntryState entry : pool.entries) {
            String rawTable = entry.tableField.getRawCurrentString();
            if (rawTable != null) {
                entry.table = rawTable.trim();
            }
            if (entry.typeDropdown != null && "dynamic".equals(entry.typeDropdown.getSelected())) {
                String rawItem = entry.dynamicField.getRawCurrentString();
                if (rawItem != null) {
                    entry.item = rawItem.trim();
                }
            }
            String rawExtraConditions = entry.extraConditionsField.getRawCurrentString();
            if (rawExtraConditions != null) {
                entry.extraConditionsText = rawExtraConditions.trim();
            }
            String rawExtraFunctions = entry.extraFunctionsField.getRawCurrentString();
            if (rawExtraFunctions != null) {
                entry.extraFunctionsText = rawExtraFunctions.trim();
            }
        }
    }

    private FieldRow row(LootField field, String labelKey, Widget control) {
        FieldRow r = new FieldRow(
                new TextTexture(Component.translatable(labelKey).getString(), UiColors.TEXT_PRIMARY)
                        .setType(TextTexture.TextType.LEFT),
                control, null);
        control.setHoverTooltips(List.of(Component.translatable(LootTooltips.key(field))));
        return r;
    }

    private static String shortId(String id) {
        if (id.startsWith("minecraft:")) {
            return id.substring("minecraft:".length());
        }
        return id;
    }

    private void rebuildRows() {
        List<FieldRow> rows = new ArrayList<>();

        int sel = Math.max(0, Math.min(selectedEntry, pool.entries.size() - 1));
        EntryState entry = pool.entries.get(sel);
        String entryType = entry.typeDropdown.getSelected() == null ? "item"
                : entry.typeDropdown.getSelected();
        rows.add(row(LootField.ENTRY_TYPE, LootKeys.LOOT_ENTRY_TYPE, entry.typeDropdown));
        if ("loot_table".equals(entryType)) {
            rows.add(row(LootField.ENTRY_LOOT_TABLE,
                    LootKeys.LOOT_ENTRY_LOOT_TABLE, entry.tableField));
        } else if ("dynamic".equals(entryType)) {
            rows.add(row(LootField.ENTRY_DYNAMIC_NAME, LootKeys.LOOT_ENTRY_DYNAMIC_NAME,
                    entry.dynamicField));
        } else if (!"empty".equals(entryType)) {
            LootField pickField =
                    "tag".equals(entryType) ? LootField.ENTRY_TAG : LootField.ENTRY_ITEM;
            String pickKey = "tag".equals(entryType)
                    ? LootKeys.LOOT_ENTRY_TAG
                    : LootKeys.LOOT_ENTRY_ITEM;
            rows.add(row(pickField, pickKey, entry.pickSlot));
        }
        rows.add(row(LootField.POOL_RANDOM_CHANCE, LootKeys.LOOT_RANDOM_CHANCE, pool.randomChanceField));
        rows.add(row(LootField.ENTRY_CHANCE, LootKeys.LOOT_ENTRY_CHANCE, entry.chanceField));
        rows.add(row(LootField.ENTRY_WEIGHT, LootKeys.LOOT_ENTRY_WEIGHT, entry.weightField));
        if ("item".equals(entryType)) {
            rows.add(row(LootField.ENTRY_QUALITY, LootKeys.LOOT_ENTRY_QUALITY, entry.qualityField));
        }
        if (!"empty".equals(entryType) && !"tag".equals(entryType)) {
            String countType = entry.countTypeDropdown.getSelected() == null ? "constant"
                    : entry.countTypeDropdown.getSelected();
            rows.add(row(LootField.ENTRY_COUNT_TYPE, LootKeys.LOOT_ENTRY_COUNT_TYPE,
                    entry.countTypeDropdown));
            if ("uniform".equals(countType)) {
                rows.add(row(LootField.ENTRY_COUNT_MIN, LootKeys.LOOT_ENTRY_COUNT_MIN,
                        entry.countMinField));
                rows.add(row(LootField.ENTRY_COUNT_MAX, LootKeys.LOOT_ENTRY_COUNT_MAX,
                        entry.countMaxField));
            } else {
                rows.add(row(LootField.ENTRY_COUNT_VALUE, LootKeys.LOOT_ENTRY_COUNT_VALUE,
                        entry.countValueField));
            }
        }
        String rollsType = pool.rollsTypeDropdown.getSelected() == null ? "constant"
                : pool.rollsTypeDropdown.getSelected();
        rows.add(row(LootField.POOL_ROLLS_TYPE, LootKeys.LOOT_POOL_ROLLS_TYPE, pool.rollsTypeDropdown));
        if ("uniform".equals(rollsType)) {
            rows.add(row(LootField.POOL_ROLLS_MIN, LootKeys.LOOT_POOL_ROLLS_MIN, pool.rollsMinField));
            rows.add(row(LootField.POOL_ROLLS_MAX, LootKeys.LOOT_POOL_ROLLS_MAX, pool.rollsMaxField));
        } else if ("binomial".equals(rollsType)) {
            rows.add(row(LootField.POOL_ROLLS_N, LootKeys.LOOT_POOL_ROLLS_N, pool.rollsNField));
            rows.add(row(LootField.POOL_ROLLS_P, LootKeys.LOOT_POOL_ROLLS_P, pool.rollsPField));
        } else {
            rows.add(row(LootField.POOL_ROLLS_VALUE, LootKeys.LOOT_POOL_ROLLS_VALUE,
                    pool.rollsValueField));
        }
        rows.add(row(LootField.ENTRY_TOOL, LootKeys.LOOT_ENTRY_TOOL, entry.toolDropdown));
        if ("fortune".equals(entry.toolDropdown.getSelected())) {
            rows.add(row(LootField.ENTRY_FORTUNE_BONUS, LootKeys.LOOT_ENTRY_FORTUNE_BONUS,
                    entry.fortuneToggle));
        }
        rows.add(row(LootField.POOL_LOOTING_ENCHANT, LootKeys.LOOT_LOOTING_ENCHANT,
                pool.lootingEnchantToggle));
        if (pool.lootingEnchant) {
            rows.add(row(LootField.ENTRY_LOOTING_BONUS, LootKeys.LOOT_ENTRY_LOOTING_BONUS,
                    entry.lootingBonusToggle));
        }
        if (pool.lootingEnchant && entry.lootingBonusOn) {
            rows.add(row(LootField.ENTRY_LOOTING_MIN, LootKeys.LOOT_ENTRY_LOOTING_MIN,
                    entry.bonusMinField));
            rows.add(row(LootField.ENTRY_LOOTING_MAX, LootKeys.LOOT_ENTRY_LOOTING_MAX,
                    entry.bonusMaxField));
            rows.add(row(LootField.ENTRY_LOOTING_LIMIT, LootKeys.LOOT_ENTRY_LOOTING_LIMIT,
                    entry.bonusLimitField));
        }
        if (pool.lootingEnchant) {
            rows.add(row(LootField.POOL_LOOTING_COUNT, LootKeys.LOOT_LOOTING_COUNT,
                    pool.lootingCountField));
            rows.add(row(LootField.POOL_LOOTING_LIMIT, LootKeys.LOOT_LOOTING_LIMIT,
                    pool.lootingLimitField));
        }
        if (pool.lootingEnchant) {
            rows.add(row(LootField.ENTRY_CHANCE_LOOTING, LootKeys.LOOT_ENTRY_CHANCE_LOOTING,
                    entry.chanceLootingField));
        }
        if (LootService.LOOT_TYPE_ENTITY.equals(lootType)) {
            rows.add(row(LootField.POOL_KILLED_BY_PLAYER, LootKeys.LOOT_KILLED_BY_PLAYER,
                    pool.killedByPlayerToggle));
        }
        rows.add(row(LootField.ENTRY_KILLED_BY_PLAYER, LootKeys.LOOT_ENTRY_KILLED_BY_PLAYER,
                entry.killedToggle));
        rows.add(row(LootField.POOL_SURVIVES_EXPLOSION, LootKeys.LOOT_SURVIVES_EXPLOSION,
                pool.survivesExplosionToggle));
        rows.add(row(LootField.ENTRY_EXPLOSION_DECAY, LootKeys.LOOT_ENTRY_EXPLOSION_DECAY,
                entry.explosionToggle));
        rows.add(row(LootField.POOL_FURNACE_SMELT, LootKeys.LOOT_FURNACE_SMELT,
                pool.furnaceSmeltToggle));
        entry.groupDropdown.setOptions(groupOptions(entry));
        entry.groupDropdown.setSelected(groupSelected(entry));
        rows.add(row(LootField.ENTRY_GROUP, LootKeys.LOOT_ENTRY_GROUP, entry.groupDropdown));
        rows.add(row(LootField.ENTRY_EXTRA_CONDITIONS, LootKeys.LOOT_ENTRY_EXTRA_CONDITIONS,
                entry.extraConditionsField));
        rows.add(row(LootField.ENTRY_EXTRA_FUNCTIONS, LootKeys.LOOT_ENTRY_EXTRA_FUNCTIONS,
                entry.extraFunctionsField));

        setRows(rows);
        notifyEntryList();
    }

    private static String formatPercent(float fraction) {
        return ScriptEscaping.fmt(Math.round(fraction * 10000f) / 100f);
    }

    private static TextFieldWidget commitField(Consumer<String> onCommit) {
        CommitField field = new CommitField(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }
}
