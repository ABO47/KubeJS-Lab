package com.abo47.kubejslab.client.ui.theme;

import java.util.HashMap;
import java.util.Map;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;


public final class UiColors {
    private static final Map<Long, IGuiTexture> BORDERED_CACHE = new HashMap<>();

    private UiColors() {
    }

    public static final int SURFACE_BASE = 0xFF171C21;
    public static final int SURFACE_PANEL = 0xFF202933;
    public static final int SURFACE_PANEL_ALT = 0xFF2C3742;
    public static final int BORDER_BASE = 0xFF546170;
    public static final int BORDER_ACCENT = 0xFF65B7C8;
    public static final int TEXT_PRIMARY = 0xFFEAF1F4;
    public static final int TEXT_SECONDARY = 0xFFB8C7CE;
    public static final int TEXT_MUTED = 0xFF88979F;
    public static final int SUCCESS = 0xFF66D38D;
    public static final int INTERACTIVE = 0xFF64C3D2;
    public static final int WARNING = 0xFFE5B44A;
    public static final int ERROR = 0xFFE06F73;
    public static final int POPUP_FILL = 0xFF0D1114;
    public static final int TAG_GOLD = 0xffd9b84c;
    public static final int INPUT_TINT = 0x402E7CF6;
    public static final int OUTPUT_TINT = 0x40FF8C42;
    public static final int FLUID_INPUT_TINT = 0x802E5BF6;
    public static final int FLUID_OUTPUT_TINT = 0x80FF7A1A;
    public static final int ADDITIVE_TINT = 0x8047B33D;
    public static final int BLUEPRINT_TINT = 0x8000C8C8;
    public static final int MOLD_TINT = 0x809933CC;

    public static final int MOUSE_BUTTON_LEFT = 0;
    public static final int MOUSE_BUTTON_RIGHT = 1;

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static IGuiTexture bordered(int fill, int border) {
        long key = ((long) fill << 32) | (border & 0xFFFFFFFFL);
        return BORDERED_CACHE.computeIfAbsent(key, k -> {
            ColorRectTexture fillTex = new ColorRectTexture(fill);
            ColorRectTexture borderTex = new ColorRectTexture(border);
            return (g, mx, my, x, y, w, h) -> {
                fillTex.draw(g, mx, my, x, y, w, h);
                borderTex.draw(g, mx, my, x, y, w, 1);
                borderTex.draw(g, mx, my, x, y + h - 1, w, 1);
                borderTex.draw(g, mx, my, x, y, 1, h);
                borderTex.draw(g, mx, my, x + w - 1, y, 1, h);
            };
        });
    }

    public static int pressedFill(int accent) {
        return withAlpha(accent, 76);
    }
}
