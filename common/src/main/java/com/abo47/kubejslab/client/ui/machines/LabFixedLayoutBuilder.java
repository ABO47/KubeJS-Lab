package com.abo47.kubejslab.client.ui.machines;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;
import com.abo47.kubejslab.platform.Services;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;


final class LabFixedLayoutBuilder {
    private final LabMachineLayoutWidget widget;

    LabFixedLayoutBuilder(LabMachineLayoutWidget widget) {
        this.widget = widget;
    }

    void build(LabRecipeMachine support) {
        List<LabSlotDescriptor> inputDesc = new ArrayList<>(support.inputSlots());
        List<LabSlotDescriptor> outputDesc = new ArrayList<>(support.outputSlots());

        int maxInputCol = 0;
        int minOutputCol = Integer.MAX_VALUE;
        int maxOutputCol = 0;
        int maxRow = 0;
        for (LabSlotDescriptor d : inputDesc) {
            maxInputCol = Math.max(maxInputCol, d.col());
            maxRow = Math.max(maxRow, d.row());
        }
        for (LabSlotDescriptor d : outputDesc) {
            minOutputCol = Math.min(minOutputCol, d.col());
            maxOutputCol = Math.max(maxOutputCol, d.col());
            maxRow = Math.max(maxRow, d.row());
        }
        int outputColOffset = minOutputCol == Integer.MAX_VALUE ? 0 : maxInputCol + 2 - minOutputCol;

        int gridW = maxInputCol + 1;
        int gridH = maxRow + 1;
        if (minOutputCol != Integer.MAX_VALUE) {
            gridW = Math.max(gridW, maxInputCol + 1 + 1 + (maxOutputCol - minOutputCol + 1));
        }
        int ox = (widget.getSizeWidth() - gridW * 18) / 2;
        int oy = Math.max(LabLayout.MACHINE_PAD, (widget.getSizeHeight() - gridH * 18) / 2);

        LabRecipeIndex.LabRecipeEntry entry = widget.entry();
        IRecipeLayoutDrawable<?> jeiLayout = widget.jeiLayout();
        Recipe<?> original = widget.original();
        List<ProcessingOutput> rollable = LabMachineLayoutWidget.rollableResults(original);
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

        int outputOX = ox + outputColOffset * 18;
        int itemInputIndex = 0;
        int fluidInputIndex = 0;
        for (LabSlotDescriptor d : inputDesc) {
            LabSlotData data = new LabSlotData();
            if (d.kind() == LabSlotKind.FLUID) {
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
            addFixedSlot(d, data, ox, oy);
        }

        int itemOutputIndex = 0;
        int fluidOutputIndex = 0;
        for (LabSlotDescriptor d : outputDesc) {
            LabSlotData data = new LabSlotData();
            if (d.kind() == LabSlotKind.FLUID) {
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
            addFixedSlot(d, data, outputOX, oy);
        }
        widget.notifyOutputsChanged();
    }

    private void addFixedSlot(LabSlotDescriptor d, LabSlotData data, int ox, int oy) {
        Widget slot = createDescriptorWidget(d, data);
        slot.setSelfPosition(ox + d.col() * 18, oy + d.row() * 18);
        widget.addWidget(slot);
        widget.addSlotPair(new LabSlotPair(data, null, d.input() ? RecipeIngredientRole.INPUT
                : RecipeIngredientRole.OUTPUT, d.col(), d.row()));
    }

    private Widget createDescriptorWidget(LabSlotDescriptor d, LabSlotData data) {
        if (d.kind() == LabSlotKind.FLUID) {
            LabPhantomFluidSlotWidget slot = new LabPhantomFluidSlotWidget(data, 0, 0);
            slot.setClientSideWidget();
            slot.setDragOwner(widget);
            slot.setRole(d.input() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT);
            return slot;
        }
        LabPhantomHandler handler = new LabPhantomHandler(data);
        LabPhantomSlotWidget slot = new LabPhantomSlotWidget(handler, 0, 0, 0);
        slot.setClearSlotOnRightClick(true);
        slot.setClientSideWidget();
        slot.setDragOwner(widget);
        slot.setRole(d.input() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT);
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