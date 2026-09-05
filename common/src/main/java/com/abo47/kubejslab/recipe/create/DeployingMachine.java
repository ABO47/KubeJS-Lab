package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;


public class DeployingMachine extends ProcessingRecipeMachine {
    public DeployingMachine() {
        super(new ResourceLocation("create", "deploying"), "create:deploying", false, false, true, false);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 0, false));
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
