package com.abo47.kubejslab.client.ui.items;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;


public final class LabItemPreviewWidget extends WidgetGroup {
    private static final int ICON_SIZE = 40;
    private static final IGuiTexture PANEL_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL_ALT, LabColors.BORDER_BASE);

    private String type = "basic";
    private String name = "";
    private String id = "kubejs:lab/";

    public LabItemPreviewWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setItem(String type, String name, String id) {
        this.type = type == null || type.isBlank() ? "basic" : type;
        this.name = name == null ? "" : name;
        this.id = id == null ? "" : id;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int px = getPositionX();
        int py = getPositionY();
        int pw = getSizeWidth();
        int ph = getSizeHeight();
        PANEL_TEXTURE.draw(graphics, mouseX, mouseY, px, py, pw, ph);

        int iconX = px + (pw - ICON_SIZE) / 2;
        int iconY = py + 14;
        new ItemStackTexture(new ItemStack(typeIcon(type)))
                .draw(graphics, mouseX, mouseY, iconX, iconY, ICON_SIZE, ICON_SIZE);

        int textY = iconY + ICON_SIZE + 12;
        String label = name.isBlank() ? type : name;
        new TextTexture(label)
                .setType(TextTexture.TextType.LEFT_HIDE)
                .setColor(LabColors.TEXT_PRIMARY)
                .draw(graphics, mouseX, mouseY, px + 8, py + textY, pw - 16, 11);
        new TextTexture(id)
                .setType(TextTexture.TextType.LEFT_HIDE)
                .setColor(LabColors.TEXT_MUTED)
                .draw(graphics, mouseX, mouseY, px + 8, py + textY + 15, pw - 16, 9);
    }

    private static Item typeIcon(String type) {
        return switch (type) {
            case "sword" -> Items.IRON_SWORD;
            case "pickaxe" -> Items.IRON_PICKAXE;
            case "axe" -> Items.IRON_AXE;
            case "shovel" -> Items.IRON_SHOVEL;
            case "hoe" -> Items.IRON_HOE;
            case "shears" -> Items.SHEARS;
            case "helmet" -> Items.IRON_HELMET;
            case "chestplate" -> Items.IRON_CHESTPLATE;
            case "leggings" -> Items.IRON_LEGGINGS;
            case "boots" -> Items.IRON_BOOTS;
            case "music_disc" -> Items.MUSIC_DISC_13;
            default -> Items.STICK;
        };
    }
}