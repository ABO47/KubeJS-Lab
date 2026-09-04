package com.abo47.kubejslab.client.ui.widgets;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;


public final class ScrollBarWidget extends Widget {
    public static final int RESERVED_WIDTH = UiLayout.SCROLLBAR_W;
    private static final int RAIL_WIDTH = 2;
    private static final int MIN_KNOB_HEIGHT = UiLayout.KNOB_MIN_H;
    private static final ColorRectTexture RAIL_TEX = new ColorRectTexture(
            UiColors.withAlpha(UiColors.SURFACE_PANEL_ALT, 170));
    private static final ColorRectTexture THUMB_TEX = new ColorRectTexture(UiColors.BORDER_BASE);
    private static final ColorRectTexture THUMB_ACTIVE_TEX = new ColorRectTexture(UiColors.TEXT_MUTED);

    private final IntSupplier valueSupplier;
    private final IntSupplier maxSupplier;
    private final IntSupplier knobHeightSupplier;
    private final IntConsumer valueConsumer;
    private final BooleanSupplier draggingSupplier;
    private final Consumer<Boolean> draggingConsumer;
    private final Runnable refresh;

    public ScrollBarWidget(
            int x,
            int y,
            int width,
            int height,
            IntSupplier valueSupplier,
            IntSupplier maxSupplier,
            IntSupplier knobHeightSupplier,
            IntConsumer valueConsumer,
            BooleanSupplier draggingSupplier,
            Consumer<Boolean> draggingConsumer,
            Runnable refresh
    ) {
        super(x, y, width, height);
        this.valueSupplier = valueSupplier;
        this.maxSupplier = maxSupplier;
        this.knobHeightSupplier = knobHeightSupplier;
        this.valueConsumer = valueConsumer;
        this.draggingSupplier = draggingSupplier;
        this.draggingConsumer = draggingConsumer;
        this.refresh = refresh;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();

        int railX = x + Math.max(0, (w - RAIL_WIDTH) / 2);
        int max = Math.max(0, maxSupplier.getAsInt());
        if (max <= 0) {
            return;
        }
        RAIL_TEX.draw(g, mx, my, railX, y, RAIL_WIDTH, h);

        int knobH = knobHeight();
        int current = ScrollMath.clamp(valueSupplier.getAsInt(), max);
        int span = Math.max(0, h - knobH);
        int knobY = y + (span <= 0 ? 0 : Math.round((float) span * ((float) current / (float) max)));
        boolean hovered = isMouseOverElement(mx, my);
        ColorRectTexture thumb = draggingSupplier.getAsBoolean() || hovered ? THUMB_ACTIVE_TEX : THUMB_TEX;
        thumb.draw(g, mx, my, x, knobY, w, knobH);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        draggingConsumer.accept(true);
        updateFromMouse(mouseY);
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        int max = Math.max(0, maxSupplier.getAsInt());
        if (max <= 0) {
            return true;
        }
        int current = ScrollMath.clamp(valueSupplier.getAsInt(), max);
        int step = Math.max(1, Math.min(24, Math.max(1, max / 8)));
        int next = ScrollMath.wheel(current, max, step, wheelDelta);
        if (next != current) {
            valueConsumer.accept(next);
            refresh.run();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingSupplier.getAsBoolean()) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (updateFromMouse(mouseY)) {
            refresh.run();
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!draggingSupplier.getAsBoolean()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        draggingConsumer.accept(false);
        refresh.run();
        return true;
    }

    private boolean updateFromMouse(double mouseY) {
        int current = valueSupplier.getAsInt();
        int next = ScrollMath.byMouse(
                (int) Math.round(mouseY),
                getPositionY(),
                getSizeHeight(),
                knobHeight(),
                maxSupplier.getAsInt()
        );
        if (next == current) {
            return false;
        }
        valueConsumer.accept(next);
        return true;
    }

    private int knobHeight() {
        int h = getSizeHeight();
        return Math.max(1, Math.min(h, Math.max(MIN_KNOB_HEIGHT, knobHeightSupplier.getAsInt())));
    }
}