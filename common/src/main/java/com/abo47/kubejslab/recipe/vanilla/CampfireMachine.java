package com.abo47.kubejslab.recipe.vanilla;

import net.minecraft.resources.ResourceLocation;


public final class CampfireMachine extends CookingBase {
    private static final ResourceLocation JEI_UID = new ResourceLocation("minecraft", "campfire");

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
    }

    @Override
    public String jsonType() {
        return "minecraft:campfire_cooking";
    }
}
