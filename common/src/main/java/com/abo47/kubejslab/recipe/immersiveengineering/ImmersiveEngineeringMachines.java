package com.abo47.kubejslab.recipe.immersiveengineering;

import dev.architectury.platform.Platform;

import com.abo47.kubejslab.recipe.LabRecipeMachines;

public final class ImmersiveEngineeringMachines {
    private ImmersiveEngineeringMachines() {
    }

    public static void register() {
        if (!Platform.isModLoaded("immersiveengineering")) {
            return;
        }
        LabRecipeMachines.register(new AlloyMachine());
        LabRecipeMachines.register(new BlastFurnaceMachine());
        LabRecipeMachines.register(new CokeOvenMachine());
        LabRecipeMachines.register(new ClocheMachine());
        LabRecipeMachines.register(new MetalPressMachine());
        LabRecipeMachines.register(new CrusherMachine());
        LabRecipeMachines.register(new SawmillMachine());
        LabRecipeMachines.register(new ArcFurnaceMachine());
        LabRecipeMachines.register(new BottlingMachine());
        LabRecipeMachines.register(new MixerMachine());
        LabRecipeMachines.register(new RefineryMachine());
        LabRecipeMachines.register(new FermenterSqueezerMachine("fermenter"));
        LabRecipeMachines.register(new FermenterSqueezerMachine("squeezer"));
        LabRecipeMachines.register(new BlueprintMachine());
    }
}