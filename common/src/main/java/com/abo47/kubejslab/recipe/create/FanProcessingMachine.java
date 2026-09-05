package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;


public final class FanProcessingMachine extends ProcessingRecipeMachine {
    public FanProcessingMachine(String jeiPath, String jsonPath) {
        super(new ResourceLocation("create", jeiPath), "create:" + jsonPath,
                false, false, false, true);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 3, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 3, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 2, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 2, true),
                new SlotDescriptor(false, SlotKind.ITEM, 3, 2, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 3, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 3, true),
                new SlotDescriptor(false, SlotKind.ITEM, 3, 3, true));
    }
}