package com.abo47.kubejslab.client.ui.loot;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabEntryCardWidget;
import com.abo47.kubejslab.client.ui.base.LabIconAtlas;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.loot.model.LabLootStatus;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootCardWidget extends LabEntryCardWidget {
    private static final IGuiTexture MODIFIED_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.WARNING);
    private static final IGuiTexture DISABLED_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.ERROR);
    private static final int BADGE_X = 16;
    private static final int BADGE_Y = 3;
    private static final int BADGE_SIZE = 9;
    private static final int ENTITY_ICON = 22;

    private final ResourceTexture pendingIcon =
            LabIconAtlas.iconTexture("repeat", LabActionTone.NEUTRAL);
    private final ResourceLocation entityId;
    private final boolean isEntity;
    private LabLootStatus status = LabLootStatus.NORMAL;
    private boolean pending;

    public LabLootCardWidget(int x, int y, int w, int h, LabLootIndex.LabLootEntry entry,
            Runnable onClick, CardRightClick onRightClick) {
        super(x, y, w, h, iconFor(entry), entry.name(), entry.id().toString(), onClick, onRightClick);
        this.isEntity = LabLootService.LOOT_TYPE_ENTITY.equals(entry.lootType());
        this.entityId = isEntity ? entry.id() : null;
    }

    private static ItemStack iconFor(LabLootIndex.LabLootEntry entry) {
        if (LabLootService.LOOT_TYPE_BLOCK.equals(entry.lootType())) {
            Block block = BuiltInRegistries.BLOCK.get(entry.id());
            if (block != null) {
                return new ItemStack(block);
            }
        }
        if (LabLootService.LOOT_TYPE_FISHING.equals(entry.lootType())) {
            return new ItemStack(Items.FISHING_ROD);
        }
        if (LabLootService.LOOT_TYPE_GIFT.equals(entry.lootType())) {
            return new ItemStack(Items.EMERALD);
        }
        return new ItemStack(Items.CHEST);
    }

    public void setStatus(LabLootStatus status) {
        this.status = status;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    @Override
    protected void drawIcon(GuiGraphics g, int mx, int my) {
        if (isEntity && entityId != null) {
            int x = getPositionX() + 2;
            int y = getPositionY() + (getSizeHeight() - ENTITY_ICON) / 2;
            if (!LabLootPreviewWidget.renderEntity(g, x + ENTITY_ICON / 2, y + ENTITY_ICON / 2, ENTITY_ICON, ENTITY_ICON,
                    entityId)) {
                super.drawIcon(g, mx, my);
            }
            return;
        }
        super.drawIcon(g, mx, my);
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
