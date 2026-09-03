package com.abo47.kubejslab.client.ui.loot;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabIconAtlas;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.picker.LabPick;
import com.abo47.kubejslab.loot.model.LabLootPoolValues;


public final class LabLootPoolModal {
    public static final int MODAL_W = 432;
    public static final int MODAL_H = 260;
    private static final int BODY_Y = 22;
    private static final int BODY_PAD = 8;

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
        panel.addWidget(titleLabel(title));
        addHeaderClose(MODAL_W - 24, 3);

        this.settings = new LabLootPoolSettingsWidget(BODY_PAD, BODY_Y, MODAL_W - BODY_PAD * 2,
                MODAL_H - BODY_Y - BODY_PAD,
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
    }

    private WidgetGroup titleLabel(String title) {
        return new WidgetGroup(8, 6, MODAL_W - 50, 9) {
            private final TextTexture tex = new TextTexture(title, LabColors.TEXT_PRIMARY)
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(MODAL_W - 50);

            @Override
            public void drawInBackground(GuiGraphics g, int mx, int my, float pt) {
                tex.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        };
    }

    private void addHeaderClose(int x, int y) {
        ResourceTexture icon = LabIconAtlas.iconTexture("close", LabColors.ERROR);
        IGuiTexture face = new IGuiTexture() {
            @Override
            public void draw(GuiGraphics g, int mx, int my, float x0, float y0, int w0, int h0) {
                LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE)
                        .draw(g, mx, my, x0, y0, w0, h0);
                icon.draw(g, mx, my, x0 + 2, y0 + 2, w0 - 4, h0 - 4);
            }
        };
        ButtonWidget button = new ButtonWidget(x, y, 16, 16, face, cd -> close());
        button.setClientSideWidget();
        button.setHoverTexture((g, mx, my, x0, y0, w0, h0) ->
                LabGlow.drawGlow(g, mx, my, (int) x0, (int) y0, (int) w0, (int) h0));
        panel.addWidget(button);
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public boolean consumePick(LabPick pick) {
        if (!layer.isVisible()) {
            return false;
        }
        return settings.consumePick(pick);
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
