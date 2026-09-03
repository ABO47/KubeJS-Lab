package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabCardBrowserWidget;
import com.abo47.kubejslab.client.ui.base.LabEntryCardWidget;


public final class LabLootEntryBrowserWidget
        extends LabCardBrowserWidget<LabEntryCardWidget, LabLootEntryBrowserWidget.EntryRef> {
    public record EntryRef(int index) {
    }

    private final LabLootPoolSettingsWidget settings;

    public LabLootEntryBrowserWidget(LabLootPoolSettingsWidget settings, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.settings = settings;
        setClientSideWidget();
    }

    public static ResourceLocation entryId(int index) {
        return new ResourceLocation("kubejslab", "pool-entry-" + index);
    }

    @Override
    protected List<EntryRef> entries() {
        List<EntryRef> matches = new ArrayList<>();
        String query = query().toLowerCase(Locale.ROOT).trim();
        for (int j = 0; j < settings.entryCount(); j++) {
            if (!query.isBlank()) {
                String haystack = (settings.entryCardName(j) + " " + settings.entryCardId(j))
                        .toLowerCase(Locale.ROOT);
                if (!haystack.contains(query)) {
                    continue;
                }
            }
            matches.add(new EntryRef(j));
        }
        return matches;
    }

    @Override
    protected LabEntryCardWidget createCard(EntryRef entry, int x, int y, int w, int h) {
        return new LabEntryCardWidget(x, y, w, h,
                settings.entryCardIcon(entry.index()),
                settings.entryCardName(entry.index()),
                settings.entryCardId(entry.index()),
                () -> settings.selectEntry(entry.index()),
                (mx, my) -> {
                    settings.selectEntry(entry.index());
                    fireEntryRightClick(entry, mx, my);
                });
    }

    @Override
    protected ResourceLocation entryId(EntryRef entry) {
        return entryId(entry.index());
    }
}
