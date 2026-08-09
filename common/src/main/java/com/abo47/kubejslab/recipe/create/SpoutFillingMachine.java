package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;


public class SpoutFillingMachine extends ProcessingRecipeMachine {
    public SpoutFillingMachine() {
        super(new ResourceLocation("create", "spout_filling"), "create:filling", false, false, false, false);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false),
                new LabSlotDescriptor(true, LabSlotKind.FLUID, 1, 2, true));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(new LabSlotDescriptor(false, LabSlotKind.ITEM, 3, 0, false));
    }
}
