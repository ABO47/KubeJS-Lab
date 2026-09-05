package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;


public class DrainingMachine extends ProcessingRecipeMachine {
    public DrainingMachine() {
        super(new ResourceLocation("create", "draining"), "create:emptying", false, false, false, false);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(new SlotDescriptor(true, SlotKind.FLUID, 0, 0, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.ITEM, 2, 0, false));
    }
}
