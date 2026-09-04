package com.abo47.kubejslab.forge;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.command.MainCommand;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.platform.Services;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(KubeJSLab.MOD_ID)
public final class ForgeMod {
    public ForgeMod() {
        Services.setPlatform(new ForgePlatformService());

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeContent.register(modBus);
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onCommonSetupNetwork);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(KubeJSLab::bootstrap);
    }

    private void onCommonSetupNetwork(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkRegistry::register);
    }

    private void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        MainCommand.register(event.getDispatcher());
    }
}
