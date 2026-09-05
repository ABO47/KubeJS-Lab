package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;


public class MixingMachine extends ProcessingRecipeMachine {
    public MixingMachine() {
        super(new ResourceLocation("create", "mixing"), "create:mixing", true, true, false, false);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(true, SlotKind.FLUID, 1, 2, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 0, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 0, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 1, true),
                new SlotDescriptor(false, SlotKind.FLUID, 0, 2, false));
    }
}
