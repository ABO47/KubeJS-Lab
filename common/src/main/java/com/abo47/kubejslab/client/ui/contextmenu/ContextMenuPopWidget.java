package com.abo47.kubejslab.client.ui.contextmenu;
import java.util.function.LongSupplier;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import com.abo47.kubejslab.client.ui.theme.UiColors;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;


public final class ContextMenuPopWidget extends WidgetGroup {
    private static final long MENU_MS = 95;
    private static final float START_SCALE = 0.97f;
    private static final float MENU_Z = 240.0f;
    private static final int SHADOW_ALPHA = 58;
    private static final int VEIL_ALPHA = 48;

    private final LongSupplier startMsSupplier;

    private ContextMenuPopWidget(int x, int y, int w, int h, WidgetGroup content, LongSupplier startMsSupplier) {
        super(x, y, w, h);
        this.startMsSupplier = startMsSupplier;
        content.setSelfPosition(new Position(0, 0));
        addWidget(content);
    }

    public static WidgetGroup menu(WidgetGroup content, LongSupplier startMsSupplier) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        return new ContextMenuPopWidget(content.getPositionX(), content.getPositionY(),
                content.getSizeWidth(), content.getSizeHeight(), content, startMsSupplier);
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mx, int my, float pt) {
        float amount = openAmount();
        if (amount <= 0.01f) {
            return;
        }
        float scale = UiAnimationProgress.interpolate(START_SCALE, 1.0f, amount);
        int x = getPositionX();
        int y = getPositionY();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0f, 0.0f, MENU_Z);
        try {
            new ColorRectTexture(UiColors.withAlpha(UiColors.SURFACE_BASE, (int) (SHADOW_ALPHA * amount)))
                    .draw(graphics, mx, my, x + 2, y + 3, getSizeWidth(), getSizeHeight());
            pose.translate(x, y, 0.0f);
            pose.scale(scale, scale, 1.0f);
            pose.translate(-x, -y, 0.0f);
            super.drawInBackground(graphics, mx, my, pt);
            new ColorRectTexture(UiColors.withAlpha(UiColors.SURFACE_BASE, (int) (VEIL_ALPHA * (1.0f - amount))))
                    .draw(graphics, mx, my, x, y, getSizeWidth(), getSizeHeight());
        } finally {
            pose.popPose();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
        }
    }

    private float openAmount() {
        long startMs = startMsSupplier.getAsLong();
        if (!UiAnimationProgress.running(startMs, MENU_MS)) {
            return 1.0f;
        }
        return UiAnimationProgress.cubicOutProgress(startMs, MENU_MS);
    }
}
