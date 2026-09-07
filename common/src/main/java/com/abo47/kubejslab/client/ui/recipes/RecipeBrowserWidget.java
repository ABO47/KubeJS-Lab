package com.abo47.kubejslab.client.ui.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.picker.SearchQuery;
import com.abo47.kubejslab.client.ui.widgets.CardBrowserWidget;
import com.abo47.kubejslab.recipe.model.RecipeStatus;


public final class RecipeBrowserWidget extends CardBrowserWidget<RecipeCardWidget, RecipeIndex.RecipeEntry> {
    private Set<ResourceLocation> machineRecipeIds;
    private ResourceLocation machineUid;

    public RecipeBrowserWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setRecipeClickListener(Consumer<RecipeIndex.RecipeEntry> recipeClickListener) {
        setEntryClickListener(recipeClickListener);
    }

    public void setRecipeRightClickListener(RecipeRightClick recipeRightClickListener) {
        setEntryRightClickListener(recipeRightClickListener::onRightClick);
    }

    public void setSelectedRecipeId(ResourceLocation selectedRecipeId) {
        setSelectedId(selectedRecipeId);
    }

    public void setMachineFilter(Set<ResourceLocation> machineRecipeIds) {
        if (Objects.equals(this.machineRecipeIds, machineRecipeIds)) {
            return;
        }
        this.machineRecipeIds = machineRecipeIds;
        resetScroll();
    }

    public void setMachineUid(ResourceLocation machineUid) {
        if (Objects.equals(this.machineUid, machineUid)) {
            return;
        }
        this.machineUid = machineUid;
        resetScroll();
    }

    @Override
    protected List<RecipeIndex.RecipeEntry> entries() {
        List<RecipeIndex.RecipeEntry> base = RecipeIndex.search(query(), kubejsOnly(), machineRecipeIds);
        if (base.isEmpty() && machineRecipeIds != null && !machineRecipeIds.isEmpty()) {
            int unfiltered = RecipeIndex.search(query(), kubejsOnly(), null).size();
            int total = RecipeIndex.search("", false, null).size() + RecipeIndex.search("", true, null).size();
            KubeJSLab.LOGGER.warn(
                    "[RecipeBrowserWidget] filtered 0 with machineIds {} query='{}' kubejsOnly={} -> unfiltered={} total={} machineUid={} — fallback to unfiltered",
                    machineRecipeIds.size(), query(), kubejsOnly(), unfiltered, total, machineUid);
            if (unfiltered > 0) {
                base = RecipeIndex.search(query(), kubejsOnly(), null);
            }
        }
        List<RecipeIndex.RecipeEntry> entries = new ArrayList<>(base);
        SearchQuery parsed = SearchQuery.parse(query());
        String tagNeedle = parsed.tag() == null ? ""
                : SearchNormalizer.normalizeUserSearch(parsed.tag());
        entries.addAll(RecipeStates.disabledEntries(machineUid).stream()
                .filter(e -> kubejsOnly() == e.kubejs())
                .filter(e -> parsed.matchesMod(e.id()))
                .filter(e -> parsed.matchesText(e.normalizedId(), e.normalizedName()))
                .filter(e -> tagNeedle.isBlank() || e.matches(tagNeedle))
                .toList());
        if (parsed.onlyDisabled() || parsed.onlyModified()) {
            entries.removeIf(e -> {
                RecipeStatus status = RecipeStates.statusOf(e.id());
                boolean keepDisabled = parsed.onlyDisabled() && status == RecipeStatus.DISABLED;
                boolean keepModified = parsed.onlyModified() && status == RecipeStatus.MODIFIED;
                return !(keepDisabled || keepModified);
            });
        }
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(RecipeIndex.RecipeEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RecipeIndex.RecipeEntry::id));
        if (entries.isEmpty()) {
            int total = RecipeIndex.search("", false, null).size() + RecipeIndex.search("", true, null).size();
            KubeJSLab.LOGGER.warn(
                    "[RecipeBrowserWidget] still empty after fallback: query='{}' kubejsOnly={} machineIds={} totalEntries={}",
                    query(), kubejsOnly(), machineRecipeIds == null ? "null" : machineRecipeIds.size(), total);
        }
        return entries;
    }

    @Override
    protected RecipeCardWidget createCard(RecipeIndex.RecipeEntry entry, int x, int y, int w, int h) {
        RecipeCardWidget card = new RecipeCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(RecipeStates.statusOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(RecipeIndex.RecipeEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface RecipeRightClick {
        void onRightClick(RecipeIndex.RecipeEntry entry, double mouseX, double mouseY);
    }
}