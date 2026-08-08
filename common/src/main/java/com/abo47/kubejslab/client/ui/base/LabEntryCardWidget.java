package com.abo47.kubejslab.client.ui.base;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;


public class LabEntryCardWidget extends Widget {
    protected static final IGuiTexture CARD_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);
    protected static final IGuiTexture SELECTED_FILL =
            new ColorRectTexture(LabColors.withAlpha(LabColors.INTERACTIVE, 30));
    protected static final int ICON_X = 4;
    protected static final int ICON_SIZE = 16;
    protected static final int TEXT_GAP = 4;
    protected static final int TEXT_Y = 3;
    protected static final int TEXT_LINE_H = 9;
    protected static final int ID_Y = 14;
    protected static final int ID_LINE_H = 8;

    private final ItemStackTexture iconTex;
    private final TextTexture nameTex;
    private final TextTexture idTex;
    private final int textW;
    private final int textX;
    private final Runnable onClick;
    private final CardRightClick onRightClick;
    private IGuiTexture cardTexture = CARD_TEXTURE;
    private boolean selected;

    public LabEntryCardWidget(int x, int y, int w, int h, ItemStack icon, String name, String idLine,
            Runnable onClick, CardRightClick onRightClick) {
        super(x, y, w, h);
        this.iconTex = new ItemStackTexture(icon);
        this.textX = ICON_X + ICON_SIZE + TEXT_GAP;
        this.textW = w - textX;
        this.nameTex = new TextTexture(name, LabColors.TEXT_PRIMARY)
                .setWidth(textW)
                .setType(TextTexture.TextType.LEFT_HIDE);
        this.idTex = new TextTexture(idLine, LabColors.TEXT_MUTED)
                .setWidth(textW)
                .setType(TextTexture.TextType.LEFT_HIDE);
        this.onClick = onClick;
        this.onRightClick = onRightClick;
    }

    protected void setCardTexture(IGuiTexture cardTexture) {
        this.cardTexture = cardTexture;
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

        cardTexture.draw(g, mx, my, x, y, w, h);

        if (selected) {
            SELECTED_FILL.draw(g, mx, my, x + 1, y + 1, w - 2, h - 2);
        }

        if (isMouseOverElement(mx, my)) {
            LabGlow.drawGlow(g, mx, my, x, y, w, h);
        }

        drawIcon(g, mx, my);
        nameTex.draw(g, mx, my, x + textX, y + TEXT_Y, textW, TEXT_LINE_H);
        idTex.draw(g, mx, my, x + textX, y + ID_Y, textW, ID_LINE_H);
    }

    protected void drawIcon(GuiGraphics g, int mx, int my) {
        iconTex.draw(g, mx, my, getPositionX() + ICON_X, getPositionY() + (getSizeHeight() - ICON_SIZE) / 2,
                ICON_SIZE, ICON_SIZE);
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