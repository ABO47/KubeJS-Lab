package com.abo47.kubejslab.client.ui;

public final class LabLayout {
    private LabLayout() {
    }

    public static final int ROOT_W = 563;
    public static final int ROOT_H = 352;
    public static final int PAD = 16;
    public static final int PAD_Y = 8;

    public static final int BODY_X = PAD;
    public static final int BODY_Y = PAD_Y;
    public static final int BODY_W = ROOT_W - PAD * 2;
    public static final int BODY_H = ROOT_H - BODY_Y - PAD_Y;

    public static final int LEFT_PANEL_W = 168;
    public static final int GAP = 3;

    public static final int PANEL_INSET = 6;

    public static final int TAB_H = 18;
    public static final int TAB_GAP = 4;
    public static final int TAB_INSET = PANEL_INSET;
}
