package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabCardBrowserWidget;


public final class LabRecipeBrowserWidget extends LabCardBrowserWidget<LabRecipeCardWidget, LabRecipeIndex.LabRecipeEntry> {
    private Set<ResourceLocation> machineRecipeIds;
    private ResourceLocation machineUid;

    public LabRecipeBrowserWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setRecipeClickListener(Consumer<LabRecipeIndex.LabRecipeEntry> recipeClickListener) {
        setEntryClickListener(recipeClickListener);
    }

    public void setRecipeRightClickListener(RecipeRightClick recipeRightClickListener) {
        setEntryRightClickListener(recipeRightClickListener::onRightClick);
    }

    public void setSelectedRecipeId(ResourceLocation selectedRecipeId) {
        setSelectedId(selectedRecipeId);
    }

    public void setMachineFilter(Set<ResourceLocation> machineRecipeIds) {
        this.machineRecipeIds = machineRecipeIds;
        resetScroll();
    }

    public void setMachineUid(ResourceLocation machineUid) {
        this.machineUid = machineUid;
        resetScroll();
    }

    @Override
    protected List<LabRecipeIndex.LabRecipeEntry> entries() {
        List<LabRecipeIndex.LabRecipeEntry> entries =
                new ArrayList<>(LabRecipeIndex.search(query(), kubejsOnly(), machineRecipeIds));
        entries.addAll(LabRecipeStates.disabledEntries(machineUid).stream()
                .filter(e -> kubejsOnly() == e.kubejs())
                .filter(e -> query().isBlank() || e.matches(query()))
                .toList());
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(LabRecipeIndex.LabRecipeEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabRecipeIndex.LabRecipeEntry::id));
        return entries;
    }

    @Override
    protected LabRecipeCardWidget createCard(LabRecipeIndex.LabRecipeEntry entry, int x, int y, int w, int h) {
        LabRecipeCardWidget card = new LabRecipeCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(LabRecipeStates.statusOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(LabRecipeIndex.LabRecipeEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface RecipeRightClick {
        void onRightClick(LabRecipeIndex.LabRecipeEntry entry, double mouseX, double mouseY);
    }
}