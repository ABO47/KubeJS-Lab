package com.abo47.kubejslab.recipe.vanilla;

import com.abo47.kubejslab.recipe.LabRecipeMachines;

public final class VanillaRecipeMachines {
    private VanillaRecipeMachines() {
    }

    public static void register() {
        LabRecipeMachines.register(new CraftingMachine());
        LabRecipeMachines.register(new FurnaceMachine());
        LabRecipeMachines.register(new BlastFurnaceMachine());
        LabRecipeMachines.register(new SmokerMachine());
        LabRecipeMachines.register(new CampfireMachine());
        LabRecipeMachines.register(new StonecutterMachine());
        LabRecipeMachines.register(new SmithingMachine());
    }
}
