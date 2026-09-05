package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.widgets.CardBrowserWidget;


public final class BlockBrowserWidget extends CardBrowserWidget<BlockCardWidget, BlockIndex.BlockEntry> {
    private String typeFilter;

    public BlockBrowserWidget(int x, int y, int w, int h) {
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

    public void setBlockClickListener(Consumer<BlockIndex.BlockEntry> blockClickListener) {
        setEntryClickListener(blockClickListener);
    }

    public void setBlockRightClickListener(BlockRightClick blockRightClickListener) {
        setEntryRightClickListener(blockRightClickListener::onRightClick);
    }

    public void setSelectedBlockId(ResourceLocation selectedBlockId) {
        setSelectedId(selectedBlockId);
    }

    @Override
    protected List<BlockIndex.BlockEntry> entries() {
        List<BlockIndex.BlockEntry> entries = new ArrayList<>(BlockIndex.search(query(), kubejsOnly()));
        entries.addAll(BlockStates.stateEntries().stream()
                .filter(e -> query().isBlank() || e.matches(SearchNormalizer.normalizeUserSearch(query())))
                .toList());
        if (typeFilter != null && !typeFilter.isBlank()) {
            entries.removeIf(e -> {
                var state = BlockStates.stateOf(e.id());
                String t = (state != null && state.type() != null && !state.type().isBlank()) ? state.type() : BlockIndex.typeOf(e.id());
                return !typeFilter.equals(t);
            });
        }
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(BlockIndex.BlockEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(BlockIndex.BlockEntry::id));
        return entries;
    }

    @Override
    protected BlockCardWidget createCard(BlockIndex.BlockEntry entry, int x, int y, int w, int h) {
        BlockCardWidget card = new BlockCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(BlockStates.statusOf(entry.id()));
        card.setPending(BlockStates.pendingRestartOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(BlockIndex.BlockEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface BlockRightClick {
        void onRightClick(BlockIndex.BlockEntry entry, double mouseX, double mouseY);
    }
}
