package com.abo47.kubejslab.recipe.immersiveengineering;

import com.abo47.kubejslab.recipe.MachineRegistry;

import dev.architectury.platform.Platform;


public final class ImmersiveMachines {
    private ImmersiveMachines() {
    }

    public static void register() {
        if (!Platform.isModLoaded("immersiveengineering")) {
            return;
        }
        MachineRegistry.register(new AlloyMachine());
        MachineRegistry.register(new BlastFurnaceMachine());
        MachineRegistry.register(new CokeOvenMachine());
        MachineRegistry.register(new ClocheMachine());
        MachineRegistry.register(new MetalPressMachine());
        MachineRegistry.register(new CrusherMachine());
        MachineRegistry.register(new SawmillMachine());
        MachineRegistry.register(new ArcFurnaceMachine());
        MachineRegistry.register(new BottlingMachine());
        MachineRegistry.register(new MixerMachine());
        MachineRegistry.register(new RefineryMachine());
        MachineRegistry.register(new FermenterSqueezerMachine("fermenter"));
        MachineRegistry.register(new FermenterSqueezerMachine("squeezer"));
        MachineRegistry.register(new BlueprintMachine());
    }
}