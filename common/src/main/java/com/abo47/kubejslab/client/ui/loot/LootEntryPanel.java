package com.abo47.kubejslab.client.ui.loot;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.client.ui.widgets.TextField;


public final class LootEntryPanel extends WidgetGroup {
    public interface EntryContextHandler {
        void onEntryRightClick(int index, double mx, double my);
    }

    public interface EmptyContextHandler {
        void onEmptyRightClick(double mx, double my);
    }

    private final LootPoolSettingsWidget settings;
    private final LootEntryBrowserWidget browser;
    private EntryContextHandler entryContextHandler;
    private EmptyContextHandler emptyContextHandler;

    public LootEntryPanel(LootPoolSettingsWidget settings, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.settings = settings;

        this.browser = new LootEntryBrowserWidget(settings, 0,
                UiLayout.SEARCH_H + UiLayout.SEARCH_LIST_GAP, w,
                h - UiLayout.SEARCH_H - UiLayout.SEARCH_LIST_GAP);
        browser.setEntryRightClickListener((entry, mx, my) -> {
            if (entryContextHandler != null) {
                entryContextHandler.onEntryRightClick(entry.index(), mx, my);
            }
        });
        addWidget(browser);

        TextField search = new TextField(
                UiLayout.LIST_INSET,
                0,
                w - UiLayout.LIST_INSET - 2,
                UiLayout.SEARCH_H,
                null,
                query -> browser.setQuery(query));
        search.setClientSideWidget();
        search.setMaxStringLength(Integer.MAX_VALUE);
        search.setValidator(SearchNormalizer::normalizeUserSearch);
        search.setBordered(false);
        search.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        search.setTextColor(UiColors.TEXT_PRIMARY);
        addWidget(search);

        settings.setEntryListListener(this::refresh);
        refresh();
    }

    public void setEntryContextHandler(EntryContextHandler handler) {
        entryContextHandler = handler;
    }

    public void setEmptyContextHandler(EmptyContextHandler handler) {
        emptyContextHandler = handler;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) {
            return true;
        }
        if (button == UiColors.MOUSE_BUTTON_RIGHT && emptyContextHandler != null
                && isMouseOverElement(mx, my)) {
            emptyContextHandler.onEmptyRightClick(mx, my);
            return true;
        }
        return false;
    }

    private void refresh() {
        browser.setSelectedId(LootEntryBrowserWidget.entryId(settings.getSelectedEntry()));
    }
}
