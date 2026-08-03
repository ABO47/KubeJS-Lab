package com.abo47.kubejslab.client;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.LabScreen;

public final class LabKeybindings {
    private static final String CATEGORY = "key.categories." + KubeJSLab.MOD_ID;

    public static final KeyMapping OPEN_UI = new KeyMapping(
            "key." + KubeJSLab.MOD_ID + ".open_ui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY);

    private LabKeybindings() {
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
            LabScreen.open();
        }
    }
}
