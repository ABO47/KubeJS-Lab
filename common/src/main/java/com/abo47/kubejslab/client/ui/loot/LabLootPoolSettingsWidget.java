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
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabPickTarget;
import com.abo47.kubejslab.client.ui.base.LabRowCardSettingsWidget;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;
import com.abo47.kubejslab.client.ui.picker.LabPick;
import com.abo47.kubejslab.client.ui.picker.LabPickerEntries;
import com.abo47.kubejslab.loot.model.LabLootEntryValues;
import com.abo47.kubejslab.loot.model.LabLootField;
import com.abo47.kubejslab.loot.model.LabLootPoolValues;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootPoolSettingsWidget extends LabRowCardSettingsWidget {
    public static final int MAX_ENTRIES = 64;

    private static final List<String> ROLLS_TYPES = List.of("constant", "uniform", "binomial");
    private static final List<String> ENTRY_TYPES = List.of("item", "tag", "empty", "loot_table", "dynamic");
    private static final List<String> COUNT_TYPES = List.of("constant", "uniform");
    private static final List<String> TOOL_OPTIONS = List.of("none", "silk_touch", "fortune");
    private static final String GROUP_NONE = "none";
    private static final String GROUP_NEW = "new group";

    private final PoolState pool = new PoolState();
    private String lootType = LabLootService.LOOT_TYPE_BLOCK;
    private final List<Widget> dynamicWidgets = new ArrayList<>();
    private LabPick pendingPick;
    private int selectedEntry;
    private Runnable entryListListener;

    private final class PickSlot extends Widget implements LabPickTarget {
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
                LabColors.bordered(0, LabColors.INTERACTIVE).draw(g, mx, my, x, y, 15, 15);
            } else if (isMouseOverElement(mx, my)) {
                LabGlow.drawGlow(g, mx, my, x, y, 15, 15);
            }
            setHoverTooltips(slotTips(entry));
        }
    }

    public boolean offerPick(LabPick pick) {
        if (pick instanceof LabPick.Item || pick instanceof LabPick.Tag) {
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
        if (pendingPick instanceof LabPick.Item item) {
            entry.item = BuiltInRegistries.ITEM.getKey(item.stack().getItem()).toString();
            entry.typeDropdown.setSelected("item");
        } else if (pendingPick instanceof LabPick.Tag tag) {
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
            return tagId == null ? ItemStack.EMPTY : LabPickerEntries.tagPreview(tagId);
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
            List<ItemStack> previews = LabPickerEntries.tagPreviews(ResourceLocation.tryParse(entry.tag));
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
        return List.of(Component.translatable(LabGuiKeys.LAB_LOOT_PICK_HINT));
    }

    private static final class EntryState {
        LabOptionDropdownWidget typeDropdown;
        LabOptionDropdownWidget countTypeDropdown;
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
        LabOptionDropdownWidget toolDropdown;
        LabToggleSwitchWidget killedToggle;
        TextFieldWidget chanceField;
        TextFieldWidget chanceLootingField;
        LabToggleSwitchWidget fortuneToggle;
        LabOptionDropdownWidget groupDropdown;
        TextFieldWidget dynamicField;
        LabToggleSwitchWidget explosionToggle;
        LabToggleSwitchWidget lootingBonusToggle;
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
        InfoText notesInfo;
    }

    private static final class InfoText extends WidgetGroup {
        private TextTexture text;

        InfoText() {
            super(0, 0, CONTROL_W, FIELD_H);
            setValue("");
        }

        void setValue(String value) {
            this.text = new TextTexture(value == null ? "" : value, LabColors.TEXT_MUTED)
                    .setType(TextTexture.TextType.LEFT)
                    .setWidth(CONTROL_W);
            setHoverTooltips(List.of(Component.literal(value == null ? "" : value)));
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
            text.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
    }

    private static final class PoolState {
        LabOptionDropdownWidget rollsTypeDropdown;
        TextFieldWidget rollsValueField;
        TextFieldWidget rollsMinField;
        TextFieldWidget rollsMaxField;
        TextFieldWidget rollsNField;
        TextFieldWidget rollsPField;
        LabToggleSwitchWidget survivesExplosionToggle;
        TextFieldWidget randomChanceField;
        LabToggleSwitchWidget killedByPlayerToggle;
        LabToggleSwitchWidget furnaceSmeltToggle;
        LabToggleSwitchWidget lootingEnchantToggle;
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
        InfoText poolNotesInfo;
        final List<EntryState> entries = new ArrayList<>();
    }

    public LabLootPoolSettingsWidget(int x, int y, int w, int h, String deleteLabel, String doneLabel) {
        super(x, y, w, h, deleteLabel, doneLabel);
        pool.entries.add(new EntryState());
        rebuildStateWidgets();
        rebuildRows();
    }

    public LabLootPoolValues getPoolValues() {
        syncLiveText();
        return buildPoolValues();
    }

    public void applyPool(LabLootPoolValues values, String lootType) {
        this.lootType = lootType == null || lootType.isBlank() ? LabLootService.LOOT_TYPE_BLOCK : lootType;
        selectedEntry = 0;
        pool.rollsValueText = formatFloat(values.rollsValue());
        pool.rollsMinText = formatFloat(values.rollsMin());
        pool.rollsMaxText = formatFloat(values.rollsMax());
        pool.rollsNText = Integer.toString(values.rollsN());
        pool.rollsPText = formatFloat(values.rollsP());
        pool.survivesExplosion = values.survivesExplosion();
        pool.randomChanceText = formatPercent(values.randomChance());
        pool.killedByPlayer = values.killedByPlayer();
        pool.furnaceSmelt = values.furnaceSmelt();
        pool.lootingEnchant = values.lootingEnchant();
        pool.lootingCountText = formatFloat(values.lootingCount());
        pool.lootingLimitText = Integer.toString(values.lootingLimit());
        pool.bonusRolls = values.bonusRolls();
        pool.poolConditionNotes = values.poolConditionNotes();
        pool.entries.clear();
        List<LabLootEntryValues> entries = values.entries().isEmpty()
                ? List.of(LabLootEntryValues.defaults())
                : values.entries();
        int entryCount = Math.min(entries.size(), MAX_ENTRIES);
        for (int j = 0; j < entryCount; j++) {
            LabLootEntryValues e = entries.get(j);
            EntryState entry = new EntryState();
            entry.item = e.item();
            entry.tag = e.tag();
            entry.table = e.lootTable();
            entry.countValueText = formatFloat(e.countValue());
            entry.countMinText = formatFloat(e.countMin());
            entry.countMaxText = formatFloat(e.countMax());
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
            entry.bonusMinText = formatFloat(e.lootBonusMin());
            entry.bonusMaxText = formatFloat(e.lootBonusMax());
            entry.bonusLimitText = Integer.toString(e.lootBonusLimit());
            entry.extraConditionsText = e.extraConditions();
            entry.extraFunctionsText = e.extraFunctions();
            pool.entries.add(entry);
        }
        rebuildStateWidgets();
        pool.rollsTypeDropdown.setSelected(values.rollsType());
        for (int j = 0; j < pool.entries.size(); j++) {
            EntryState entry = pool.entries.get(j);
            LabLootEntryValues e = entries.get(j);
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
                ? base + " " + I18n.get(LabGuiKeys.LAB_LOOT_ENTRY_GROUP_SUFFIX)
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

    private LabLootPoolValues buildPoolValues() {
        List<LabLootEntryValues> entryValues = new ArrayList<>();
        for (EntryState entry : pool.entries) {
            String entryType = entry.typeDropdown.getSelected() == null ? "item"
                    : entry.typeDropdown.getSelected();
            String countType = entry.countTypeDropdown.getSelected() == null ? "constant"
                    : entry.countTypeDropdown.getSelected();
            String toolSelected = entry.toolDropdown.getSelected();
            String toolRequirement = toolSelected == null || GROUP_NONE.equals(toolSelected) ? "" : toolSelected;
            entryValues.add(new LabLootEntryValues(
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
            entryValues.add(LabLootEntryValues.defaults());
        }
        String rollsType = pool.rollsTypeDropdown.getSelected() == null ? "constant"
                : pool.rollsTypeDropdown.getSelected();
        return new LabLootPoolValues(
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
        if (w instanceof LabOptionDropdownWidget dropdown) {
            addPopupDropdown(dropdown);
        }
    }

    private void untrack(Widget w) {
        removeWidget(w);
        if (w instanceof LabOptionDropdownWidget dropdown) {
            removePopupDropdown(dropdown);
        }
    }

    private LabOptionDropdownWidget dropdown(List<String> options) {
        LabOptionDropdownWidget d = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
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

    private LabToggleSwitchWidget toggle(BooleanSupplier supplier, Consumer<Boolean> responder) {
        LabToggleSwitchWidget t = new LabToggleSwitchWidget(0, 0, supplier, responder, () -> rebuildRows());
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
        pool.poolNotesInfo = new InfoText();
        track(pool.poolNotesInfo);
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
                ? I18n.get(LabGuiKeys.LAB_LOOT_TOOL_NONE)
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
        entry.notesInfo = new InfoText();
        track(entry.notesInfo);
        entry.groupDropdown = dropdown(groupOptions(entry));
        entry.groupDropdown.setLabelMapper(value -> {
            if (GROUP_NONE.equals(value)) {
                return I18n.get(LabGuiKeys.LAB_LOOT_TOOL_NONE);
            }
            if (GROUP_NEW.equals(value)) {
                return I18n.get(LabGuiKeys.LAB_LOOT_GROUP_NEW);
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

    private FieldRow row(LabLootField field, String labelKey, Widget control) {
        FieldRow r = new FieldRow(
                new TextTexture(Component.translatable(labelKey).getString(), LabColors.TEXT_PRIMARY)
                        .setType(TextTexture.TextType.LEFT),
                control, null);
        control.setHoverTooltips(List.of(Component.translatable(LabLootTooltips.key(field))));
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
        rows.add(row(LabLootField.ENTRY_TYPE, LabGuiKeys.LAB_LOOT_ENTRY_TYPE, entry.typeDropdown));
        if ("loot_table".equals(entryType)) {
            rows.add(row(LabLootField.ENTRY_LOOT_TABLE,
                    LabGuiKeys.LAB_LOOT_ENTRY_LOOT_TABLE, entry.tableField));
        } else if ("dynamic".equals(entryType)) {
            rows.add(row(LabLootField.ENTRY_DYNAMIC_NAME, LabGuiKeys.LAB_LOOT_ENTRY_DYNAMIC_NAME,
                    entry.dynamicField));
        } else if (!"empty".equals(entryType)) {
            LabLootField pickField =
                    "tag".equals(entryType) ? LabLootField.ENTRY_TAG : LabLootField.ENTRY_ITEM;
            String pickKey = "tag".equals(entryType)
                    ? LabGuiKeys.LAB_LOOT_ENTRY_TAG
                    : LabGuiKeys.LAB_LOOT_ENTRY_ITEM;
            rows.add(row(pickField, pickKey, entry.pickSlot));
        }
        rows.add(row(LabLootField.POOL_RANDOM_CHANCE, LabGuiKeys.LAB_LOOT_RANDOM_CHANCE, pool.randomChanceField));
        rows.add(row(LabLootField.ENTRY_CHANCE, LabGuiKeys.LAB_LOOT_ENTRY_CHANCE, entry.chanceField));
        rows.add(row(LabLootField.ENTRY_WEIGHT, LabGuiKeys.LAB_LOOT_ENTRY_WEIGHT, entry.weightField));
        if ("item".equals(entryType)) {
            rows.add(row(LabLootField.ENTRY_QUALITY, LabGuiKeys.LAB_LOOT_ENTRY_QUALITY, entry.qualityField));
        }
        if (!"empty".equals(entryType) && !"tag".equals(entryType)) {
            String countType = entry.countTypeDropdown.getSelected() == null ? "constant"
                    : entry.countTypeDropdown.getSelected();
            rows.add(row(LabLootField.ENTRY_COUNT_TYPE, LabGuiKeys.LAB_LOOT_ENTRY_COUNT_TYPE,
                    entry.countTypeDropdown));
            if ("uniform".equals(countType)) {
                rows.add(row(LabLootField.ENTRY_COUNT_MIN, LabGuiKeys.LAB_LOOT_ENTRY_COUNT_MIN,
                        entry.countMinField));
                rows.add(row(LabLootField.ENTRY_COUNT_MAX, LabGuiKeys.LAB_LOOT_ENTRY_COUNT_MAX,
                        entry.countMaxField));
            } else {
                rows.add(row(LabLootField.ENTRY_COUNT_VALUE, LabGuiKeys.LAB_LOOT_ENTRY_COUNT_VALUE,
                        entry.countValueField));
            }
        }
        String rollsType = pool.rollsTypeDropdown.getSelected() == null ? "constant"
                : pool.rollsTypeDropdown.getSelected();
        rows.add(row(LabLootField.POOL_ROLLS_TYPE, LabGuiKeys.LAB_LOOT_POOL_ROLLS_TYPE, pool.rollsTypeDropdown));
        if ("uniform".equals(rollsType)) {
            rows.add(row(LabLootField.POOL_ROLLS_MIN, LabGuiKeys.LAB_LOOT_POOL_ROLLS_MIN, pool.rollsMinField));
            rows.add(row(LabLootField.POOL_ROLLS_MAX, LabGuiKeys.LAB_LOOT_POOL_ROLLS_MAX, pool.rollsMaxField));
        } else if ("binomial".equals(rollsType)) {
            rows.add(row(LabLootField.POOL_ROLLS_N, LabGuiKeys.LAB_LOOT_POOL_ROLLS_N, pool.rollsNField));
            rows.add(row(LabLootField.POOL_ROLLS_P, LabGuiKeys.LAB_LOOT_POOL_ROLLS_P, pool.rollsPField));
        } else {
            rows.add(row(LabLootField.POOL_ROLLS_VALUE, LabGuiKeys.LAB_LOOT_POOL_ROLLS_VALUE,
                    pool.rollsValueField));
        }
        rows.add(row(LabLootField.ENTRY_TOOL, LabGuiKeys.LAB_LOOT_ENTRY_TOOL, entry.toolDropdown));
        if ("fortune".equals(entry.toolDropdown.getSelected())) {
            rows.add(row(LabLootField.ENTRY_FORTUNE_BONUS, LabGuiKeys.LAB_LOOT_ENTRY_FORTUNE_BONUS,
                    entry.fortuneToggle));
        }
        rows.add(row(LabLootField.POOL_LOOTING_ENCHANT, LabGuiKeys.LAB_LOOT_LOOTING_ENCHANT,
                pool.lootingEnchantToggle));
        if (pool.lootingEnchant) {
            rows.add(row(LabLootField.ENTRY_LOOTING_BONUS, LabGuiKeys.LAB_LOOT_ENTRY_LOOTING_BONUS,
                    entry.lootingBonusToggle));
        }
        if (pool.lootingEnchant && entry.lootingBonusOn) {
            rows.add(row(LabLootField.ENTRY_LOOTING_MIN, LabGuiKeys.LAB_LOOT_ENTRY_LOOTING_MIN,
                    entry.bonusMinField));
            rows.add(row(LabLootField.ENTRY_LOOTING_MAX, LabGuiKeys.LAB_LOOT_ENTRY_LOOTING_MAX,
                    entry.bonusMaxField));
            rows.add(row(LabLootField.ENTRY_LOOTING_LIMIT, LabGuiKeys.LAB_LOOT_ENTRY_LOOTING_LIMIT,
                    entry.bonusLimitField));
        }
        if (pool.lootingEnchant) {
            rows.add(row(LabLootField.POOL_LOOTING_COUNT, LabGuiKeys.LAB_LOOT_LOOTING_COUNT,
                    pool.lootingCountField));
            rows.add(row(LabLootField.POOL_LOOTING_LIMIT, LabGuiKeys.LAB_LOOT_LOOTING_LIMIT,
                    pool.lootingLimitField));
            rows.add(row(LabLootField.ENTRY_CHANCE_LOOTING, LabGuiKeys.LAB_LOOT_ENTRY_CHANCE_LOOTING,
                    entry.chanceLootingField));
        }
        if (LabLootService.LOOT_TYPE_ENTITY.equals(lootType)) {
            rows.add(row(LabLootField.POOL_KILLED_BY_PLAYER, LabGuiKeys.LAB_LOOT_KILLED_BY_PLAYER,
                    pool.killedByPlayerToggle));
        }
        rows.add(row(LabLootField.ENTRY_KILLED_BY_PLAYER, LabGuiKeys.LAB_LOOT_ENTRY_KILLED_BY_PLAYER,
                entry.killedToggle));
        rows.add(row(LabLootField.POOL_SURVIVES_EXPLOSION, LabGuiKeys.LAB_LOOT_SURVIVES_EXPLOSION,
                pool.survivesExplosionToggle));
        rows.add(row(LabLootField.ENTRY_EXPLOSION_DECAY, LabGuiKeys.LAB_LOOT_ENTRY_EXPLOSION_DECAY,
                entry.explosionToggle));
        rows.add(row(LabLootField.POOL_FURNACE_SMELT, LabGuiKeys.LAB_LOOT_FURNACE_SMELT,
                pool.furnaceSmeltToggle));
        entry.groupDropdown.setOptions(groupOptions(entry));
        entry.groupDropdown.setSelected(groupSelected(entry));
        rows.add(row(LabLootField.ENTRY_GROUP, LabGuiKeys.LAB_LOOT_ENTRY_GROUP, entry.groupDropdown));
        rows.add(row(LabLootField.ENTRY_EXTRA_CONDITIONS, LabGuiKeys.LAB_LOOT_ENTRY_EXTRA_CONDITIONS,
                entry.extraConditionsField));
        rows.add(row(LabLootField.ENTRY_EXTRA_FUNCTIONS, LabGuiKeys.LAB_LOOT_ENTRY_EXTRA_FUNCTIONS,
                entry.extraFunctionsField));
        if (!entry.conditionNotes.isEmpty()) {
            entry.notesInfo.setValue(LabLootNoteText.joinStrings(entry.conditionNotes, ", "));
            rows.add(infoRow(LabGuiKeys.LAB_LOOT_ENTRY_VANILLA, entry.notesInfo));
        }
        if (!pool.poolConditionNotes.isEmpty()) {
            pool.poolNotesInfo.setValue(LabLootNoteText.joinStrings(pool.poolConditionNotes, ", "));
            rows.add(infoRow(LabGuiKeys.LAB_LOOT_POOL_VANILLA, pool.poolNotesInfo));
        }

        setRows(rows);
        notifyEntryList();
    }

    private FieldRow infoRow(String labelKey, InfoText info) {
        return new FieldRow(
                new TextTexture(Component.translatable(labelKey).getString(), LabColors.TEXT_MUTED)
                        .setType(TextTexture.TextType.LEFT),
                info, null);
    }

    private static String formatPercent(float fraction) {
        return formatFloat(Math.round(fraction * 10000f) / 100f);
    }

    private static TextFieldWidget commitField(Consumer<String> onCommit) {
        LabCommitFieldWidget field = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }
}
