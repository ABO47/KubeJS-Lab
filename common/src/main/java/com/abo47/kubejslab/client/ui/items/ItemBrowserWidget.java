package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.widgets.CardBrowserWidget;


public final class ItemBrowserWidget extends CardBrowserWidget<ItemCardWidget, ItemIndex.ItemEntry> {
    private String typeFilter;

    public ItemBrowserWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setTypeFilter(String typeFilter) {
        if (typeFilter == null || typeFilter.isBlank() || "all".equals(typeFilter)) {
            this.typeFilter = null;
        } else {
            this.typeFilter = typeFilter;
        }
        resetScroll();
    }

    public void setItemClickListener(Consumer<ItemIndex.ItemEntry> itemClickListener) {
        setEntryClickListener(itemClickListener);
    }

    public void setItemRightClickListener(ItemRightClick itemRightClickListener) {
        setEntryRightClickListener(itemRightClickListener::onRightClick);
    }

    public void setSelectedItemId(ResourceLocation selectedItemId) {
        setSelectedId(selectedItemId);
    }

    @Override
    protected List<ItemIndex.ItemEntry> entries() {
        List<ItemIndex.ItemEntry> entries = new ArrayList<>(ItemIndex.search(query(), kubejsOnly()));
        entries.addAll(ItemStates.stateEntries().stream()
                .filter(e -> query().isBlank() || e.matches(SearchNormalizer.normalizeUserSearch(query())))
                .toList());
        if (typeFilter != null && !typeFilter.isBlank()) {
            entries.removeIf(e -> {
                var state = ItemStates.stateOf(e.id());
                String t = (state != null && state.type() != null && !state.type().isBlank()) ? state.type() : ItemIndex.typeOf(e.id());
                return !typeFilter.equals(t);
            });
        }
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(ItemIndex.ItemEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ItemIndex.ItemEntry::id));
        return entries;
    }

    @Override
    protected ItemCardWidget createCard(ItemIndex.ItemEntry entry, int x, int y, int w, int h) {
        ItemCardWidget card = new ItemCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(ItemStates.statusOf(entry.id()));
        card.setPending(ItemStates.pendingRestartOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(ItemIndex.ItemEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface ItemRightClick {
        void onRightClick(ItemIndex.ItemEntry entry, double mouseX, double mouseY);
    }
}