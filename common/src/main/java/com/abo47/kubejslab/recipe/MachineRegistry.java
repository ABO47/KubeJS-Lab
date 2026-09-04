package com.abo47.kubejslab.recipe;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.create.CreateMachines;
import com.abo47.kubejslab.recipe.immersiveengineering.ImmersiveMachines;
import com.abo47.kubejslab.recipe.vanilla.VanillaMachines;


public final class MachineRegistry {
    private static final Map<ResourceLocation, RecipeHandler> MACHINES = new HashMap<>();
    private static boolean initialized;

    private MachineRegistry() {
    }

    @Nullable
    public static RecipeHandler get(ResourceLocation jeiUid) {
        init();
        return jeiUid == null ? null : MACHINES.get(jeiUid);
    }

    public static boolean supports(ResourceLocation jeiUid) {
        return get(jeiUid) != null;
    }

    public static void register(RecipeHandler machine) {
        MACHINES.put(machine.jeiUid(), machine);
    }

    private static void init() {
        if (!initialized) {
            initialized = true;
            VanillaMachines.register();
            CreateMachines.register();
            ImmersiveMachines.register();
        }
    }
}
