package com.abo47.kubejslab.client.ui.loot;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabTextFieldWidget;
import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;


public final class LabLootEntryPanel extends WidgetGroup {
    public interface EntryContextHandler {
        void onEntryRightClick(int index, double mx, double my);
    }

    public interface EmptyContextHandler {
        void onEmptyRightClick(double mx, double my);
    }

    private final LabLootPoolSettingsWidget settings;
    private final LabLootEntryBrowserWidget browser;
    private EntryContextHandler entryContextHandler;
    private EmptyContextHandler emptyContextHandler;

    public LabLootEntryPanel(LabLootPoolSettingsWidget settings, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.settings = settings;

        this.browser = new LabLootEntryBrowserWidget(settings, 0,
                LabLayout.SEARCH_H + LabLayout.SEARCH_LIST_GAP, w,
                h - LabLayout.SEARCH_H - LabLayout.SEARCH_LIST_GAP);
        browser.setEntryRightClickListener((entry, mx, my) -> {
            if (entryContextHandler != null) {
                entryContextHandler.onEntryRightClick(entry.index(), mx, my);
            }
        });
        addWidget(browser);

        LabTextFieldWidget search = new LabTextFieldWidget(
                LabLayout.LIST_INSET,
                0,
                w - LabLayout.LIST_INSET * 2,
                LabLayout.SEARCH_H,
                null,
                query -> browser.setQuery(query));
        search.setClientSideWidget();
        search.setMaxStringLength(Integer.MAX_VALUE);
        search.setValidator(LabSearchNormalizer::normalizeUserSearch);
        search.setBordered(false);
        search.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
        search.setTextColor(LabColors.TEXT_PRIMARY);
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
        if (button == LabColors.MOUSE_BUTTON_RIGHT && emptyContextHandler != null
                && isMouseOverElement(mx, my)) {
            emptyContextHandler.onEmptyRightClick(mx, my);
            return true;
        }
        return false;
    }

    private void refresh() {
        browser.setSelectedId(LabLootEntryBrowserWidget.entryId(settings.getSelectedEntry()));
    }
}
