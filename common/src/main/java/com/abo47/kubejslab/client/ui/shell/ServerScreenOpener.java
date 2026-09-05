package com.abo47.kubejslab.client.ui.shell;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.lowdragmc.lowdraglib.core.mixins.accessor.ServerPlayerAccessor;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.side.ForgeEventHooks;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.block.runtime.BlockService;
import com.abo47.kubejslab.item.runtime.ItemService;
import com.abo47.kubejslab.loot.runtime.LootService;
import com.abo47.kubejslab.loot.runtime.LootTableScanner;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.loot.S2CLootTableListPacket;
import com.abo47.kubejslab.recipe.runtime.RecipeService;

import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;


public final class ServerScreenOpener {
    private ServerScreenOpener() {
    }

    public static void open(BlockPos holder, ServerPlayer player) {
        ModularUI ui = ScreenFactory.createUI(holder, player);
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
        KubeJSLab.LOGGER.info("[Screen] open for {}: OpenScreen payload is {} bytes (blockPos only)", player.getName().getString(),
                serializedHolder.readableBytes());
        if (serializedHolder.readableBytes() > 512) {
            KubeJSLab.LOGGER.warn("OpenScreen payload is large: {} bytes", serializedHolder.readableBytes());
        }

        ModularUIContainer container = new ModularUIContainer(ui, windowId);
        NetworkRegistry.sendOpenScreen(player, serializedHolder, windowId);
        NetworkRegistry.sendRecipeState(player, RecipeService.statePacket());
        NetworkRegistry.sendItemState(player, ItemService.statePacket());
        NetworkRegistry.sendBlockState(player, BlockService.statePacket());
        NetworkRegistry.sendLootState(player, LootService.statePacket());
        NetworkRegistry.sendLootTableList(player, new S2CLootTableListPacket(LootTableScanner.scan(player.getServer())));

        accessor.callInitMenu(container);
        player.containerMenu = container;
        if (Platform.isForge()) {
            ForgeEventHooks.postPlayerContainerEvent(player, container);
        }
    }
}
