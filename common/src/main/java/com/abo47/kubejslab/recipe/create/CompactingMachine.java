package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;


public class CompactingMachine extends ProcessingRecipeMachine {
    public CompactingMachine() {
        super(new ResourceLocation("create", "packing"), "create:compacting", true, true, false, false);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 2, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 1, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 1, true),
                new SlotDescriptor(true, SlotKind.ITEM, 2, 1, true),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 2, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 2, true),
                new SlotDescriptor(true, SlotKind.ITEM, 2, 2, true),
                new SlotDescriptor(true, SlotKind.FLUID, 0, 3, true));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 0, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 0, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 0, 2, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 2, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 2, true),
                new SlotDescriptor(false, SlotKind.FLUID, 2, 3, true));
    }
}
