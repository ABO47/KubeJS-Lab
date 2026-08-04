package com.abo47.kubejslab.client.ui;

public final class LabScrollMath {
    private LabScrollMath() {
    }

    public static int clamp(int value, int max) {
        return Math.max(0, Math.min(Math.max(0, max), value));
    }

    public static int wheel(int current, int max, int step, double wheelDelta) {
        int next = current + (wheelDelta > 0 ? -step : step);
        return clamp(next, max);
    }

    public static int byMouse(int mouseY, int trackTop, int trackHeight, int knobHeight, int maxValue) {
        if (maxValue <= 0 || trackHeight <= 0) {
            return 0;
        }
        int span = Math.max(1, trackHeight - knobHeight);
        int target = Math.max(0, Math.min(span, mouseY - trackTop - knobHeight / 2));
        return Math.round((float) target / (float) span * maxValue);
    }
}
