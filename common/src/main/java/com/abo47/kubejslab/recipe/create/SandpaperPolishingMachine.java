package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;
import com.abo47.kubejslab.recipe.model.LabSlotLayouts;


public class SandpaperPolishingMachine extends ProcessingRecipeMachine {
    public SandpaperPolishingMachine() {
        super(new ResourceLocation("create", "sandpaper_polishing"), "create:sandpaper_polishing",
                false, false, false, true);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return LabSlotLayouts.oneInput();
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 0, false));
    }
}
