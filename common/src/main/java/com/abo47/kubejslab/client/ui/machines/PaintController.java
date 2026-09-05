package com.abo47.kubejslab.client.ui.machines;

import java.util.function.Supplier;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import com.abo47.kubejslab.client.ui.picker.Pick;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.recipe.model.SlotKind;


final class PaintController {
    private final Supplier<ModularUIContainer> container;
    private final Runnable onChanged;
    private int paintButton = -1;
    private final Set<SlotData> paintedSlots = new HashSet<>();
    private Pick pendingPick;

    PaintController(Supplier<ModularUIContainer> container, Runnable onChanged) {
        this.container = container;
        this.onChanged = onChanged;
    }

    void beginPaint(int button) {
        paintButton = button;
        paintedSlots.clear();
    }

    boolean isPainting(int button) {
        return paintButton == button;
    }

    void endPaint() {
        paintButton = -1;
        paintedSlots.clear();
        onChanged.run();
    }

    void paintSlot(int button, SlotData data) {
        ModularUIContainer source = container.get();
        if (source == null) {
            return;
        }
        if (!paintedSlots.add(data)) {
            return;
        }
        ItemStack carried = source.getCarried();
        if (button == UiColors.MOUSE_BUTTON_RIGHT && carried.isEmpty()) {
            data.clear();
            return;
        }
        if (pendingPick != null) {
            applyPick(data, pendingPick);
            pendingPick = null;
            return;
        }
        if (carried.isEmpty()) {
            return;
        }
        if (data.kind == SlotKind.FLUID) {
            IFluidTransfer transfer = FluidTransferHelper.getFluidTransfer(carried);
            if (transfer != null && transfer.getTanks() > 0) {
                FluidStack content = transfer.getFluidInTank(0);
                if (!content.isEmpty()) {
                    data.setFluidValue(content.copy(1000));
                }
            }
            return;
        }
        ItemStack current = data.stack;
        if (current.isEmpty() || !ItemStack.isSameItem(current, carried)) {
            data.setItemValue(carried.copy());
        } else {
            data.setItemValue(current.copyWithCount(Math.min(current.getCount() + 1, current.getMaxStackSize())));
        }
    }

    void adjustStackCount(SlotData data, int delta) {
        if (data.kind == SlotKind.FLUID) {
            data.setFluidValue(data.fluid.copy(Math.max(1, data.fluid.getAmount() + delta * 1000)));
            onChanged.run();
            return;
        }
        if (data.stack.isEmpty()) {
            return;
        }
        int count = Math.max(1, Math.min(data.stack.getMaxStackSize(), data.stack.getCount() + delta));
        data.setItemValue(data.stack.copyWithCount(count));
        onChanged.run();
    }

    void setPendingPick(Pick pendingPick) {
        this.pendingPick = pendingPick;
    }

    void clearPendingPick() {
        pendingPick = null;
    }

    private static void applyPick(SlotData data, Pick pick) {
        if (pick instanceof Pick.Item item) {
            if (data.kind != SlotKind.FLUID) {
                data.setItemValue(item.stack().copy());
            }
            return;
        }
        if (pick instanceof Pick.Tag tag) {
            if (data.kind != SlotKind.FLUID) {
                data.setTagValue(tag.tag());
            }
            return;
        }
        if (pick instanceof Pick.Fluid fluid) {
            if (data.kind == SlotKind.FLUID) {
                data.setFluidValue(fluid.fluid());
            }
        }
    }
}