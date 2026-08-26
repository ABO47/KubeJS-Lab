package com.abo47.kubejslab.client.ui.recipes;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabEntryCardWidget;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;


public final class LabRecipeCardWidget extends LabEntryCardWidget {
    private static final IGuiTexture MODIFIED_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.WARNING);
    private static final IGuiTexture DISABLED_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.ERROR);
    private static final int ICON_X = 4;
    private static final int ICON_SIZE = 16;

    private final FluidStack fluidOutput;

    public LabRecipeCardWidget(int x, int y, int w, int h, LabRecipeIndex.LabRecipeEntry entry,
            Runnable onClick, CardRightClick onRightClick) {
        super(x, y, w, h, entry.output(), entry.name(), entry.id().toString(), onClick, onRightClick);
        this.fluidOutput = entry.fluidOutput();
    }

    public void setStatus(LabRecipeStatus status) {
        setCardTexture(switch (status) {
            case MODIFIED -> MODIFIED_TEXTURE;
            case DISABLED -> DISABLED_TEXTURE;
            case NORMAL -> CARD_TEXTURE;
        });
    }

    @Override
    protected void drawIcon(GuiGraphics g, int mx, int my) {
        if (fluidOutput != null && !fluidOutput.isEmpty()) {
            DrawerHelper.drawFluidForGui(g, fluidOutput, Math.max(fluidOutput.getAmount(), 1000),
                    getPositionX() + ICON_X, getPositionY() + (getSizeHeight() - ICON_SIZE) / 2,
                    ICON_SIZE, ICON_SIZE);
            return;
        }
        super.drawIcon(g, mx, my);
    }
}