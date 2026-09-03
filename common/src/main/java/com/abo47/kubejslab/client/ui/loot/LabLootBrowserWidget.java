package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabCardBrowserWidget;


public final class LabLootBrowserWidget extends LabCardBrowserWidget<LabLootCardWidget, LabLootIndex.LabLootEntry> {
    private String lootTypeFilter;

    public LabLootBrowserWidget(int x, int y, int w, int h) {
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

    public void setLootClickListener(Consumer<LabLootIndex.LabLootEntry> listener) {
        setEntryClickListener(listener);
    }

    public void setLootRightClickListener(LootRightClick listener) {
        setEntryRightClickListener(listener::onRightClick);
    }

    public void setSelectedLootId(ResourceLocation selectedId) {
        setSelectedId(selectedId);
    }

    @Override
    protected List<LabLootIndex.LabLootEntry> entries() {
        List<LabLootIndex.LabLootEntry> entries = new ArrayList<>(LabLootIndex.search(query(), kubejsOnly(), lootTypeFilter));
        entries.addAll(LabLootStates.stateEntries().stream()
                .filter(e -> lootTypeFilter == null || lootTypeFilter.isBlank() || lootTypeFilter.equals(e.lootType()))
                .toList());
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(LabLootIndex.LabLootEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabLootIndex.LabLootEntry::id));
        return entries;
    }

    @Override
    protected LabLootCardWidget createCard(LabLootIndex.LabLootEntry entry, int x, int y, int w, int h) {
        LabLootCardWidget card = new LabLootCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(LabLootStates.statusOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(LabLootIndex.LabLootEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface LootRightClick {
        void onRightClick(LabLootIndex.LabLootEntry entry, double mouseX, double mouseY);
    }
}
