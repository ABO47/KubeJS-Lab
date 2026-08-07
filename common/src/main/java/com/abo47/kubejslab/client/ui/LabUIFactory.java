package com.abo47.kubejslab.client.ui;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.lowdragmc.lowdraglib.core.mixins.accessor.ServerPlayerAccessor;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.side.ForgeEventHooks;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.recipe.runtime.LabRecipeService;

import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;


public final class LabUIFactory {
    private LabUIFactory() {
    }

    public static void open(BlockPos holder, ServerPlayer player) {
        ModularUI ui = LabScreen.createUI(holder, player);
        if (ui == null) {
            return;
        }
        ui.initWidgets();

        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        ServerPlayerAccessor accessor = (ServerPlayerAccessor) player;
        accessor.callNextContainerCounter();
        int windowId = accessor.getContainerCounter();

        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        serializedHolder.writeBlockPos(holder);
        KubeJSLab.LOGGER.info("[LabUI] open for {}: OpenScreen payload is {} bytes (blockPos only)", player.getName().getString(),
                serializedHolder.readableBytes());
        if (serializedHolder.readableBytes() > 512) {
            KubeJSLab.LOGGER.warn("OpenScreen payload is large: {} bytes", serializedHolder.readableBytes());
        }

        ModularUIContainer container = new ModularUIContainer(ui, windowId);
        ModNetwork.sendOpenScreen(player, serializedHolder, windowId);
        ModNetwork.sendRecipeState(player, LabRecipeService.statePacket());

        accessor.callInitMenu(container);
        player.containerMenu = container;
        if (Platform.isForge()) {
            ForgeEventHooks.postPlayerContainerEvent(player, container);
        }
    }
}
