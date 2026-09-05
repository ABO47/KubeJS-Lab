package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;


public class SpoutFillingMachine extends ProcessingRecipeMachine {
    public SpoutFillingMachine() {
        super(new ResourceLocation("create", "spout_filling"), "create:filling", false, false, false, false);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false),
                new SlotDescriptor(true, SlotKind.FLUID, 1, 2, true));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.ITEM, 3, 0, false));
    }
}
