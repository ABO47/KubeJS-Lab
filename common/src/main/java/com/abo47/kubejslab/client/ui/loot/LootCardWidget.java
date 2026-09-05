package com.abo47.kubejslab.client.ui.loot;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.widgets.EntryCardWidget;
import com.abo47.kubejslab.loot.model.LootStatus;
import com.abo47.kubejslab.loot.runtime.LootService;


public final class LootCardWidget extends EntryCardWidget {
    private static final IGuiTexture MODIFIED_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_PANEL_ALT, UiColors.WARNING);
    private static final IGuiTexture DISABLED_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_PANEL_ALT, UiColors.ERROR);
    private static final int ENTITY_ICON = 22;

    private final ResourceLocation entityId;
    private final boolean isEntity;
    private LootStatus status = LootStatus.NORMAL;

    public LootCardWidget(int x, int y, int w, int h, LootIndex.LootEntry entry,
            Runnable onClick, CardRightClick onRightClick) {
        super(x, y, w, h, iconFor(entry), entry.name(), entry.id().toString(), onClick, onRightClick);
        this.isEntity = LootService.LOOT_TYPE_ENTITY.equals(entry.lootType());
        this.entityId = isEntity ? entry.id() : null;
    }

    private static ItemStack iconFor(LootIndex.LootEntry entry) {
        if (LootService.LOOT_TYPE_BLOCK.equals(entry.lootType())) {
            Block block = BuiltInRegistries.BLOCK.get(entry.id());
            if (block != null) {
                return new ItemStack(block);
            }
        }
        if (LootService.LOOT_TYPE_FISHING.equals(entry.lootType())) {
            return new ItemStack(Items.FISHING_ROD);
        }
        if (LootService.LOOT_TYPE_GIFT.equals(entry.lootType())) {
            return new ItemStack(Items.EMERALD);
        }
        return new ItemStack(Items.CHEST);
    }

    public void setStatus(LootStatus status) {
        this.status = status;
    }

    @Override
    protected void drawIcon(GuiGraphics g, int mx, int my) {
        if (isEntity && entityId != null) {
            int x = getPositionX() + 2;
            int y = getPositionY() + (getSizeHeight() - ENTITY_ICON) / 2;
            if (!LootPreviewWidget.renderEntity(g, x + ENTITY_ICON / 2, y + ENTITY_ICON / 2, ENTITY_ICON, ENTITY_ICON,
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
    }
}
