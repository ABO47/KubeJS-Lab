package com.abo47.kubejslab.client.ui.items;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.theme.IconAtlas;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.widgets.EntryCardWidget;
import com.abo47.kubejslab.item.model.ItemStatus;


public final class ItemCardWidget extends EntryCardWidget {
    private static final IGuiTexture MODIFIED_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_PANEL_ALT, UiColors.WARNING);
    private static final IGuiTexture DISABLED_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_PANEL_ALT, UiColors.ERROR);
    private static final int BADGE_X = 16;
    private static final int BADGE_Y = 3;
    private static final int BADGE_SIZE = 9;

    private final ResourceTexture pendingIcon =
            IconAtlas.iconTexture("repeat", ActionTone.NEUTRAL);
    private ItemStatus status = ItemStatus.NORMAL;
    private boolean pending;

    public ItemCardWidget(int x, int y, int w, int h, ItemIndex.ItemEntry entry,
            Runnable onClick, CardRightClick onRightClick) {
        super(x, y, w, h, entry.stack(), entry.name(), entry.id().toString(), onClick, onRightClick);
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        setCardTexture(switch (status) {
            case MODIFIED -> MODIFIED_TEXTURE;
            case DISABLED -> DISABLED_TEXTURE;
            case CREATED, NORMAL -> CARD_TEXTURE;
        });
        super.drawInBackground(g, mx, my, pt);
        if (pending && pendingIcon != null) {
            pendingIcon.draw(g, mx, my, getPositionX() + getSizeWidth() - BADGE_X,
                    getPositionY() + BADGE_Y, BADGE_SIZE, BADGE_SIZE);
        }
    }
}