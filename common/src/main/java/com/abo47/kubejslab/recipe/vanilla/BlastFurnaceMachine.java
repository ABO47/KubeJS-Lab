package com.abo47.kubejslab.recipe.vanilla;

import net.minecraft.resources.ResourceLocation;


public final class BlastFurnaceMachine extends CookingBase {
    private static final ResourceLocation JEI_UID = new ResourceLocation("minecraft", "blasting");

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
    }

    @Override
    public String jsonType() {
        return "minecraft:blasting";
    }
}
