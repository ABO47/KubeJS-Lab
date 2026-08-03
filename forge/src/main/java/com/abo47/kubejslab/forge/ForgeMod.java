package com.abo47.kubejslab.forge;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.platform.Services;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KubeJSLab.MOD_ID)
public final class ForgeMod {
    public ForgeMod() {
        Services.setPlatform(new ForgePlatformService());

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(KubeJSLab::bootstrap);
    }
}
