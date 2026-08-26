package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabCardBrowserWidget;
import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;


public final class LabBlockBrowserWidget extends LabCardBrowserWidget<LabBlockCardWidget, LabBlockIndex.LabBlockEntry> {
    public LabBlockBrowserWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setBlockClickListener(Consumer<LabBlockIndex.LabBlockEntry> blockClickListener) {
        setEntryClickListener(blockClickListener);
    }

    public void setBlockRightClickListener(BlockRightClick blockRightClickListener) {
        setEntryRightClickListener(blockRightClickListener::onRightClick);
    }

    public void setSelectedBlockId(ResourceLocation selectedBlockId) {
        setSelectedId(selectedBlockId);
    }

    @Override
    protected List<LabBlockIndex.LabBlockEntry> entries() {
        List<LabBlockIndex.LabBlockEntry> entries = new ArrayList<>(LabBlockIndex.search(query(), kubejsOnly()));
        entries.addAll(LabBlockStates.stateEntries().stream()
                .filter(e -> query().isBlank() || e.matches(LabSearchNormalizer.normalizeUserSearch(query())))
                .toList());
        Set<ResourceLocation> seen = new HashSet<>();
        entries.removeIf(e -> !seen.add(e.id()));
        entries.sort(Comparator.comparing(LabBlockIndex.LabBlockEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabBlockIndex.LabBlockEntry::id));
        return entries;
    }

    @Override
    protected LabBlockCardWidget createCard(LabBlockIndex.LabBlockEntry entry, int x, int y, int w, int h) {
        LabBlockCardWidget card = new LabBlockCardWidget(x, y, w, h, entry,
                () -> fireEntryClick(entry),
                (mouseX, mouseY) -> fireEntryRightClick(entry, mouseX, mouseY));
        card.setStatus(LabBlockStates.statusOf(entry.id()));
        card.setPending(LabBlockStates.pendingRestartOf(entry.id()));
        return card;
    }

    @Override
    protected ResourceLocation entryId(LabBlockIndex.LabBlockEntry entry) {
        return entry.id();
    }

    @FunctionalInterface
    public interface BlockRightClick {
        void onRightClick(LabBlockIndex.LabBlockEntry entry, double mouseX, double mouseY);
    }
}
