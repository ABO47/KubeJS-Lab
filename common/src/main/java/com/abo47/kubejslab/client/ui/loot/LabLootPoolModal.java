package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabModalHeader;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuAnimation;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextMenuPanel;
import com.abo47.kubejslab.client.ui.picker.LabPick;
import com.abo47.kubejslab.loot.model.LabLootPoolValues;


public final class LabLootPoolModal {
    public static final int MODAL_W = 432;
    public static final int MODAL_H = 260;
    private static final int BODY_Y = 22;
    private static final int BODY_PAD = 8;
    private static final int LIST_X = 8;
    private static final int LIST_W = 150;
    private static final int LIST_GAP = 4;
    private static final int CONTAINER_PAD = 4;

    private static final int DIM_COLOR = LabColors.withAlpha(LabColors.SURFACE_BASE, 140);
    private static final IGuiTexture CONTAINER_TEXTURE = LabColors.bordered(
            LabColors.withAlpha(LabColors.SURFACE_PANEL_ALT, 120), LabColors.BORDER_BASE);

    private final WidgetGroup layer;
    private final WidgetGroup panel;
    private final LabLootPoolSettingsWidget settings;
    private Runnable onClose;
    private WidgetGroup contextMenu;
    private ButtonWidget contextDismiss;
    private long contextMenuMs;

    private LabLootPoolModal(WidgetGroup layer, String title, LabLootPoolValues pool, String lootType,
            Runnable onDelete, Consumer<LabLootPoolValues> onApply) {
        this.layer = layer;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H,
                new ColorRectTexture(DIM_COLOR), cd -> {
                }).setClientSideWidget());

        this.panel = new WidgetGroup((LabLayout.ROOT_W - MODAL_W) / 2,
                (LabLayout.ROOT_H - MODAL_H) / 2, MODAL_W, MODAL_H) {
            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                super.mouseClicked(mx, my, button);
                return isMouseOverElement(mx, my);
            }
        };
        this.panel.setBackground(LabColors.bordered(
                LabColors.withAlpha(LabColors.SURFACE_BASE, 252), LabColors.BORDER_ACCENT));
        layer.addWidget(panel);
        panel.addWidget(LabModalHeader.titleLabel(title, 8, LabModalHeader.contentW(MODAL_W, 8)));
        panel.addWidget(LabModalHeader.closeButton(LabModalHeader.closeX(MODAL_W), this::close));

        int listRight = LIST_X + LIST_W + LIST_GAP;
        int bodyH = MODAL_H - BODY_Y - BODY_PAD;
        int rightW = MODAL_W - listRight - BODY_PAD;

        WidgetGroup listContainer = new WidgetGroup(LIST_X, BODY_Y, LIST_W, bodyH);
        listContainer.setBackground(CONTAINER_TEXTURE);
        panel.addWidget(listContainer);

        WidgetGroup settingsContainer = new WidgetGroup(listRight, BODY_Y, rightW, bodyH);
        settingsContainer.setBackground(CONTAINER_TEXTURE);
        panel.addWidget(settingsContainer);

        this.settings = new LabLootPoolSettingsWidget(CONTAINER_PAD, CONTAINER_PAD,
                rightW - CONTAINER_PAD * 2, bodyH - CONTAINER_PAD * 2,
                I18n.get(LabGuiKeys.LAB_LOOT_DELETE),
                I18n.get(LabGuiKeys.LAB_LOOT_DONE));
        this.settings.setClientSideWidget();
        this.settings.applyPool(pool, lootType);
        this.settings.setOnDelete(() -> {
            onDelete.run();
            close();
        });
        this.settings.setOnDone(() -> {
            onApply.accept(settings.getPoolValues());
            close();
        });
        settingsContainer.addWidget(settings);

        LabLootEntryPanel entryPanel = new LabLootEntryPanel(settings, CONTAINER_PAD, CONTAINER_PAD,
                LIST_W - CONTAINER_PAD * 2, bodyH - CONTAINER_PAD * 2);
        entryPanel.setEntryContextHandler((index, mx, my) -> openEntryContext(index, mx, my));
        entryPanel.setEmptyContextHandler((mx, my) -> openEmptyContext(mx, my));
        listContainer.addWidget(entryPanel);
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public boolean offerPick(LabPick pick) {
        if (!layer.isVisible()) {
            return false;
        }
        return settings.offerPick(pick);
    }

    public void clearPendingPick() {
        settings.clearPendingPick();
    }

    public void selectEntry(int index) {
        settings.selectEntry(index);
    }

    private void openEntryContext(int index, double mx, double my) {
        settings.selectEntry(index);
        List<LabContextAction> actions = new ArrayList<>();
        actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_LOOT_ADD_ENTRY), "add", LabActionTone.PRIMARY,
                settings::addEntry));
        if (settings.entryCount() > 1) {
            int target = index;
            actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_LOOT_DELETE), "delete", LabActionTone.DANGER,
                    () -> settings.removeEntryAt(target)));
        }
        openContext(actions, mx, my);
    }

    private void openEmptyContext(double mx, double my) {
        openContext(List.of(
                new LabContextAction(I18n.get(LabGuiKeys.LAB_LOOT_ADD_ENTRY), "add", LabActionTone.PRIMARY,
                        settings::addEntry)),
                mx, my);
    }

    private void openContext(List<LabContextAction> actions, double mx, double my) {
        closeContext();
        int menuW = LabContextMenuPanel.menuWidth(actions);
        int menuH = LabContextMenuPanel.menuHeight(actions);
        int relX = (int) Math.round(mx - panel.getPositionX());
        int relY = (int) Math.round(my - panel.getPositionY());
        int menuX = Math.max(4, Math.min(relX, MODAL_W - menuW - 4));
        int menuY = Math.max(4, Math.min(relY, MODAL_H - menuH - 4));
        contextDismiss = new ButtonWidget(0, 0, MODAL_W, MODAL_H, IGuiTexture.EMPTY,
                cd -> closeContext());
        contextDismiss.setClientSideWidget();
        panel.addWidget(contextDismiss);
        contextMenuMs = System.currentTimeMillis();
        contextMenu = LabContextMenuAnimation.wrap(
                LabContextMenuPanel.build(menuX, menuY, actions, this::closeContext),
                () -> contextMenuMs);
        panel.addWidget(contextMenu);
    }

    private void closeContext() {
        if (contextDismiss != null) {
            panel.removeWidget(contextDismiss);
            contextDismiss = null;
        }
        if (contextMenu != null) {
            panel.removeWidget(contextMenu);
            contextMenu = null;
        }
    }

    private void close() {
        closeContext();
        layer.setVisible(false);
        if (onClose != null) {
            onClose.run();
        }
    }

    public static LabLootPoolModal open(WidgetGroup layer, String title, LabLootPoolValues pool, String lootType,
            Runnable onDelete, Consumer<LabLootPoolValues> onApply) {
        return new LabLootPoolModal(layer, title, pool, lootType, onDelete, onApply);
    }
}
