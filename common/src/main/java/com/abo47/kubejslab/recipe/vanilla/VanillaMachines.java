package com.abo47.kubejslab.recipe.vanilla;

import com.abo47.kubejslab.recipe.MachineRegistry;


public final class VanillaMachines {
    private VanillaMachines() {
    }

    public static void register() {
        MachineRegistry.register(new CraftingMachine());
        MachineRegistry.register(new FurnaceMachine());
        MachineRegistry.register(new BlastFurnaceMachine());
        MachineRegistry.register(new SmokerMachine());
        MachineRegistry.register(new CampfireMachine());
        MachineRegistry.register(new StonecutterMachine());
        MachineRegistry.register(new SmithingMachine());
    }
}
