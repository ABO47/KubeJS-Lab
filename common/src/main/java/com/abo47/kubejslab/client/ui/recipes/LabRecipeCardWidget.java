package com.abo47.kubejslab.client.ui.recipes;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;


public final class LabRecipeCardWidget extends Widget {
    private static final IGuiTexture CARD_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);
    private static final IGuiTexture MODIFIED_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.WARNING);
    private static final IGuiTexture DISABLED_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.ERROR);
    private static final IGuiTexture SELECTED_FILL =
            new com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture(
                    LabColors.withAlpha(LabColors.INTERACTIVE, 30));
    private static final int ICON_X = 4;
    private static final int ICON_SIZE = 16;
    private static final int TEXT_GAP = 4;
    private static final int TEXT_Y = 3;
    private static final int TEXT_LINE_H = 9;
    private static final int ID_Y = 14;
    private static final int ID_LINE_H = 8;

    private final LabRecipeIndex.LabRecipeEntry entry;
    private final ItemStackTexture iconTex;
    private final TextTexture nameTex;
    private final TextTexture idTex;
    private final int textW;
    private final int textX;
    private final Runnable onClick;
    private final CardRightClick onRightClick;
    private LabRecipeStatus status = LabRecipeStatus.NORMAL;
    private boolean selected;

    public LabRecipeCardWidget(int x, int y, int w, int h, LabRecipeIndex.LabRecipeEntry entry,
            Runnable onClick, CardRightClick onRightClick) {
        super(x, y, w, h);
        this.entry = entry;
        this.onClick = onClick;
        this.onRightClick = onRightClick;
        this.iconTex = new ItemStackTexture(entry.output());
        this.textX = ICON_X + ICON_SIZE + TEXT_GAP;
        this.textW = w - textX;
        this.nameTex = new TextTexture(entry.name(), LabColors.TEXT_PRIMARY)
                .setWidth(textW)
                .setType(TextTexture.TextType.LEFT_HIDE);
        this.idTex = new TextTexture(entry.id().toString(), LabColors.TEXT_MUTED)
                .setWidth(textW)
                .setType(TextTexture.TextType.LEFT_HIDE);
    }

    public void setStatus(LabRecipeStatus status) {
        this.status = status;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();

        IGuiTexture card = switch (status) {
            case MODIFIED -> MODIFIED_TEXTURE;
            case DISABLED -> DISABLED_TEXTURE;
            case NORMAL -> CARD_TEXTURE;
        };
        card.draw(g, mx, my, x, y, w, h);

        if (selected) {
            SELECTED_FILL.draw(g, mx, my, x + 1, y + 1, w - 2, h - 2);
        }

        if (isMouseOverElement(mx, my)) {
            LabGlow.drawGlow(g, mx, my, x, y, w, h);
        }

        iconTex.draw(g, mx, my, x + ICON_X, y + (h - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE);
        nameTex.draw(g, mx, my, x + textX, y + TEXT_Y, textW, TEXT_LINE_H);
        idTex.draw(g, mx, my, x + textX, y + ID_Y, textW, ID_LINE_H);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == LabColors.MOUSE_BUTTON_LEFT) {
            if (onClick != null) {
                onClick.run();
            }
            return true;
        }
        if (button == LabColors.MOUSE_BUTTON_RIGHT) {
            if (onRightClick != null) {
                onRightClick.onRightClick(mouseX, mouseY);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @FunctionalInterface
    public interface CardRightClick {
        void onRightClick(double mouseX, double mouseY);
    }
}
