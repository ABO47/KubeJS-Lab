package com.abo47.kubejslab.client.ui.blocks;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;


public final class LabBlockPreviewWidget extends WidgetGroup {
    private String type = "basic";
    private ItemStack stack = ItemStack.EMPTY;
    private ResourceTexture textureTex;

    public LabBlockPreviewWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setBlock(String type, ItemStack stack) {
        this.type = type == null || type.isBlank() ? "basic" : type;
        this.stack = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    public void setTexture(String relativePath) {
        textureTex = null;
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryBuild("kubejs", "textures/" + relativePath);
        if (id != null && Minecraft.getInstance().getResourceManager().getResource(id).isPresent()) {
            textureTex = new ResourceTexture(id);
        }
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int px = getPositionX();
        int py = getPositionY();
        int pw = getSizeWidth();
        int ph = getSizeHeight();

        int size = Math.max(32, Math.min(pw, ph) - 16);
        int iconX = px + (pw - size) / 2;
        int iconY = py + (ph - size) / 2;
        if (textureTex != null) {
            textureTex.draw(graphics, mouseX, mouseY, iconX, iconY, size, size);
            return;
        }
        ItemStack shown = stack.isEmpty() ? new ItemStack(typeIcon(type)) : stack;
        new ItemStackTexture(shown)
                .draw(graphics, mouseX, mouseY, iconX, iconY, size, size);
    }

    private static Item typeIcon(String type) {
        return switch (type) {
            case "slab" -> Items.OAK_SLAB;
            case "stairs" -> Items.OAK_STAIRS;
            case "fence" -> Items.OAK_FENCE;
            case "wall" -> Items.COBBLESTONE_WALL;
            case "fence_gate" -> Items.OAK_FENCE_GATE;
            case "pressure_plate" -> Items.OAK_PRESSURE_PLATE;
            case "button" -> Items.OAK_BUTTON;
            case "carpet" -> Items.WHITE_CARPET;
            case "falling" -> Items.SAND;
            case "crop" -> Items.WHEAT;
            case "detector" -> Items.DAYLIGHT_DETECTOR;
            case "cardinal" -> Items.FURNACE;
            default -> Items.BRICKS;
        };
    }
}
