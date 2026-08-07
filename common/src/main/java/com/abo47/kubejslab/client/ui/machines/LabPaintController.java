package com.abo47.kubejslab.client.ui.machines;

import java.util.function.Supplier;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.picker.LabPick;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


final class LabPaintController {
    private final Supplier<ModularUIContainer> container;
    private final Runnable onChanged;
    private int paintButton = -1;
    private final Set<LabSlotData> paintedSlots = new HashSet<>();
    private LabPick pendingPick;

    LabPaintController(Supplier<ModularUIContainer> container, Runnable onChanged) {
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

    void paintSlot(int button, LabSlotData data) {
        ModularUIContainer source = container.get();
        if (source == null) {
            return;
        }
        if (!paintedSlots.add(data)) {
            return;
        }
        ItemStack carried = source.getCarried();
        if (button == LabColors.MOUSE_BUTTON_RIGHT && carried.isEmpty()) {
            data.clear();
            return;
        }
        if (pendingPick != null) {
            applyPick(data, pendingPick);
            return;
        }
        if (carried.isEmpty()) {
            return;
        }
        if (data.kind == LabSlotKind.FLUID) {
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
            data.setItemValue(carried.copyWithCount(1));
        } else if (button == LabColors.MOUSE_BUTTON_RIGHT) {
            int next = Math.max(1, current.getCount() - 1);
            data.setItemValue(current.copyWithCount(next));
        } else {
            data.setItemValue(current.copyWithCount(Math.min(current.getCount() + 1, current.getMaxStackSize())));
        }
    }

    void adjustStackCount(LabSlotData data, int delta) {
        if (data.kind == LabSlotKind.FLUID) {
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

    void setPendingPick(LabPick pendingPick) {
        this.pendingPick = pendingPick;
    }

    private static void applyPick(LabSlotData data, LabPick pick) {
        if (pick instanceof LabPick.Item item) {
            if (data.kind != LabSlotKind.FLUID) {
                data.setItemValue(item.stack().copyWithCount(1));
            }
            return;
        }
        if (pick instanceof LabPick.Tag tag) {
            if (data.kind != LabSlotKind.FLUID) {
                data.setTagValue(tag.tag());
            }
            return;
        }
        if (pick instanceof LabPick.Fluid fluid) {
            if (data.kind == LabSlotKind.FLUID) {
                data.setFluidValue(fluid.fluid());
            }
        }
    }
}