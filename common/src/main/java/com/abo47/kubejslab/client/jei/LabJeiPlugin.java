package com.abo47.kubejslab.client.jei;

import javax.annotation.Nonnull;

import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IJeiRuntime;

import com.abo47.kubejslab.KubeJSLab;

@JeiPlugin
public final class LabJeiPlugin implements IModPlugin {
    private static IJeiRuntime jeiRuntime;
    private static IJeiHelpers jeiHelpers;

    public static IJeiRuntime runtime() {
        return jeiRuntime;
    }

    public static IJeiHelpers helpers() {
        return jeiHelpers;
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(KubeJSLab.MOD_ID, "jei_plugin");
    }
}
