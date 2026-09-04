package com.abo47.kubejslab.client.ui.theme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;


public final class IconAtlas {
    private static final String BASE = "textures/gui/icons/";
    private static final Map<String, ResourceLocation> ICON_IDS = new HashMap<>();
    private static final Map<String, ResourceTexture> ICON_TEXTURES = new HashMap<>();
    private static final Set<String> MISSING_LOGGED = new HashSet<>();

    private IconAtlas() {
    }

    public static ResourceLocation icon(String key) {
        String clean = normalizeKey(key);
        if (clean.isBlank() || !isValidIconPath(clean)) {
            return null;
        }
        ResourceLocation cached = ICON_IDS.get(clean);
        if (cached != null) {
            return cached;
        }
        ResourceLocation id = ResourceLocation.tryBuild(KubeJSLab.MOD_ID, BASE + clean + ".png");
        if (id == null || Minecraft.getInstance().getResourceManager().getResource(id).isEmpty()) {
            if (MISSING_LOGGED.add(clean)) {
                KubeJSLab.LOGGER.warn("[IconAtlas] missing icon key={}", clean);
            }
            return null;
        }
        ICON_IDS.put(clean, id);
        return id;
    }

    public static ResourceTexture iconTexture(String key, ActionTone tone) {
        return iconTexture(key, tone.accentColor());
    }

    public static ResourceTexture iconTexture(String key, int color) {
        String clean = normalizeKey(key);
        if (clean.isBlank() || !isValidIconPath(clean)) {
            return null;
        }
        String cacheKey = clean + "|" + Integer.toHexString(color);
        ResourceTexture cached = ICON_TEXTURES.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ResourceLocation id = icon(clean);
        if (id == null) {
            return null;
        }
        ResourceTexture texture = new SmoothResourceTexture(id).setColor(color);
        ICON_TEXTURES.put(cacheKey, texture);
        return texture;
    }

    private static String normalizeKey(String key) {
        String clean = key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
        if (clean.endsWith(".png")) {
            clean = clean.substring(0, clean.length() - 4);
        }
        return clean;
    }

    private static boolean isValidIconPath(String value) {
        if (value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '/' || c == '.' || c == '_' || c == '-') {
                continue;
            }
            return false;
        }
        return true;
    }

    private static final class SmoothResourceTexture extends ResourceTexture {
        private SmoothResourceTexture(ResourceLocation imageLocation) {
            super(imageLocation);
        }

        @Override
        protected void drawSubAreaInternal(GuiGraphics graphics, float x, float y, float width, float height,
                float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
            RenderSystem.setShaderTexture(0, imageLocation);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            super.drawSubAreaInternal(graphics, x, y, width, height, drawnU, drawnV, drawnWidth, drawnHeight);
        }
    }
}
