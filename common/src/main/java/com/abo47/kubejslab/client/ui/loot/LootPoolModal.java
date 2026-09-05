package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.ContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuAnimation;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuPanel;
import com.abo47.kubejslab.client.ui.picker.Pick;
import com.abo47.kubejslab.client.ui.theme.ModalHeader;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.loot.model.LootPoolValues;


public final class LootPoolModal {
    public static final int MODAL_W = 432;
    public static final int MODAL_H = 260;
    private static final int BODY_Y = 22;
    private static final int BODY_PAD = 8;
    private static final int LIST_X = 8;
    private static final int LIST_W = 150;
    private static final int LIST_GAP = 4;
    private static final int CONTAINER_PAD = 4;

    private static final int DIM_COLOR = UiColors.withAlpha(UiColors.SURFACE_BASE, 140);
    private static final IGuiTexture CONTAINER_TEXTURE = UiColors.bordered(
            UiColors.withAlpha(UiColors.SURFACE_PANEL_ALT, 120), UiColors.BORDER_BASE);

    private final WidgetGroup layer;
    private final WidgetGroup panel;
    private final LootPoolSettingsWidget settings;
    private Runnable onClose;
    private WidgetGroup contextMenu;
    private ButtonWidget contextDismiss;
    private long contextMenuMs;

    private LootPoolModal(WidgetGroup layer, String title, LootPoolValues pool, String lootType,
            Runnable onDelete, Consumer<LootPoolValues> onApply) {
        this.layer = layer;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, UiLayout.ROOT_W, UiLayout.ROOT_H,
                new ColorRectTexture(DIM_COLOR), cd -> {
                }).setClientSideWidget());

        this.panel = new WidgetGroup((UiLayout.ROOT_W - MODAL_W) / 2,
                (UiLayout.ROOT_H - MODAL_H) / 2, MODAL_W, MODAL_H) {
            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                super.mouseClicked(mx, my, button);
                return isMouseOverElement(mx, my);
            }
        };
        this.panel.setBackground(UiColors.bordered(
                UiColors.withAlpha(UiColors.SURFACE_BASE, 252), UiColors.BORDER_ACCENT));
        layer.addWidget(panel);
        panel.addWidget(ModalHeader.titleLabel(title, 8, ModalHeader.contentW(MODAL_W, 8)));
        panel.addWidget(ModalHeader.closeButton(ModalHeader.closeX(MODAL_W), this::close));

        int listRight = LIST_X + LIST_W + LIST_GAP;
        int bodyH = MODAL_H - BODY_Y - BODY_PAD;
        int rightW = MODAL_W - listRight - BODY_PAD;

        WidgetGroup listContainer = new WidgetGroup(LIST_X, BODY_Y, LIST_W, bodyH);
        listContainer.setBackground(CONTAINER_TEXTURE);
        panel.addWidget(listContainer);

        WidgetGroup settingsContainer = new WidgetGroup(listRight, BODY_Y, rightW, bodyH);
        settingsContainer.setBackground(CONTAINER_TEXTURE);
        panel.addWidget(settingsContainer);

        this.settings = new LootPoolSettingsWidget(CONTAINER_PAD, CONTAINER_PAD,
                rightW - CONTAINER_PAD * 2, bodyH - CONTAINER_PAD * 2,
                I18n.get(LootKeys.LOOT_DELETE),
                I18n.get(LootKeys.LOOT_DONE));
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

        LootEntryPanel entryPanel = new LootEntryPanel(settings, CONTAINER_PAD, CONTAINER_PAD,
                LIST_W - CONTAINER_PAD * 2, bodyH - CONTAINER_PAD * 2);
        entryPanel.setEntryContextHandler((index, mx, my) -> openEntryContext(index, mx, my));
        entryPanel.setEmptyContextHandler((mx, my) -> openEmptyContext(mx, my));
        listContainer.addWidget(entryPanel);
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public boolean offerPick(Pick pick) {
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
        List<ContextAction> actions = new ArrayList<>();
        actions.add(new ContextAction(I18n.get(LootKeys.LOOT_ADD_ENTRY), "add", ActionTone.PRIMARY,
                settings::addEntry));
        if (settings.entryCount() > 1) {
            int target = index;
            actions.add(new ContextAction(I18n.get(LootKeys.LOOT_DELETE), "delete", ActionTone.DANGER,
                    () -> settings.removeEntryAt(target)));
        }
        openContext(actions, mx, my);
    }

    private void openEmptyContext(double mx, double my) {
        openContext(List.of(
                new ContextAction(I18n.get(LootKeys.LOOT_ADD_ENTRY), "add", ActionTone.PRIMARY,
                        settings::addEntry)),
                mx, my);
    }

    private void openContext(List<ContextAction> actions, double mx, double my) {
        closeContext();
        int menuW = ContextMenuPanel.menuWidth(actions);
        int menuH = ContextMenuPanel.menuHeight(actions);
        int relX = (int) Math.round(mx - panel.getPositionX());
        int relY = (int) Math.round(my - panel.getPositionY());
        int menuX = Math.max(4, Math.min(relX, MODAL_W - menuW - 4));
        int menuY = Math.max(4, Math.min(relY, MODAL_H - menuH - 4));
        contextDismiss = new ButtonWidget(0, 0, MODAL_W, MODAL_H, IGuiTexture.EMPTY,
                cd -> closeContext());
        contextDismiss.setClientSideWidget();
        panel.addWidget(contextDismiss);
        contextMenuMs = System.currentTimeMillis();
        contextMenu = ContextMenuAnimation.wrap(
                ContextMenuPanel.build(menuX, menuY, actions, this::closeContext),
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

    public static LootPoolModal open(WidgetGroup layer, String title, LootPoolValues pool, String lootType,
            Runnable onDelete, Consumer<LootPoolValues> onApply) {
        return new LootPoolModal(layer, title, pool, lootType, onDelete, onApply);
    }
}
