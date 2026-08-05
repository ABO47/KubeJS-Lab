package com.abo47.kubejslab.client.ui.recipes;
import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabToggleSwitchWidget;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class LabRecipeSettingsWidget extends WidgetGroup {
    private static final IGuiTexture CARD_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);

    private final TextTexture shapelessLabel;
    private final LabToggleSwitchWidget shapelessToggle;
    private final LabActionButton clearButton;
    private final LabActionButton saveButton;

    private boolean shapeless;
    private boolean shapelessSupported = true;
    private Runnable onClear;
    private Runnable onSave;

    public LabRecipeSettingsWidget(int x, int y, int w, int h) {
        super(x, y, w, h);

        int pad = LabLayout.SETTINGS_PAD;
        int cardX = pad;
        int cardY = 0;
        int cardW = w - pad * 2;
        int toggleW = LabToggleSwitchWidget.DEFAULT_WIDTH;
        int toggleX = cardX + cardW - pad - toggleW;
        int toggleY = (LabLayout.CARD_H - LabToggleSwitchWidget.DEFAULT_HEIGHT) / 2;

        shapelessLabel = new TextTexture(Component.translatable(LabGuiKeys.LAB_RECIPE_SHAPELESS).getString(),
                LabColors.TEXT_PRIMARY)
                .setWidth(cardW - pad * 2 - toggleW - 4)
                .setType(TextTexture.TextType.LEFT_HIDE);

        shapelessToggle = new LabToggleSwitchWidget(
                toggleX, toggleY,
                () -> shapeless,
                value -> shapeless = value,
                null);
        addWidget(shapelessToggle);

        int btnH = LabLayout.SETTINGS_BTN_H;
        int bottomY = h - pad - btnH;
        int btnW = (cardW - LabLayout.SETTINGS_BTN_GAP) / 2;

        clearButton = new LabActionButton(cardX, bottomY, btnW, btnH,
                Component.translatable(LabGuiKeys.LAB_RECIPE_CLEAR).getString(), () -> {
            if (onClear != null) onClear.run();
        });
        addWidget(clearButton);

        saveButton = new LabActionButton(cardX + btnW + LabLayout.SETTINGS_BTN_GAP, bottomY, btnW, btnH,
                Component.translatable(LabGuiKeys.LAB_RECIPE_SAVE).getString(), () -> {
            if (onSave != null) onSave.run();
        });
        addWidget(saveButton);
    }

    public boolean isShapeless() {
        return shapeless;
    }

    public void setShapelessSupported(boolean shapelessSupported) {
        this.shapelessSupported = shapelessSupported;
        if (!shapelessSupported) {
            shapeless = false;
        }
        shapelessToggle.setVisible(shapelessSupported);
    }

    public void setOnClear(Runnable onClear) {
        this.onClear = onClear;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int pad = LabLayout.SETTINGS_PAD;
        int cardX = x + pad;
        int cardY = y;
        int cardW = w - pad * 2;
        if (shapelessSupported) {
            CARD_TEXTURE.draw(g, mx, my, cardX, cardY, cardW, LabLayout.CARD_H);
            shapelessLabel.draw(g, mx, my, cardX + pad, cardY, cardW - pad * 2 - LabToggleSwitchWidget.DEFAULT_WIDTH - 4, LabLayout.CARD_H);
        }
        super.drawInBackground(g, mx, my, pt);
    }
}