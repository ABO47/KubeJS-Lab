package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


public class DrainingMachine extends ProcessingRecipeMachine {
    public DrainingMachine() {
        super(new ResourceLocation("create", "draining"), "create:emptying", false, false, false, false);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(new LabSlotDescriptor(true, LabSlotKind.FLUID, 0, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 0, false));
    }
}
