package com.abo47.kubejslab.client.ui.picker;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.client.ui.theme.UiGlow;


public final class PickTile extends Widget {
    private static final int TILE = 18;

    private final Pick pick;
    private final List<ItemStackTexture> previews;
    private final FluidStack fluid;
    private final Consumer<Pick> onPick;

    private PickTile(Pick pick, List<ItemStackTexture> previews, FluidStack fluid, List<Component> tooltip,
            Consumer<Pick> onPick) {
        super(0, 0, TILE, TILE);
        this.pick = pick;
        this.previews = previews;
        this.fluid = fluid;
        this.onPick = onPick;
        setClientSideWidget();
        setHoverTooltips(tooltip);
    }

    public static PickTile item(Pick pick, Consumer<Pick> onPick) {
        ItemStack stack = ((Pick.Item) pick).stack();
        return new PickTile(pick, List.of(new ItemStackTexture(stack)), null, itemTooltip(stack), onPick);
    }

    static List<Component> itemTooltip(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        TooltipFlag flag = minecraft.options.advancedItemTooltips
                ? TooltipFlag.Default.ADVANCED
                : TooltipFlag.Default.NORMAL;
        List<Component> lines = new ArrayList<>(stack.getTooltipLines(minecraft.player, flag));
        String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        String modName = dev.architectury.platform.Platform.getOptionalMod(namespace)
                .map(dev.architectury.platform.Mod::getName).orElse(namespace);
        lines.add(Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        return List.copyOf(lines);
    }

    public static PickTile tag(Pick pick, Consumer<Pick> onPick) {
        net.minecraft.resources.ResourceLocation tag = ((Pick.Tag) pick).tag();
        List<ItemStackTexture> previews = PickerEntries.tagPreviews(tag).stream()
                .map(ItemStackTexture::new).toList();
        List<Component> tooltip = previews.isEmpty() ? List.of(Component.literal("#" + tag))
                : List.of(Component.literal("#" + tag + " (" + previews.size() + ")"),
                        Component.literal(previews.get(0).items[0].getHoverName().getString()));
        return new PickTile(pick, previews, null, tooltip, onPick);
    }

    public static PickTile fluid(Pick pick, Consumer<Pick> onPick) {
        FluidStack fluid = ((Pick.Fluid) pick).fluid();
        return new PickTile(pick, List.of(), fluid,
                List.of(fluid.getDisplayName(),
                        Component.literal(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString())),
                onPick);
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
            UiGlow.drawGlow(graphics, mouseX, mouseY, x, y, TILE, TILE);
        }
    }
}
