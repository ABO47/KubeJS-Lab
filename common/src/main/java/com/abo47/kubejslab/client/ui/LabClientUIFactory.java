package com.abo47.kubejslab.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;


public final class LabClientUIFactory {
    private LabClientUIFactory() {
    }

    public static void openFromScreen(FriendlyByteBuf buf, int windowId) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        BlockPos holder = buf.readBlockPos();
        ModularUI ui = LabScreen.createUI(holder, player);
        ui.initWidgets();
        LabGuiContainer gui = new LabGuiContainer(ui, windowId);
        LabScreen.activateClient(ui);
        minecraft.setScreen(gui);
        minecraft.player.containerMenu = gui.getMenu();
    }
}
