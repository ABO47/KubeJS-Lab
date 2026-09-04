package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;
import com.abo47.kubejslab.recipe.model.SlotLayouts;


public class SandpaperPolishingMachine extends ProcessingRecipeMachine {
    public SandpaperPolishingMachine() {
        super(new ResourceLocation("create", "sandpaper_polishing"), "create:sandpaper_polishing",
                false, false, false, true);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return SlotLayouts.oneInput();
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.ITEM, 0, 0, false));
    }
}
