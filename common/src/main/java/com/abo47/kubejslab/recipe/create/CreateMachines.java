package com.abo47.kubejslab.recipe.create;

import com.abo47.kubejslab.recipe.MachineRegistry;

import dev.architectury.platform.Platform;


public final class CreateMachines {
    private static final String MOD_ID = "create";

    private CreateMachines() {
    }

    public static void register() {
        if (!Platform.isModLoaded(MOD_ID)) {
            return;
        }
        MachineRegistry.register(new MillingMachine());
        MachineRegistry.register(new CrushingWheelMachine());
        MachineRegistry.register(new PressingMachine());
        MachineRegistry.register(new FanProcessingMachine("fan_washing", "splashing"));
        MachineRegistry.register(new FanProcessingMachine("fan_haunting", "haunting"));
        MachineRegistry.register(new FanSmokingMachine());
        MachineRegistry.register(new FanBlastingMachine());
        MachineRegistry.register(new MixingMachine());
        MachineRegistry.register(new CompactingMachine());
        MachineRegistry.register(new SawingMachine());
        MachineRegistry.register(new SandpaperPolishingMachine());
        MachineRegistry.register(new DeployingMachine());
        MachineRegistry.register(new ItemApplicationMachine());
        MachineRegistry.register(new SpoutFillingMachine());
        MachineRegistry.register(new DrainingMachine());
        MachineRegistry.register(new MechanicalCraftingMachine());
        MachineRegistry.register(new BlockCuttingMachine());
    }
}
