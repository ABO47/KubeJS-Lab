package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabCommitFieldWidget;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabOptionDropdownWidget;
import com.abo47.kubejslab.client.ui.base.LabRowCardSettingsWidget;
import com.abo47.kubejslab.loot.model.LabLootAction;
import com.abo47.kubejslab.loot.model.LabLootField;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.model.LabLootPoolValues;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootSettingsWidget extends LabRowCardSettingsWidget {
    public static final int MAX_POOLS = 6;

    private static final List<String> LOOT_TYPES = List.of(
            LabLootService.LOOT_TYPE_BLOCK,
            LabLootService.LOOT_TYPE_ENTITY,
            LabLootService.LOOT_TYPE_CHEST,
            LabLootService.LOOT_TYPE_FISHING,
            LabLootService.LOOT_TYPE_GIFT,
            LabLootService.LOOT_TYPE_GENERIC);

    public interface PoolEditHandler {
        void edit(int index, LabLootPoolValues snapshot, String lootType);
    }

    private final LabOptionDropdownWidget lootTypeDropdown;
    private final TextFieldWidget targetIdField;
    private final TextFieldWidget customIdField;
    private final List<LabActionButton> editButtons = new ArrayList<>();
    private final LabActionButton addPoolButton;

    private String targetId = "";
    private String customId = "";
    private final List<LabLootPoolValues> pools = new ArrayList<>();
    private PoolEditHandler editHandler;
    private Runnable previewListener;

    public LabLootSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatable(LabGuiKeys.LAB_LOOT_CLEAR).getString(),
                Component.translatable(LabGuiKeys.LAB_LOOT_SAVE).getString());

        lootTypeDropdown = new LabOptionDropdownWidget(0, 0, CONTROL_W, FIELD_H);
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
            LabActionButton edit = new LabActionButton(0, 0, CONTROL_W, FIELD_H,
                    Component.translatable(LabGuiKeys.LAB_LOOT_EDIT).getString(), () -> openPool(index));
            addWidget(edit);
            editButtons.add(edit);
        }
        addPoolButton = new LabActionButton(0, 0, CONTROL_W, FIELD_H,
                Component.translatable(LabGuiKeys.LAB_LOOT_ADD).getString(), this::addPool);
        addWidget(addPoolButton);

        pools.add(LabLootPoolValues.defaults());
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
        return sel == null || sel.isBlank() ? LabLootService.LOOT_TYPE_BLOCK : sel;
    }

    public void setFields(List<LabLootField> fields) {
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
        pools.add(LabLootPoolValues.defaults());
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
            pools.set(0, LabLootPoolValues.defaults());
        }
        rebuildRows();
        firePreview();
    }

    public void applyPoolEdit(int index, LabLootPoolValues values) {
        if (index < 0 || index >= pools.size() || values == null) {
            return;
        }
        pools.set(index, values);
        rebuildRows();
        firePreview();
    }

    public String poolTitle(int index) {
        String base = Component.translatable(LabGuiKeys.LAB_LOOT_POOL).getString();
        return pools.size() <= 1 ? base : base + " " + (index + 1);
    }

    private FieldRow row(LabLootField field, String labelKey, Widget control) {
        FieldRow r = new FieldRow(
                new TextTexture(Component.translatable(labelKey).getString(), LabColors.TEXT_PRIMARY)
                        .setType(TextTexture.TextType.LEFT),
                control, null);
        control.setHoverTooltips(List.of(Component.translatable(LabLootTooltips.key(field))));
        return r;
    }

    private FieldRow plainRow(String labelText, Widget control, boolean disabled) {
        return new FieldRow(
                new TextTexture(labelText, LabColors.TEXT_PRIMARY).setType(TextTexture.TextType.LEFT), control,
                null, disabled);
    }

    private void rebuildRows() {
        List<FieldRow> rows = new ArrayList<>();

        rows.add(row(LabLootField.LOOT_TYPE, LabGuiKeys.LAB_LOOT_TYPE, lootTypeDropdown));
        rows.add(row(LabLootField.TARGET_ID, LabGuiKeys.LAB_LOOT_TARGET_ID, targetIdField));
        rows.add(row(LabLootField.CUSTOM_ID, LabGuiKeys.LAB_LOOT_CUSTOM_ID, customIdField));

        for (int i = 0; i < pools.size(); i++) {
            rows.add(plainRow(poolTitle(i), editButtons.get(i), false));
        }
        rows.add(plainRow(Component.translatable(LabGuiKeys.LAB_LOOT_NEW_POOL).getString(), addPoolButton,
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

    public LabLootFieldValues getValues() {
        syncLiveText();
        List<LabLootPoolValues> snapshot = pools.isEmpty()
                ? List.of(LabLootPoolValues.defaults())
                : List.copyOf(pools);
        return new LabLootFieldValues(targetId, customId, snapshot);
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
        pools.clear();
        List<LabLootPoolValues> source = v.pools().isEmpty() ? List.of(LabLootPoolValues.defaults()) : v.pools();
        int poolCount = Math.min(source.size(), MAX_POOLS);
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
        LabCommitFieldWidget field = new LabCommitFieldWidget(0, 0, CONTROL_W, FIELD_H, null, onCommit);
        configureCommit(field);
        return field;
    }
}
