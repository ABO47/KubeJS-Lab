package com.abo47.kubejslab.recipe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.create.CreateMachines;
import com.abo47.kubejslab.recipe.vanilla.VanillaRecipeMachines;

public final class LabRecipeMachines {
    private static final Map<ResourceLocation, LabRecipeMachine> MACHINES = new HashMap<>();
    private static boolean initialized;

    private LabRecipeMachines() {
    }

    @Nullable
    public static LabRecipeMachine get(ResourceLocation jeiUid) {
        init();
        return jeiUid == null ? null : MACHINES.get(jeiUid);
    }

    public static boolean supports(ResourceLocation jeiUid) {
        return get(jeiUid) != null;
    }

    public static void register(LabRecipeMachine machine) {
        MACHINES.put(machine.jeiUid(), machine);
    }

    private static void init() {
        if (!initialized) {
            initialized = true;
            VanillaRecipeMachines.register();
            CreateMachines.register();
        }
    }
}
