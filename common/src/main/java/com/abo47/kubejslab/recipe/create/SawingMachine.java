package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;
import com.abo47.kubejslab.recipe.model.LabSlotLayouts;


public class SawingMachine extends ProcessingRecipeMachine {
    public SawingMachine() {
        super(new ResourceLocation("create", "sawing"), "create:cutting", true, false, false, true, "sawing");
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return LabSlotLayouts.oneInput();
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
