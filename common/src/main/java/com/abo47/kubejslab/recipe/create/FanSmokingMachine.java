package com.abo47.kubejslab.recipe.create;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.vanilla.CookingBase;


public final class FanSmokingMachine extends CookingBase {
    @Override
    public ResourceLocation jeiUid() {
        return new ResourceLocation("create", "fan_smoking");
    }

    @Override
    public String jsonType() {
        return "minecraft:smoking";
    }
}