package com.abo47.kubejslab.forge.mixin;

import java.util.Map;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(RecipeManager.class)
public interface RecipeManagerInvoker {
    @Invoker("apply")
    void kubejslab$apply(Map<ResourceLocation, JsonElement> recipes, ResourceManager resources,
            ProfilerFiller profiler);
}
