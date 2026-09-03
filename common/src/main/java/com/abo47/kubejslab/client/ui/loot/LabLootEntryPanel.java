package com.abo47.kubejslab.client.ui.loot;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabActionButton;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.base.LabTextFieldWidget;
import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;


public final class LabLootEntryPanel extends WidgetGroup {
    private static final int ADD_BTN_H = 16;

    private final LabLootPoolSettingsWidget settings;
    private final LabLootEntryBrowserWidget browser;

    public LabLootEntryPanel(LabLootPoolSettingsWidget settings, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.settings = settings;

        this.browser = new LabLootEntryBrowserWidget(settings, 0,
                LabLayout.SEARCH_H + LabLayout.SEARCH_LIST_GAP, w,
                h - LabLayout.SEARCH_H - LabLayout.SEARCH_LIST_GAP - ADD_BTN_H - LabLayout.SEARCH_LIST_GAP);
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

        LabActionButton add = new LabActionButton(
                LabLayout.LIST_INSET,
                h - ADD_BTN_H,
                w - LabLayout.LIST_INSET * 2,
                ADD_BTN_H,
                Component.translatable(LabGuiKeys.LAB_LOOT_ADD_ENTRY).getString(),
                settings::addEntry);
        addWidget(add);

        settings.setEntryListListener(this::refresh);
        refresh();
    }

    private void refresh() {
        browser.setSelectedId(LabLootEntryBrowserWidget.entryId(settings.getSelectedEntry()));
    }
}
