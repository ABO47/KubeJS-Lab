package com.abo47.kubejslab.client;

import java.util.function.Consumer;

import net.minecraft.client.KeyMapping;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.network.NetworkRegistry;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;


public final class Keybindings {
    private static final String CATEGORY = "key.categories." + KubeJSLab.MOD_ID;

    public static final KeyMapping OPEN_UI = new KeyMapping(
            "key." + KubeJSLab.MOD_ID + ".open_ui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY);

    private Keybindings() {
    }

    public static void registerKeyMappings(Consumer<KeyMapping> registrar) {
        registrar.accept(OPEN_UI);
    }

    public static void onClientTick() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (OPEN_UI.consumeClick()) {
            NetworkRegistry.requestOpenScreen();
        }
    }
}
