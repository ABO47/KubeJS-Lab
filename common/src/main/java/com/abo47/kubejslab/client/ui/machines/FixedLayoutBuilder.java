package com.abo47.kubejslab.client.ui.machines;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.client.ui.recipes.RecipeIndex;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.platform.Services;
import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;


final class FixedLayoutBuilder {
    private final MachineLayoutWidget widget;

    FixedLayoutBuilder(MachineLayoutWidget widget) {
        this.widget = widget;
    }

    void build(RecipeHandler support) {
        List<SlotDescriptor> inputDesc = new ArrayList<>(support.inputSlots());
        List<SlotDescriptor> outputDesc = new ArrayList<>(support.outputSlots());

        int maxInputCol = 0;
        int minOutputCol = Integer.MAX_VALUE;
        int maxOutputCol = 0;
        int maxRow = 0;
        for (SlotDescriptor d : inputDesc) {
            maxInputCol = Math.max(maxInputCol, d.col());
            maxRow = Math.max(maxRow, d.row());
        }
        for (SlotDescriptor d : outputDesc) {
            minOutputCol = Math.min(minOutputCol, d.col());
            maxOutputCol = Math.max(maxOutputCol, d.col());
            maxRow = Math.max(maxRow, d.row());
        }
        int outputBlockCols = minOutputCol == Integer.MAX_VALUE ? 0 : maxOutputCol - minOutputCol + 1;

        int gridH = maxRow + 1;
        int ox = 0;
        int outputOX = outputBlockCols == 0 ? ox : ox + (UiLayout.MACHINE_COLS - outputBlockCols) * 18;
        int oy = Math.max(UiLayout.MACHINE_PAD, (widget.getSizeHeight() - gridH * 18) / 2);

        RecipeIndex.RecipeEntry entry = widget.entry();
        IRecipeLayoutDrawable<?> jeiLayout = widget.jeiLayout();
        Recipe<?> original = widget.original();
        List<?> rollable = MachineLayoutWidget.rollableResults(original);
        List<Float> ieChances = support.outputChances(original);
        if (ieChances.isEmpty()) {
            ieChances = List.of();
        }

        List<FluidStack> fluidInputs = new ArrayList<>();
        List<FluidStack> fluidOutputs = new ArrayList<>();
        List<ItemStack> itemInputs = new ArrayList<>();
        List<ItemStack> itemOutputs = new ArrayList<>();
        if (entry != null && jeiLayout != null) {
            for (IRecipeSlotView view : jeiLayout.getRecipeSlotsView().getSlotViews()) {
                boolean fluidSlot = Services.platform().readFluidIngredient(view).isPresent();
                if (view.getRole() == RecipeIngredientRole.INPUT) {
                    if (fluidSlot) {
                        fluidInputs.add(onlyFluid(view));
                    } else {
                        itemInputs.add(onlyItem(view));
                    }
                } else if (view.getRole() == RecipeIngredientRole.OUTPUT) {
                    if (fluidSlot) {
                        fluidOutputs.add(onlyFluid(view));
                    } else {
                        itemOutputs.add(onlyItem(view));
                    }
                }
            }
        }

        int itemInputIndex = 0;
        int fluidInputIndex = 0;
        for (SlotDescriptor d : inputDesc) {
            SlotData data = d.kind() == SlotKind.FLUID ? new SlotData(true) : new SlotData();
            if (d.kind() == SlotKind.FLUID) {
                if (fluidInputIndex < fluidInputs.size()) {
                    data.setFluidValue(fluidInputs.get(fluidInputIndex++));
                }
            } else {
                if (itemInputIndex < itemInputs.size()) {
                    data.setItemValue(itemInputs.get(itemInputIndex));
                    widget.applyIngredientKind(data, original, itemInputIndex, true);
                    itemInputIndex++;
                }
            }
            addFixedSlot(d, data, ox + d.col() * 18, oy + d.row() * 18);
        }

        int itemOutputIndex = 0;
        int fluidOutputIndex = 0;
        for (SlotDescriptor d : outputDesc) {
            SlotData data = d.kind() == SlotKind.FLUID ? new SlotData(true) : new SlotData();
            if (d.kind() == SlotKind.FLUID) {
                if (fluidOutputIndex < fluidOutputs.size()) {
                    data.setFluidValue(fluidOutputs.get(fluidOutputIndex++));
                }
            } else {
                if (itemOutputIndex < itemOutputs.size()) {
                    data.setItemValue(itemOutputs.get(itemOutputIndex));
                    widget.applyChance(data, rollable, ieChances, itemOutputIndex);
                    itemOutputIndex++;
                }
            }
            addFixedSlot(d, data, outputOX + (d.col() - minOutputCol) * 18, oy + d.row() * 18);
        }
        widget.notifyOutputsChanged();
    }

    private void addFixedSlot(SlotDescriptor d, SlotData data, int x, int y) {
        Widget slot = createDescriptorWidget(d, data);
        slot.setSelfPosition(x, y);
        widget.addWidget(slot);
        widget.addSlotPair(new SlotPair(data, null, d.input() ? RecipeIngredientRole.INPUT
                : RecipeIngredientRole.OUTPUT, d.col(), d.row(), d.tint()));
    }

    private Widget createDescriptorWidget(SlotDescriptor d, SlotData data) {
        if (d.kind() == SlotKind.FLUID) {
            PhantomFluidSlotWidget slot = new PhantomFluidSlotWidget(data, 0, 0);
            slot.setClientSideWidget();
            slot.setDragOwner(widget);
            slot.setRole(d.input() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT);
            return slot;
        }
        PhantomHandler handler = new PhantomHandler(data);
        PaintSlotWidget slot = new PaintSlotWidget(handler, 0, 0, 0);
        slot.setClearSlotOnRightClick(true);
        slot.setClientSideWidget();
        slot.setDragOwner(widget);
        slot.setRole(d.input() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT);
        slot.setTint(d.tint());
        return slot;
    }

    private static ItemStack onlyItem(IRecipeSlotView view) {
        for (ItemStack s : view.getIngredients(VanillaTypes.ITEM_STACK).toList()) {
            return s;
        }
        return ItemStack.EMPTY;
    }

    private static FluidStack onlyFluid(IRecipeSlotView view) {
        return Services.platform().readFluidIngredient(view).orElse(FluidStack.empty());
    }
}