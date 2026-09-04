package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.widgets.CardBrowserWidget;


public final class LootBrowserWidget extends CardBrowserWidget<LootCardWidget, LootIndex.LootEntry> {
    private String lootTypeFilter;

    public LootBrowserWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setLootTypeFilter(String lootTypeFilter) {
        if (lootTypeFilter == null || lootTypeFilter.isBlank() || "all".equals(lootTypeFilter)) {
            this.lootTypeFilter = null;
        } else {
            this.lootTypeFilter = lootTypeFilter;
        }
        resetScroll();
    }

    public void setLootClickListener(Consumer<LootIndex.LootEntry> listener) {
        setEntryClickListener(listener);
    }

    public void setLootRightClickListener(LootRightClick listener) {
        setEntryRightClickListener(listener::onRightClick);
    }

    public void setSelectedLootId(ResourceLocation selectedId) {
        setSelectedId(selectedId);
    }

    @Override
    protected List<LootIndex.LootEntry> entries() {
        List<LootIndex.LootEntry> entries = new ArrayList<>(LootIndex.search(query(), kubejsOnly(), lootTypeFilter));
        entries.addAll(LootStates.stateEntries().stream()
                .filter(e -> lootTypeFilter == null || lootTypeFilter.isBlank() || lootTypeFilter.equals(e.lootType()))
                .toList());
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(LootIndex.LootEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LootIndex.LootEntry::id));
        return entries;
    }

    @Override
    protected LootCardWidget createCard(LootIndex.LootEntry entry, int x, int y, int w, int h) {
        LootCardWidget card = new LootCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(LootStates.statusOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(LootIndex.LootEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface LootRightClick {
        void onRightClick(LootIndex.LootEntry entry, double mouseX, double mouseY);
    }
}
