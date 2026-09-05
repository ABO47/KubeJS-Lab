package com.abo47.kubejslab.client.ui.contextmenu;

public final class UiAnimationProgress {
    private UiAnimationProgress() {
    }

    public static boolean running(long startMs, long durationMs) {
        if (startMs <= 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        return now - startMs >= 0 && now - startMs < durationMs;
    }

    public static float linearProgress(long startMs, long durationMs) {
        if (durationMs <= 0) {
            return 1.0f;
        }
        float t = (System.currentTimeMillis() - startMs) / (float) durationMs;
        return Math.max(0.0f, Math.min(1.0f, t));
    }

    public static float cubicOut(float progress) {
        float t = Math.max(0.0f, Math.min(1.0f, progress));
        float inverse = 1.0f - t;
        return 1.0f - inverse * inverse * inverse;
    }

    public static float cubicOutProgress(long startMs, long durationMs) {
        return cubicOut(linearProgress(startMs, durationMs));
    }

    public static float interpolate(float from, float to, float progress) {
        float clamped = Math.max(0.0f, Math.min(1.0f, progress));
        return from + (to - from) * clamped;
    }
}
