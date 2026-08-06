package com.abo47.kubejslab.recipe.create;

import dev.architectury.platform.Platform;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.LabRecipeMachines;

public final class CreateMachines {
    private static final String MOD_ID = "create";

    private CreateMachines() {
    }

    public static void register() {
        if (!Platform.isModLoaded(MOD_ID)) {
            return;
        }
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("milling"), true, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("crushing"), true, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("pressing"), false, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("splashing"), false, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("haunting"), false, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("mixing"), true, true, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("compacting"), true, true, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("cutting"), true, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("sandpaper_polishing"), false, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("deploying"), false, false, true));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("item_application"), false, false, true));
        LabRecipeMachines.register(new MechanicalCraftingMachine());
    }

    private static ResourceLocation uid(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
