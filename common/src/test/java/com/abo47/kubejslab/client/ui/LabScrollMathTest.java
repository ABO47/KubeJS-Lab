package com.abo47.kubejslab.client.ui;
import com.abo47.kubejslab.client.ui.base.LabScrollMath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LabScrollMathTest {
    @Test
    void clampNeverGoesNegativeOrBeyondMax() {
        assertEquals(0, LabScrollMath.clamp(-5, 100));
        assertEquals(100, LabScrollMath.clamp(150, 100));
        assertEquals(50, LabScrollMath.clamp(50, 100));
        assertEquals(0, LabScrollMath.clamp(0, 0));
        assertEquals(0, LabScrollMath.clamp(10, -3));
    }

    @Test
    void wheelScrollsUpWhenDeltaIsPositive() {
        assertEquals(0, LabScrollMath.wheel(5, 100, 10, 1));
        assertEquals(100, LabScrollMath.wheel(100, 100, 10, -100));
        assertEquals(100, LabScrollMath.wheel(100, 100, 10, -1));
        assertEquals(15, LabScrollMath.wheel(5, 100, 10, -1));
    }

    @Test
    void byMouseMapsKnobPositionToValue() {
        int value = LabScrollMath.byMouse(20, 0, 100, 20, 100);
        assertEquals(13, value);
        assertEquals(100, LabScrollMath.byMouse(150, 0, 100, 20, 100));
        assertEquals(0, LabScrollMath.byMouse(0, 0, 100, 20, 100));
        assertEquals(0, LabScrollMath.byMouse(50, 0, 100, 20, 0));
    }
}