package com.abo47.kubejslab.client.ui.shell;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;


public final class ClientScreenOpener {
    private ClientScreenOpener() {
    }

    public static void openFromScreen(FriendlyByteBuf buf, int windowId) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        BlockPos holder = buf.readBlockPos();
        ModularUI ui = ScreenFactory.createUI(holder, player);
        ui.initWidgets();
        ScreenContainer gui = new ScreenContainer(ui, windowId);
        ScreenFactory.activateClient(ui);
        minecraft.setScreen(gui);
        minecraft.player.containerMenu = gui.getMenu();
    }
}
