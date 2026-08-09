package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabCardBrowserWidget;
import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;


public final class LabItemBrowserWidget extends LabCardBrowserWidget<LabItemCardWidget, LabItemIndex.LabItemEntry> {
    public LabItemBrowserWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setItemClickListener(Consumer<LabItemIndex.LabItemEntry> itemClickListener) {
        setEntryClickListener(itemClickListener);
    }

    public void setItemRightClickListener(ItemRightClick itemRightClickListener) {
        setEntryRightClickListener(itemRightClickListener::onRightClick);
    }

    public void setSelectedItemId(ResourceLocation selectedItemId) {
        setSelectedId(selectedItemId);
    }

    @Override
    protected List<LabItemIndex.LabItemEntry> entries() {
        List<LabItemIndex.LabItemEntry> entries = new ArrayList<>(LabItemIndex.search(query(), kubejsOnly()));
        entries.addAll(LabItemStates.stateEntries().stream()
                .filter(e -> query().isBlank() || e.matches(LabSearchNormalizer.normalizeUserSearch(query())))
                .toList());
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(LabItemIndex.LabItemEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabItemIndex.LabItemEntry::id));
        return entries;
    }

    @Override
    protected LabItemCardWidget createCard(LabItemIndex.LabItemEntry entry, int x, int y, int w, int h) {
        LabItemCardWidget card = new LabItemCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(LabItemStates.statusOf(entry.id()));
        card.setPending(LabItemStates.pendingRestartOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(LabItemIndex.LabItemEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface ItemRightClick {
        void onRightClick(LabItemIndex.LabItemEntry entry, double mouseX, double mouseY);
    }
}