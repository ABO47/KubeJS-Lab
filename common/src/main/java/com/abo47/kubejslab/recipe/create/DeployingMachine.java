package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


public class DeployingMachine extends ProcessingRecipeMachine {
    public DeployingMachine() {
        super(new ResourceLocation("create", "deploying"), "create:deploying", false, false, true, false);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 1, true));
    }
}
