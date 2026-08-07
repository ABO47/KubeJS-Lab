package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


public final class FanProcessingMachine extends ProcessingRecipeMachine {
    public FanProcessingMachine(String jeiPath, String jsonPath) {
        super(new ResourceLocation("create", jeiPath), "create:" + jsonPath,
                false, false, false, true);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 2, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 2, true));
    }
}