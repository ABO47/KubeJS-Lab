package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


public class MixingMachine extends ProcessingRecipeMachine {
    public MixingMachine() {
        super(new ResourceLocation("create", "mixing"), "create:mixing", true, true, false, false);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.FLUID, 1, 2, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.FLUID, 0, 2, false));
    }
}
