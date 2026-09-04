package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;
import com.abo47.kubejslab.recipe.model.SlotLayouts;


public class SawingMachine extends ProcessingRecipeMachine {
    public SawingMachine() {
        super(new ResourceLocation("create", "sawing"), "create:cutting", true, false, false, true, "sawing");
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return SlotLayouts.oneInput();
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 0, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 0, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 1, 1, true));
    }
}
