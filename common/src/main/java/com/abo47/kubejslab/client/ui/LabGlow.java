package com.abo47.kubejslab.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;

public final class LabGlow {
    private static final ResourceLocation GLOW_SHADER = new ResourceLocation("kubejslab", "glow");

    private LabGlow() {
    }

    private static ShaderTexture shader() {
        return ShaderTexture.createShader(GLOW_SHADER);
    }

    public static void drawGlow(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h) {
        drawGlow(graphics, mouseX, mouseY, x, y, w, h, LabColors.INTERACTIVE);
    }

    public static void drawGlow(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h, int glowColor) {
        ShaderTexture s = shader();
        if (s == null) {
            return;
        }
        float r = FastColor.ARGB32.red(glowColor) / 255f;
        float g = FastColor.ARGB32.green(glowColor) / 255f;
        float b = FastColor.ARGB32.blue(glowColor) / 255f;
        s.setUniformCache(cache -> cache.glUniform4F("uGlowColor", r, g, b, 1f));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        s.draw(graphics, mouseX, mouseY, x, y, w, h);
    }
}
