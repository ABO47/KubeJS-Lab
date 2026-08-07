package com.abo47.kubejslab.client.ui.base;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;


public final class LabToggleSwitchWidget extends SwitchWidget {
    public static final int DEFAULT_WIDTH = 34;
    public static final int DEFAULT_HEIGHT = 16;

    private static final long ANIMATION_MS = 170L;
    private static final ColorRectTexture[] BORDER_TEX = new ColorRectTexture[]{
            new ColorRectTexture(LabColors.BORDER_BASE),
            new ColorRectTexture(LabColors.SUCCESS)};
    private static final ColorRectTexture TRACK_TEX =
            new ColorRectTexture(LabColors.withAlpha(LabColors.SURFACE_BASE, 210));
    private static final ColorRectTexture[] KNOB_TEX = new ColorRectTexture[]{
            new ColorRectTexture(LabColors.TEXT_SECONDARY),
            new ColorRectTexture(LabColors.TEXT_PRIMARY)};
    private static final ColorRectTexture SHADOW_TEX =
            new ColorRectTexture(LabColors.withAlpha(LabColors.SURFACE_BASE, 100));
    private static final ColorRectTexture[] HIGHLIGHT_TEX = new ColorRectTexture[]{
            new ColorRectTexture(LabColors.withAlpha(LabColors.TEXT_PRIMARY, 48)),
            new ColorRectTexture(LabColors.withAlpha(LabColors.TEXT_PRIMARY, 90))};
    private static final ColorRectTexture[] ACTIVE_TRACK_TEX = new ColorRectTexture[79];

    static {
        for (int i = 0; i < ACTIVE_TRACK_TEX.length; i++) {
            ACTIVE_TRACK_TEX[i] = new ColorRectTexture(LabColors.withAlpha(LabColors.SUCCESS, 70 + i));
        }
    }

    private final BooleanSupplier valueSupplier;
    private final Consumer<Boolean> valueConsumer;
    private final Runnable refresh;
    private Motion localMotion;

    public LabToggleSwitchWidget(int x, int y, BooleanSupplier valueSupplier, Consumer<Boolean> valueConsumer, Runnable refresh) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
        this.valueSupplier = valueSupplier == null ? () -> false : valueSupplier;
        this.valueConsumer = valueConsumer == null ? value -> {} : valueConsumer;
        this.refresh = refresh == null ? () -> {} : refresh;
        setClientSideWidget();
        setSupplier(this.valueSupplier::getAsBoolean);
        setOnPressCallback(this::handleToggle);
        setPressed(this.valueSupplier.getAsBoolean());
        refreshTextures();
    }

    private void handleToggle(ClickData clickData, Boolean pressed) {
        boolean from = valueSupplier.getAsBoolean();
        boolean to = Boolean.TRUE.equals(pressed);
        startMotion(from, to);
        valueConsumer.accept(to);
        refresh.run();
    }

    private void startMotion(boolean from, boolean to) {
        if (from != to) {
            localMotion = new Motion(from, to, System.currentTimeMillis());
        }
    }

    private VisualState visualState(boolean target) {
        Motion motion = localMotion;
        if (motion == null || motion.to() != target) {
            return new VisualState(target ? 1.0f : 0.0f, 0.0f);
        }
        long elapsed = System.currentTimeMillis() - motion.startMs();
        if (elapsed < 0L || elapsed >= ANIMATION_MS) {
            localMotion = null;
            return new VisualState(target ? 1.0f : 0.0f, 0.0f);
        }
        float t = Math.max(0.0f, Math.min(1.0f, elapsed / (float) ANIMATION_MS));
        float eased = t * t * (3.0f - 2.0f * t);
        float amount = motion.from() ? 1.0f - eased : eased;
        float pulse = (float) Math.sin(eased * Math.PI);
        return new VisualState(amount, pulse);
    }

    private void refreshTextures() {
        setBaseTexture(new SwitchVisualTexture(false));
        setPressedTexture(new SwitchVisualTexture(true));
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (isMouseOverElement(mouseX, mouseY)) {
            LabGlow.drawGlow(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
    }

    private final class SwitchVisualTexture implements IGuiTexture {
        private final boolean target;

        private SwitchVisualTexture(boolean target) {
            this.target = target;
        }

        @Override
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            VisualState visual = visualState(target);
            float amount = visual.amount();
            float pulse = visual.pulse();
            int ix = Math.round(x);
            int iy = Math.round(y);
            int state = target ? 1 : 0;
            ColorRectTexture borderTex = BORDER_TEX[state];
            ColorRectTexture knobTex = KNOB_TEX[state];

            borderTex.draw(graphics, 0, 0, ix, iy, width, height);
            int trackW = Math.max(2, width - 1) - 1;
            int trackH = Math.max(2, height - 1) - 1;
            TRACK_TEX.draw(graphics, 0, 0, ix + 1, iy + 1, trackW, trackH);
            int activeW = Math.round((width - 2) * amount);
            if (activeW > 2) {
                ACTIVE_TRACK_TEX[Math.round(amount * 78)]
                        .draw(graphics, 0, 0, ix + 1, iy + 1, activeW, trackH);
            }

            int knobSize = Math.max(8, height - 4 + Math.round(pulse * 2.0f));
            int knobTravel = Math.max(0, width - knobSize - 4);
            int knobX = ix + 2 + Math.round(knobTravel * amount);
            int knobY = iy + Math.max(2, (height - knobSize) / 2);
            SHADOW_TEX.draw(graphics, 0, 0, knobX + 1, knobY + 1, knobSize, knobSize);
            knobTex.draw(graphics, 0, 0, knobX, knobY, knobSize, knobSize);
            HIGHLIGHT_TEX[state].draw(graphics, 0, 0, knobX + 2, knobY + 2, knobSize - 4, 1);
        }
    }

    private record Motion(boolean from, boolean to, long startMs) {
    }

    private record VisualState(float amount, float pulse) {
    }
}
