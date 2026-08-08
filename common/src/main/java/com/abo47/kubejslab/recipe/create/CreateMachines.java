package com.abo47.kubejslab.recipe.create;

import com.abo47.kubejslab.recipe.LabRecipeMachines;

import dev.architectury.platform.Platform;


public final class CreateMachines {
    private static final String MOD_ID = "create";

    private CreateMachines() {
    }

    public static void register() {
        if (!Platform.isModLoaded(MOD_ID)) {
            return;
        }
        LabRecipeMachines.register(new MillingMachine());
        LabRecipeMachines.register(new CrushingWheelMachine());
        LabRecipeMachines.register(new PressingMachine());
        LabRecipeMachines.register(new FanProcessingMachine("fan_washing", "splashing"));
        LabRecipeMachines.register(new FanProcessingMachine("fan_haunting", "haunting"));
        LabRecipeMachines.register(new FanSmokingMachine());
        LabRecipeMachines.register(new FanBlastingMachine());
        LabRecipeMachines.register(new MixingMachine());
        LabRecipeMachines.register(new CompactingMachine());
        LabRecipeMachines.register(new SawingMachine());
        LabRecipeMachines.register(new SandpaperPolishingMachine());
        LabRecipeMachines.register(new DeployingMachine());
        LabRecipeMachines.register(new ItemApplicationMachine());
        LabRecipeMachines.register(new SpoutFillingMachine());
        LabRecipeMachines.register(new DrainingMachine());
        LabRecipeMachines.register(new MechanicalCraftingMachine());
        LabRecipeMachines.register(new BlockCuttingMachine());
    }
}
