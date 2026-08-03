package com.abo47.kubejslab.client.ui;

public final class LabColors {
    private LabColors() {
    }

    public static final int SURFACE_BASE = 0xFF171C21;
    public static final int SURFACE_PANEL = 0xFF202933;
    public static final int SURFACE_PANEL_ALT = 0xFF2C3742;
    public static final int BORDER_BASE = 0xFF546170;
    public static final int TEXT_PRIMARY = 0xFFEAF1F4;
    public static final int TEXT_MUTED = 0xFF88979F;

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }
}
