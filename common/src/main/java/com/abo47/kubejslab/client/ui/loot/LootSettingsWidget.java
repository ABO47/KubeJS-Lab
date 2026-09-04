package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.widgets.ActionButton;
import com.abo47.kubejslab.client.ui.widgets.CommitField;
import com.abo47.kubejslab.client.ui.widgets.OptionDropdownWidget;
import com.abo47.kubejslab.client.ui.widgets.RowCardSettings;
import com.abo47.kubejslab.loot.model.LootAction;
import com.abo47.kubejslab.loot.model.LootField;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.runtime.LootService;


public final class LootSettingsWidget extends RowCardSettings {
    public static final int MAX_POOLS = 6;

    private static final List<String> LOOT_TYPES = List.of(
            LootService.LOOT_TYPE_BLOCK,
            LootService.LOOT_TYPE_ENTITY,
            LootService.LOOT_TYPE_CHEST,
            LootService.LOOT_TYPE_FISHING,
            LootService.LOOT_TYPE_GIFT,
            LootService.LOOT_TYPE_GENERIC);

    public interface PoolEditHandler {
        void edit(int index, LootPoolValues snapshot, String lootType);
    }

    private final OptionDropdownWidget lootTypeDropdown;
    private final TextFieldWidget targetIdField;
    private final TextFieldWidget customIdField;
    private final List<ActionButton> editButtons = new ArrayList<>();
    private final ActionButton addPoolButton;

    private String targetId = "";
    private String customId = "";
    private int droppedPools = 0;
    private int droppedEntries = 0;
    private final List<LootPoolValues> pools = new ArrayList<>();
    private PoolEditHandler editHandler;
    private Runnable previewListener;

    public LootSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatable(LootKeys.LOOT_CLEAR).getString(),
                Component.translatable(LootKeys.LOOT_SAVE).getString());

        lootTypeDropdown = new OptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
        lootTypeDropdown.setOptions(LOOT_TYPES);
        lootTypeDropdown.setOnSelect(value -> {
            rebuildRows();
            firePreview();
        });
        addWidget(lootTypeDropdown);
        addPopupDropdown(lootTypeDropdown);

        targetIdField = commitField(v -> {
            targetId = v;
            firePreview();
        });
        addWidget(targetIdField);

        customIdField = commitField(v -> {
            customId = v;
            firePreview();
        });
        addWidget(customIdField);

        for (int i = 0; i < MAX_POOLS; i++) {
            int index = i;
            ActionButton edit = new ActionButton(0, 0, CONTROL_W, FIELD_H,
                    Component.translatable(LootKeys.LOOT_EDIT).getString(), () -> openPool(index));
            addWidget(edit);
            editButtons.add(edit);
        }
        addPoolButton = new ActionButton(0, 0, CONTROL_W, FIELD_H,
                Component.translatable(LootKeys.LOOT_ADD).getString(), this::addPool);
        addWidget(addPoolButton);

        pools.add(LootPoolValues.defaults());
    }

    public void setPreviewListener(Runnable r) {
        previewListener = r;
    }

    public void setOnEditPool(PoolEditHandler handler) {
        editHandler = handler;
    }

    private void firePreview() {
        if (previewListener != null) {
            previewListener.run();
        }
    }

    public void setLootType(String type) {
        lootTypeDropdown.setSelected(type);
    }

    public String getLootType() {
        String sel = lootTypeDropdown.getSelected();
        return sel == null || sel.isBlank() ? LootService.LOOT_TYPE_BLOCK : sel;
    }

    public void setFields(List<LootField> fields) {
        rebuildRows();
    }

    private void openPool(int index) {
        if (index < 0 || index >= pools.size() || editHandler == null) {
            return;
        }
        editHandler.edit(index, pools.get(index), getLootType());
    }

    private void addPool() {
        if (pools.size() >= MAX_POOLS) {
            return;
        }
        syncLiveText();
        pools.add(LootPoolValues.defaults());
        rebuildRows();
        firePreview();
        openPool(pools.size() - 1);
    }

    public void deletePoolAt(int index) {
        if (index < 0 || index >= pools.size()) {
            return;
        }
        if (pools.size() > 1) {
            pools.remove(index);
        } else {
            pools.set(0, LootPoolValues.defaults());
        }
        rebuildRows();
        firePreview();
    }

    public void applyPoolEdit(int index, LootPoolValues values) {
        if (index < 0 || index >= pools.size() || values == null) {
            return;
        }
        pools.set(index, values);
        rebuildRows();
        firePreview();
    }

    public String poolTitle(int index) {
        String base = Component.translatable(LootKeys.LOOT_POOL).getString();
        return pools.size() <= 1 ? base : base + " " + (index + 1);
    }

    private FieldRow row(LootField field, String labelKey, Widget control) {
        FieldRow r = new FieldRow(
                new TextTexture(Component.translatable(labelKey).getString(), UiColors.TEXT_PRIMARY)
                        .setType(TextTexture.TextType.LEFT),
                control, null);
        control.setHoverTooltips(List.of(Component.translatable(LootTooltips.key(field))));
        return r;
    }

    private FieldRow plainRow(String labelText, Widget control, boolean disabled) {
        return new FieldRow(
                new TextTexture(labelText, UiColors.TEXT_PRIMARY).setType(TextTexture.TextType.LEFT), control,
                null, disabled);
    }

    private void rebuildRows() {
        List<FieldRow> rows = new ArrayList<>();

        rows.add(row(LootField.LOOT_TYPE, LootKeys.LOOT_TYPE, lootTypeDropdown));
        rows.add(row(LootField.TARGET_ID, LootKeys.LOOT_TARGET_ID, targetIdField));
        rows.add(row(LootField.CUSTOM_ID, LootKeys.LOOT_CUSTOM_ID, customIdField));

        for (int i = 0; i < pools.size(); i++) {
            rows.add(plainRow(poolTitle(i), editButtons.get(i), false));
        }
        rows.add(plainRow(Component.translatable(LootKeys.LOOT_NEW_POOL).getString(), addPoolButton,
                pools.size() >= MAX_POOLS));

        setRows(rows);
    }

    private void syncLiveText() {
        String rawTarget = targetIdField.getRawCurrentString();
        if (rawTarget != null) {
            targetId = rawTarget.trim();
        }
        String rawCustom = customIdField.getRawCurrentString();
        if (rawCustom != null) {
            customId = rawCustom.trim();
        }
    }

    public LootFieldValues getValues() {
        syncLiveText();
        List<LootPoolValues> snapshot = pools.isEmpty()
                ? List.of(LootPoolValues.defaults())
                : List.copyOf(pools);
        return new LootFieldValues(targetId, customId, snapshot, droppedPools, droppedEntries);
    }

    public List<String> getTags() {
        return List.of();
    }

    public List<LootAction> getActions() {
        return List.of();
    }

    public void applyValues(LootFieldValues v) {
        targetId = v.targetId();
        customId = v.customId();
        droppedPools = v.droppedPools();
        droppedEntries = v.droppedEntries();
        pools.clear();
        List<LootPoolValues> source = v.pools().isEmpty() ? List.of(LootPoolValues.defaults()) : v.pools();
        int poolCount = Math.min(source.size(), MAX_POOLS);
        droppedPools += Math.max(0, source.size() - poolCount);
        for (int i = 0; i < poolCount; i++) {
            pools.add(source.get(i));
        }
        targetIdField.setCurrentString(targetId);
        customIdField.setCurrentString(customId);
        rebuildRows();
    }

    public void setClearHandler(Runnable r) {
        setOnClear(r);
    }

    public void setSaveHandler(Runnable r) {
        setOnSave(r);
    }

    private static TextFieldWidget commitField(Consumer<String> onCommit) {
        CommitField field = new CommitField(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }
}
