package com.abo47.kubejslab.client.ui.loot;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabModalHeader;
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

    private static final int DIM_COLOR = LabColors.withAlpha(LabColors.SURFACE_BASE, 140);

    private final WidgetGroup layer;
    private final WidgetGroup panel;
    private final LabLootPoolSettingsWidget settings;
    private Runnable onClose;

    private LabLootPoolModal(WidgetGroup layer, String title, LabLootPoolValues pool, String lootType,
            Runnable onDelete, Consumer<LabLootPoolValues> onApply) {
        this.layer = layer;

        layer.clearAllWidgets();
        layer.setVisible(true);
        layer.addWidget(new ButtonWidget(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H,
                new ColorRectTexture(DIM_COLOR), cd -> close()).setClientSideWidget());

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
        this.settings = new LabLootPoolSettingsWidget(listRight, BODY_Y, MODAL_W - listRight - BODY_PAD,
                bodyH,
                Component.translatable(LabGuiKeys.LAB_LOOT_DELETE).getString(),
                Component.translatable(LabGuiKeys.LAB_LOOT_DONE).getString());
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
        panel.addWidget(settings);

        panel.addWidget(new LabLootEntryPanel(settings, LIST_X, BODY_Y, LIST_W, bodyH));
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

    private void close() {
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
