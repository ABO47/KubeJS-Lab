package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.picker.SearchQuery;
import com.abo47.kubejslab.client.ui.widgets.CardBrowserWidget;
import com.abo47.kubejslab.client.ui.widgets.EntryCardWidget;


public final class LootEntryBrowserWidget
        extends CardBrowserWidget<EntryCardWidget, LootEntryBrowserWidget.EntryRef> {
    public record EntryRef(int index) {
    }

    private final LootPoolSettingsWidget settings;

    public LootEntryBrowserWidget(LootPoolSettingsWidget settings, int x, int y, int w, int h) {
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
        SearchQuery parsed = SearchQuery.parse(query());
        String tagNeedle = parsed.tag() == null ? ""
                : SearchNormalizer.normalizeUserSearch(parsed.tag());
        for (int j = 0; j < settings.entryCount(); j++) {
            String name = settings.entryCardName(j);
            String id = settings.entryCardId(j);
            if (!parsed.text().isBlank() || !tagNeedle.isBlank() || !parsed.mod().isBlank()) {
                String haystack = (name + " " + id).toLowerCase(Locale.ROOT);
                if (!parsed.text().isBlank() && !haystack.contains(parsed.text())) {
                    continue;
                }
                if (!tagNeedle.isBlank() && !haystack.contains(tagNeedle)) {
                    continue;
                }
                if (!parsed.mod().isBlank()) {
                    String idLower = id == null ? "" : id.toLowerCase(Locale.ROOT);
                    int colon = idLower.indexOf(':');
                    String namespace = colon >= 0 ? idLower.substring(0, colon) : "";
                    if (!parsed.mod().equals(namespace) && !haystack.contains(parsed.mod())) {
                        continue;
                    }
                }
            }
            matches.add(new EntryRef(j));
        }
        return matches;
    }

    @Override
    protected EntryCardWidget createCard(EntryRef entry, int x, int y, int w, int h) {
        EntryCardWidget card = new EntryCardWidget(x, y, w, h,
                settings.entryCardIcon(entry.index()),
                settings.entryCardName(entry.index()),
                settings.entryCardId(entry.index()),
                () -> settings.selectEntry(entry.index()),
                (mx, my) -> {
                    settings.selectEntry(entry.index());
                    fireEntryRightClick(entry, mx, my);
                });
        String tool = settings.entryToolRequirement(entry.index());
        if (tool != null && !tool.isBlank() && !"none".equals(tool)) {
            card.setHoverTooltips(List.of(
                    Component.literal(settings.entryCardName(entry.index())),
                    Component.translatable(LootKeys.LOOT_PREVIEW_REQUIRES)
                            .append(Component.translatable(toolLabelKey(tool)))));
        }
        return card;
    }

    private static String toolLabelKey(String tool) {
        return switch (tool) {
            case "silk_touch" -> LootKeys.LOOT_TOOL_SILK_TOUCH;
            case "fortune" -> LootKeys.LOOT_TOOL_FORTUNE;
            case "shears" -> LootKeys.LOOT_TOOL_SHEARS;
            case "silk_touch_or_shears" -> LootKeys.LOOT_TOOL_SILK_TOUCH_OR_SHEARS;
            case "no_silk_touch" -> LootKeys.LOOT_TOOL_NO_SILK_TOUCH;
            default -> LootKeys.LOOT_TOOL_NONE;
        };
    }

    @Override
    protected ResourceLocation entryId(EntryRef entry) {
        return entryId(entry.index());
    }
}
