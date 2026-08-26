package com.abo47.kubejslab.client.ui.base;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;


public final class LabTab extends Widget {
    private static final ColorRectTexture ACTIVE_FILL = new ColorRectTexture(LabColors.SURFACE_BASE);
    private static final ColorRectTexture INACTIVE_FILL = new ColorRectTexture(
            LabColors.withAlpha(LabColors.SURFACE_PANEL_ALT, 142));
    private static final ColorRectTexture BORDER_TEX = new ColorRectTexture(LabColors.BORDER_BASE);

    private final String label;
    private final TextTexture labelTex;
    private boolean active;
    private Supplier<LabTabCounts> countsSupplier;
    private String totalTooltipKey;

    public LabTab(int x, int y, int w, int h, String translationKey, boolean active) {
        super(x, y, w, h);
        this.label = translationKey;
        this.labelTex = new TextTexture(translationKey, -1)
                .setWidth(Math.max(1, w - 8))
                .setType(TextTexture.TextType.HIDE);
        this.active = active;
    }

    public void setCounts(Supplier<LabTabCounts> countsSupplier, String totalTooltipKey) {
        this.countsSupplier = countsSupplier;
        this.totalTooltipKey = totalTooltipKey;
    }

    public boolean isTabActive() {
        return active;
    }

    public void setTabActive(boolean active) {
        this.active = active;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();

        ColorRectTexture fill = active ? ACTIVE_FILL : INACTIVE_FILL;
        fill.draw(g, mx, my, x, y, w, h);
        BORDER_TEX.draw(g, mx, my, x, y, w, 1);
        BORDER_TEX.draw(g, mx, my, x, y, 1, h);
        BORDER_TEX.draw(g, mx, my, x + w - 1, y, 1, h);

        if (!active && isMouseOverElement(mx, my)) {
            LabGlow.drawGlow(g, mx, my, x, y, w, h);
        }

        if (!label.isEmpty()) {
            labelTex.setColor(active ? LabColors.TEXT_PRIMARY : LabColors.TEXT_MUTED);
            labelTex.draw(g, mx, my, x, y, w, h);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        if (countsSupplier != null && isMouseOverElement(mx, my)) {
            LabTabCounts c = countsSupplier.get();
            setHoverTooltips(
                    Component.translatable(totalTooltipKey, c.total()),
                    Component.translatable(LabGuiKeys.LAB_TAB_TOOLTIP_DISABLED, c.disabled()),
                    Component.translatable(LabGuiKeys.LAB_TAB_TOOLTIP_MODIFIED, c.modified()));
        }
        super.drawInForeground(g, mx, my, pt);
    }
}
