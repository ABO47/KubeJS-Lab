package com.abo47.kubejslab.recipe.vanilla;

import net.minecraft.resources.ResourceLocation;

public final class FurnaceMachine extends CookingBase {
    private static final ResourceLocation JEI_UID = new ResourceLocation("minecraft", "furnace");

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
    }

    @Override
    public String jsonType() {
        return "minecraft:smelting";
    }
}
