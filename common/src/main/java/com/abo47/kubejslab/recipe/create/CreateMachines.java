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
        LabRecipeMachines.register(processing("milling", "milling", true, false, false, true));
        LabRecipeMachines.register(processing("crushing", "crushing", true, false, false, true));
        LabRecipeMachines.register(processing("pressing", "pressing", false, false, false, false));
        LabRecipeMachines.register(processing("fan_washing", "splashing", false, false, false, true));
        LabRecipeMachines.register(processing("fan_haunting", "haunting", false, false, false, true));
        LabRecipeMachines.register(processing("mixing", "mixing", true, true, false, false));
        LabRecipeMachines.register(processing("packing", "compacting", true, true, false, false));
        LabRecipeMachines.register(new ProcessingRecipeMachine(uid("sawing"), uid("cutting").toString(),
                true, false, false, true, "sawing"));
        LabRecipeMachines.register(processing("sandpaper_polishing", "sandpaper_polishing", false, false, false, true));
        LabRecipeMachines.register(processing("deploying", "deploying", false, false, true, false));
        LabRecipeMachines.register(processing("item_application", "item_application", false, false, true, false));
        LabRecipeMachines.register(processing("spout_filling", "filling", false, false, false, false));
        LabRecipeMachines.register(processing("draining", "emptying", false, false, false, false));
        LabRecipeMachines.register(new MechanicalCraftingMachine());
        LabRecipeMachines.register(new BlockCuttingMachine());
    }

    private static ProcessingRecipeMachine processing(String jeiPath, String jsonPath, boolean duration,
            boolean heat, boolean keepHeldItem) {
        return processing(jeiPath, jsonPath, duration, heat, keepHeldItem, false);
    }

    private static ProcessingRecipeMachine processing(String jeiPath, String jsonPath, boolean duration,
            boolean heat, boolean keepHeldItem, boolean chance) {
        return new ProcessingRecipeMachine(uid(jeiPath), uid(jsonPath).toString(), duration, heat, keepHeldItem,
                chance);
    }

    private static ResourceLocation uid(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
