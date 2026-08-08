package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


public class CompactingMachine extends ProcessingRecipeMachine {
    public CompactingMachine() {
        super(new ResourceLocation("create", "packing"), "create:compacting", true, true, false, false);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 2, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 2, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 2, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 2, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 2, 2, true),
                new LabSlotDescriptor(true, LabSlotKind.FLUID, 0, 3, true));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 2, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 2, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 2, true),
                new LabSlotDescriptor(false, LabSlotKind.FLUID, 2, 3, true));
    }
}
