package com.abo47.kubejslab.client.ui.picker;

import java.util.function.Consumer;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.client.ui.base.LabGlow;


public final class LabPickTile extends Widget {
    private static final int TILE = 18;

    private final LabPick pick;
    private final List<ItemStackTexture> previews;
    private final FluidStack fluid;
    private final Consumer<LabPick> onPick;

    private LabPickTile(LabPick pick, List<ItemStackTexture> previews, FluidStack fluid, List<Component> tooltip,
            Consumer<LabPick> onPick) {
        super(0, 0, TILE, TILE);
        this.pick = pick;
        this.previews = previews;
        this.fluid = fluid;
        this.onPick = onPick;
        setClientSideWidget();
        setHoverTooltips(tooltip);
    }

    public static LabPickTile item(LabPick pick, Consumer<LabPick> onPick) {
        ItemStack stack = ((LabPick.Item) pick).stack();
        return new LabPickTile(pick, List.of(new ItemStackTexture(stack)), null,
                List.of(Component.literal(stack.getHoverName().getString())), onPick);
    }

    public static LabPickTile tag(LabPick pick, Consumer<LabPick> onPick) {
        net.minecraft.resources.ResourceLocation tag = ((LabPick.Tag) pick).tag();
        List<ItemStackTexture> previews = LabPickerEntries.tagPreviews(tag).stream()
                .map(ItemStackTexture::new).toList();
        List<Component> tooltip = previews.isEmpty() ? List.of(Component.literal("#" + tag))
                : List.of(Component.literal("#" + tag),
                        Component.literal(previews.get(0).items[0].getHoverName().getString()));
        return new LabPickTile(pick, previews, null, tooltip, onPick);
    }

    public static LabPickTile fluid(LabPick pick, Consumer<LabPick> onPick) {
        FluidStack fluid = ((LabPick.Fluid) pick).fluid();
        return new LabPickTile(pick, List.of(), fluid,
                List.of(fluid.getDisplayName()), onPick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            onPick.accept(pick);
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX();
        int y = getPositionY();
        SlotWidget.ITEM_SLOT_TEXTURE.draw(graphics, mouseX, mouseY, x, y, TILE, TILE);
        if (!previews.isEmpty()) {
            int index = (int) ((Minecraft.getInstance().level.getGameTime() / 8) % previews.size());
            previews.get(index).draw(graphics, mouseX, mouseY, x + 1, y + 1, TILE - 2, TILE - 2);
        } else if (fluid != null && !fluid.isEmpty()) {
            DrawerHelper.drawFluidForGui(graphics, fluid, Math.max(fluid.getAmount(), 1000), x + 1, y + 1, TILE - 2,
                    TILE - 2);
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            LabGlow.drawGlow(graphics, mouseX, mouseY, x, y, TILE, TILE);
        }
    }
}
