package com.abo47.kubejslab.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class LabRecipeBrowserWidget extends WidgetGroup {
    private String query;
    private boolean kubejsOnly;
    private Set<ResourceLocation> machineRecipeIds;
    private Set<ResourceLocation> newRecipeIds = Set.of();
    private int scroll;
    private int scrollMax;
    private boolean dragging;
    private Consumer<LabRecipeIndex.LabRecipeEntry> recipeClickListener;
    private List<LabRecipeCardWidget> cards = List.of();

    public LabRecipeBrowserWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
        this.query = "";
    }

    public void setRecipeClickListener(Consumer<LabRecipeIndex.LabRecipeEntry> recipeClickListener) {
        this.recipeClickListener = recipeClickListener;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
        scroll = 0;
        rebuild();
    }

    public void setKubejsOnly(boolean kubejsOnly) {
        this.kubejsOnly = kubejsOnly;
        scroll = 0;
    }

    public void setMachineFilter(Set<ResourceLocation> machineRecipeIds) {
        this.machineRecipeIds = machineRecipeIds;
        scroll = 0;
    }

    public void setNewRecipeIds(Set<ResourceLocation> newRecipeIds) {
        this.newRecipeIds = newRecipeIds;
    }

    public void rebuild() {
        clearAllWidgets();
        cards = new ArrayList<>();
        List<LabRecipeIndex.LabRecipeEntry> entries = LabRecipeIndex.search(query, kubejsOnly, machineRecipeIds);
        int listW = getSizeWidth();
        int listH = getSizeHeight();
        int rowStep = LabLayout.CARD_ROW_STEP;
        int cardH = LabLayout.CARD_H;
        int rows = entries.size();
        int contentH = rows * cardH + Math.max(0, rows - 1) * LabLayout.CARD_GAP;
        scrollMax = Math.max(0, contentH - listH);
        scroll = LabScrollMath.clamp(scroll, scrollMax);
        boolean showScrollBar = scrollMax > 0;

        int trackX = LabLayout.recipeTrackX(listW);
        int cardX = LabLayout.LIST_INSET;
        int cardW = LabLayout.recipeCardWidth(listW);

        for (int row = 0; row < rows; row++) {
            int y = -scroll + row * rowStep;
            LabRecipeIndex.LabRecipeEntry entry = entries.get(row);
            LabRecipeCardWidget card = new LabRecipeCardWidget(cardX, y, cardW, cardH, entry, newRecipeIds.contains(entry.id()), () -> {
                if (recipeClickListener != null) {
                    recipeClickListener.accept(entry);
                }
            });
            cards.add(card);
            addWidget(card);
        }

        if (showScrollBar) {
            int knobH = Math.max(LabLayout.KNOB_MIN_H,
                    (int) ((float) listH * ((float) listH / (float) Math.max(listH, contentH))));
            addWidget(new LabScrollBarWidget(
                    trackX,
                    0,
                    LabScrollBarWidget.RESERVED_WIDTH,
                    listH,
                    () -> scroll,
                    () -> scrollMax,
                    () -> knobH,
                    value -> scroll = value,
                    () -> dragging,
                    value -> dragging = value,
                    this::repositionCards
            ));
        }
    }

    private void repositionCards() {
        int rowStep = LabLayout.CARD_ROW_STEP;
        for (int i = 0; i < cards.size(); i++) {
            LabRecipeCardWidget card = cards.get(i);
            card.setSelfPosition(LabLayout.LIST_INSET, -scroll + i * rowStep);
        }
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        g.flush();
        g.enableScissor(x, y, x + w, y + h);
        for (Widget child : widgets) {
            int cy = child.getPositionY();
            if (cy + child.getSizeHeight() < y || cy > y + h) {
                continue;
            }
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            child.drawInBackground(g, mx, my, pt);
        }
        g.flush();
        g.disableScissor();
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        int step = Math.max(8, LabLayout.CARD_ROW_STEP / 3);
        int next = LabScrollMath.wheel(scroll, scrollMax, step, wheelDelta);
        if (next != scroll) {
            scroll = next;
            repositionCards();
        }
        return true;
    }
}
