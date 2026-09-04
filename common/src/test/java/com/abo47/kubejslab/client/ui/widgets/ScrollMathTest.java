package com.abo47.kubejslab.client.ui.widgets;
import com.abo47.kubejslab.client.ui.widgets.ScrollMath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ScrollMathTest {
    @Test
    void clampNeverGoesNegativeOrBeyondMax() {
        assertEquals(0, ScrollMath.clamp(-5, 100));
        assertEquals(100, ScrollMath.clamp(150, 100));
        assertEquals(50, ScrollMath.clamp(50, 100));
        assertEquals(0, ScrollMath.clamp(0, 0));
        assertEquals(0, ScrollMath.clamp(10, -3));
    }

    @Test
    void wheelScrollsUpWhenDeltaIsPositive() {
        assertEquals(0, ScrollMath.wheel(5, 100, 10, 1));
        assertEquals(100, ScrollMath.wheel(100, 100, 10, -100));
        assertEquals(100, ScrollMath.wheel(100, 100, 10, -1));
        assertEquals(15, ScrollMath.wheel(5, 100, 10, -1));
    }

    @Test
    void byMouseMapsKnobPositionToValue() {
        int value = ScrollMath.byMouse(20, 0, 100, 20, 100);
        assertEquals(13, value);
        assertEquals(100, ScrollMath.byMouse(150, 0, 100, 20, 100));
        assertEquals(0, ScrollMath.byMouse(0, 0, 100, 20, 100));
        assertEquals(0, ScrollMath.byMouse(50, 0, 100, 20, 0));
    }
}