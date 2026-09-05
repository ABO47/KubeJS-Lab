package com.abo47.kubejslab.client.ui.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import com.abo47.kubejslab.block.model.BlockEditAction;
import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.block.runtime.BlockService;
import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.client.ui.widgets.CarouselTabWidget;
import com.abo47.kubejslab.client.ui.widgets.OptionDropdownWidget;
import com.abo47.kubejslab.client.ui.widgets.TextField;
import com.abo47.kubejslab.item.model.ItemEditAction;
import com.abo47.kubejslab.item.model.ItemFieldValues;
import com.abo47.kubejslab.loot.model.LootEditAction;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.runtime.LootService;
import com.abo47.kubejslab.recipe.MachineRegistry;
import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeEditAction;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;

public final class WorkspacePanel extends WidgetGroup {
    private static final IGuiTexture PANEL_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_PANEL, UiColors.BORDER_BASE);
    private static final IGuiTexture INNER_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE);
    private static final ColorRectTexture TAB_ERASE_TEX = new ColorRectTexture(UiColors.SURFACE_BASE);
    private static final ColorRectTexture SEPARATOR_TOP_BORDER = new ColorRectTexture(UiColors.BORDER_BASE);

    enum EditMode {
        NEW, MODIFY
    }

    private final boolean isLeft;
    private final Tab[] tabs;
    private final CarouselTabWidget carouselTab;
    private final String[] tabKeys;
    private WorkspacePanel rightPanel;
    private Runnable tabChangedListener;
    private Runnable machineChangedListener;
    private TextFieldWidget searchField;
    RecipeBrowserWidget recipeBrowser;
    ItemBrowserWidget itemBrowser;
    BlockBrowserWidget blockBrowser;
    LootBrowserWidget lootBrowser;
    MachineDropdownWidget machineDropdown;
    MachineLayoutWidget machineLayout;
    RecipeSettingsWidget settingsWidget;
    ItemSettingsWidget itemSettings;
    OptionDropdownWidget itemTypeDropdown;
    ItemPreviewWidget itemPreview;
    BlockSettingsWidget blockSettings;
    OptionDropdownWidget blockTypeDropdown;
    BlockPreviewWidget blockPreview;
    OptionDropdownWidget lootTypeDropdown;
    LootPreviewWidget lootPreview;
    LootSettingsWidget lootSettings;
    LootPoolModal poolModal;
    final RecipePanelSection recipes;
    final ItemPanelSection items;
    final BlockPanelSection blocks;
    final LootPanelSection loot;
    final RecipeSaver saver;
    final ItemSaver itemSaver;
    final BlockSaver blockSaver;
    final LootSaver lootSaver;
    private PlayerInventoryWidget inventory;
    TextTexture modeLabel;
    TextTexture itemModeLabel;
    TextTexture blockModeLabel;
    TextTexture lootModeLabel;
    private int columnX;
    private int columnW;
    private int modeLabelY;

    WorkspacePanel(boolean isLeft) {
        super(
                isLeft ? UiLayout.BODY_X : UiLayout.BODY_X + UiLayout.LEFT_PANEL_W + UiLayout.GAP,
                UiLayout.BODY_Y,
                isLeft ? UiLayout.LEFT_PANEL_W : UiLayout.BODY_W - UiLayout.LEFT_PANEL_W - UiLayout.GAP,
                UiLayout.BODY_H);
        this.isLeft = isLeft;

        int tabInset = UiLayout.TAB_INSET;
        int tabH = UiLayout.TAB_H;
        int tabGap = UiLayout.TAB_GAP;

        if (isLeft) {
            String[] keys = new String[]{UiKeys.TAB_BUILT_IN, UiKeys.TAB_CUSTOM};
            this.tabKeys = keys;
            this.carouselTab = null;
            int tabCount = keys.length;
            int totalTabGap = tabGap * (tabCount - 1);
            int panelW = UiLayout.LEFT_PANEL_W;
            int areaW = Math.max(1, panelW - tabInset * 2);
            int baseW = (areaW - totalTabGap) / tabCount;
            int remainder = (areaW - totalTabGap) % tabCount;
            int tabX = tabInset;
            this.tabs = new Tab[keys.length];
            for (int i = 0; i < keys.length; i++) {
                int w = baseW + (i < remainder ? 1 : 0);
                tabs[i] = new Tab(tabX, UiLayout.PANEL_INSET, w, tabH, keys[i], i == 0);
                addWidget(tabs[i]);
                tabX += w + tabGap;
            }
            tabs[0].setCounts(() -> {
                if (rightPanel != null) {
                    int rt = rightPanel.getSelectedTabIndex();
                    if (rt == 1) return ItemStates.counts(false);
                    if (rt == 2) return BlockStates.counts(false);
                    if (rt == 3) return LootStates.counts(false);
                }
                RecipeIndex.RecipeCounts c = RecipeIndex.counts(false);
                return new TabCounts(c.recipes(), c.disabled(), c.modified());
            }, () -> {
                if (rightPanel != null) {
                    int rt = rightPanel.getSelectedTabIndex();
                    if (rt == 1) return UiKeys.TAB_TOOLTIP_ITEMS;
                    if (rt == 2) return UiKeys.TAB_TOOLTIP_BLOCKS;
                    if (rt == 3) return UiKeys.TAB_TOOLTIP_LOOT;
                }
                return UiKeys.TAB_TOOLTIP_RECIPES;
            });
            tabs[1].setCounts(() -> {
                if (rightPanel != null) {
                    int rt = rightPanel.getSelectedTabIndex();
                    if (rt == 1) return ItemStates.counts(true);
                    if (rt == 2) return BlockStates.counts(true);
                    if (rt == 3) return LootStates.counts(true);
                }
                RecipeIndex.RecipeCounts c = RecipeIndex.counts(true);
                return new TabCounts(c.recipes(), c.disabled(), c.modified());
            }, () -> {
                if (rightPanel != null) {
                    int rt = rightPanel.getSelectedTabIndex();
                    if (rt == 1) return UiKeys.TAB_TOOLTIP_ITEMS;
                    if (rt == 2) return UiKeys.TAB_TOOLTIP_BLOCKS;
                    if (rt == 3) return UiKeys.TAB_TOOLTIP_LOOT;
                }
                return UiKeys.TAB_TOOLTIP_RECIPES;
            });
        } else {
            String[] keys = new String[]{UiKeys.TAB_RECIPE, UiKeys.TAB_ITEMS, UiKeys.TAB_BLOCKS, UiKeys.TAB_LOOT};
            this.tabKeys = keys;
            this.tabs = null;
            int panelW = UiLayout.BODY_W - UiLayout.LEFT_PANEL_W - UiLayout.GAP;
            int areaW = Math.max(1, panelW - tabInset * 2);
            this.carouselTab = new CarouselTabWidget(tabInset, UiLayout.PANEL_INSET, areaW, tabH, keys, 0);
            carouselTab.setOnChanged(() -> {
                ScreenSession.lastRightTab = carouselTab.getSelectedIndex();
                if (tabChangedListener != null) tabChangedListener.run();
            });
            addWidget(carouselTab);
        }

        this.recipes = new RecipePanelSection(this);
        this.items = new ItemPanelSection(this);
        this.blocks = new BlockPanelSection(this);
        this.loot = new LootPanelSection(this);

        if (isLeft) {
            buildLeftContent();
        } else {
            buildRightContent();
        }
        this.saver = new RecipeSaver(this);
        this.itemSaver = new ItemSaver(this);
        this.blockSaver = new BlockSaver(this);
        this.lootSaver = new LootSaver(this);
    }

    private void buildLeftContent() {
        int innerW = getSizeWidth() - UiLayout.PANEL_INSET * 2;
        int innerTop = UiLayout.PANEL_INSET + UiLayout.TAB_H;
        int searchY = innerTop + UiLayout.SEARCH_GAP;

        searchField = new TextField(
                UiLayout.PANEL_INSET + UiLayout.LIST_INSET,
                searchY,
                innerW - UiLayout.LIST_INSET * 2,
                UiLayout.SEARCH_H,
                null,
                this::onSearchChanged);
        searchField.setClientSideWidget();
        searchField.setMaxStringLength(Integer.MAX_VALUE);
        searchField.setValidator(RecipeIndex::normalizeUserSearch);
        searchField.setBordered(false);
        searchField.setBackground(UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE));
        searchField.setTextColor(UiColors.TEXT_PRIMARY);
        searchField.setVisible(false);
        addWidget(searchField);

        int browserY = searchY + UiLayout.SEARCH_H + UiLayout.SEARCH_LIST_GAP;
        int browserH = getSizeHeight() - UiLayout.PANEL_INSET - browserY - UiLayout.SEARCH_GAP;
        recipeBrowser = new RecipeBrowserWidget(UiLayout.PANEL_INSET, browserY, innerW, browserH);
        recipeBrowser.setVisible(false);
        addWidget(recipeBrowser);
        itemBrowser = new ItemBrowserWidget(UiLayout.PANEL_INSET, browserY, innerW, browserH);
        itemBrowser.setVisible(false);
        addWidget(itemBrowser);
        blockBrowser = new BlockBrowserWidget(UiLayout.PANEL_INSET, browserY, innerW, browserH);
        blockBrowser.setVisible(false);
        addWidget(blockBrowser);
        lootBrowser = new LootBrowserWidget(UiLayout.PANEL_INSET, browserY, innerW, browserH);
        lootBrowser.setVisible(false);
        addWidget(lootBrowser);
    }

    private void buildRightContent() {
        int innerTop = UiLayout.PANEL_INSET + UiLayout.TAB_H;
        int searchY = innerTop + UiLayout.SEARCH_GAP;

        int leftAreaW = UiLayout.MACHINE_W;
        columnW = UiLayout.MACHINE_W - UiLayout.MACHINE_PAD * 2;
        columnX = UiLayout.PANEL_INSET + (leftAreaW - columnW) / 2;

        machineDropdown = new MachineDropdownWidget(
                columnX,
                searchY,
                columnW,
                UiLayout.SEARCH_H);
        machineDropdown.setClientSideWidget();
        addWidget(machineDropdown);

        modeLabelY = searchY + UiLayout.SEARCH_H + UiLayout.MACHINE_GAP;
        modeLabel = new TextTexture(
                () -> I18n.get(recipes.mode == EditMode.MODIFY ? UiKeys.MODE_MODIFY : UiKeys.MODE_NEW))
                .setType(TextTexture.TextType.LEFT_HIDE)
                .setWidth(columnW)
                .setColor(UiColors.TEXT_MUTED);

        int layoutY = modeLabelY + UiLayout.MODE_LABEL_H + UiLayout.MACHINE_GAP;
        int invY = UiLayout.inventoryY(getSizeHeight());
        int layoutH = invY - layoutY - UiLayout.MACHINE_GAP;
        machineLayout = new MachineLayoutWidget(
                columnX,
                layoutY + UiLayout.MACHINE_PAD,
                columnW,
                layoutH - UiLayout.MACHINE_PAD * 2);
        machineLayout.setClientSideWidget();
        machineLayout.setOutputsChangedListener(() -> {
            settingsWidget.setOutputRows(machineLayout.getOutputRows());
            settingsWidget.consumeSurfaceSlots(machineLayout.surfaceSlots());
        });
        addWidget(machineLayout);

        int settingsX = UiLayout.PANEL_INSET + UiLayout.MACHINE_W + UiLayout.AREA_GAP;
        int settingsH = invY + UiLayout.INV_H - searchY;
        settingsWidget = new RecipeSettingsWidget(
                settingsX,
                searchY,
                UiLayout.MACHINE_W,
                settingsH);
        settingsWidget.setClientSideWidget();
        settingsWidget.setOnClear(() -> {
            machineLayout.clearPhantoms();
            if (recipes.mode == EditMode.MODIFY && recipes.modifyTarget != null) {
                RecipeIndex.RecipeEntry target = recipes.modifyTarget;
                saver.sendRecipeEdit(RecipeEditAction.RESET, target.id(),
                            ScreenFactory.emptyPayload(target, getSelectedMachineUid()));
                recipes.exitModifyMode();
                recipes.showRecipe(target);
            } else {
                settingsWidget.applyValues(RecipeFieldValues.defaults());
                settingsWidget.setOutputRows(List.of());
                recipes.exitModifyMode();
            }
            settingsWidget.resetScroll();
        });
        settingsWidget.setOnSave(() -> saver.saveRecipe());
        settingsWidget.setGridSizeListener(() -> machineLayout
                .setGridSize(settingsWidget.gridWidthValue(), settingsWidget.gridHeightValue()));
        addWidget(settingsWidget);

        inventory = new PlayerInventoryWidget();
        inventory.setSelfPosition(new Position(UiLayout.PANEL_INSET + (leftAreaW - UiLayout.INV_W) / 2, invY));
        addWidget(inventory);

        itemTypeDropdown = new OptionDropdownWidget(columnX, searchY, columnW, UiLayout.SEARCH_H);
        itemTypeDropdown.setClientSideWidget();
        {
            List<String> opts = new ArrayList<>();
            opts.add("all");
            opts.addAll(ItemSettingsWidget.types());
            itemTypeDropdown.setOptions(opts);
            itemTypeDropdown.setLabelMapper(v -> "all".equals(v) ? "All" : v);
            itemTypeDropdown.setSelected("all");
        }
        itemTypeDropdown.setOnSelect(value -> {
            if ("all".equals(value)) {
                items.refreshItemPreview();
            } else {
                itemSettings.setType(value);
                items.refreshItemPreview();
            }
        });
        itemTypeDropdown.setVisible(false);
        addWidget(itemTypeDropdown);

        itemModeLabel = new TextTexture(
                () -> I18n.get(items.itemMode == EditMode.MODIFY ? UiKeys.MODE_MODIFY : UiKeys.MODE_NEW))
                .setType(TextTexture.TextType.LEFT_HIDE)
                .setWidth(columnW)
                .setColor(UiColors.TEXT_MUTED);

        itemPreview = new ItemPreviewWidget(
                columnX,
                layoutY,
                columnW,
                invY - layoutY - UiLayout.MACHINE_GAP - UiLayout.MACHINE_PAD);
        itemPreview.setVisible(false);
        addWidget(itemPreview);

        itemSettings = new ItemSettingsWidget(settingsX, searchY, UiLayout.MACHINE_W, settingsH);
        itemSettings.setClientSideWidget();
        itemSettings.setOnClear(() -> {
            if (items.itemMode == EditMode.MODIFY && items.itemModifyTarget != null) {
                ItemIndex.ItemEntry target = items.itemModifyTarget;
                itemSaver.send(ItemEditAction.RESET, target.id());
                items.exitItemModifyMode();
            }
            itemSettings.applyValues(ItemFieldValues.defaults());
            itemSettings.applyTags(List.of());
            itemSettings.applyActions(List.of());
            itemSettings.setType("basic");
            itemTypeDropdown.setSelected("basic");
            items.refreshItemPreview();
            itemSettings.setFields(itemSettings.fullFields());
            itemSettings.resetScroll();
        });
        itemSettings.setOnSave(() -> itemSaver.saveItem());
        itemSettings.setVisible(false);
        addWidget(itemSettings);

        blockTypeDropdown = new OptionDropdownWidget(columnX, searchY, columnW, UiLayout.SEARCH_H);
        blockTypeDropdown.setClientSideWidget();
        {
            List<String> opts = new ArrayList<>();
            opts.add("all");
            opts.addAll(BlockService.TYPES);
            blockTypeDropdown.setOptions(opts);
            blockTypeDropdown.setLabelMapper(v -> "all".equals(v) ? "All" : v);
            blockTypeDropdown.setSelected("all");
        }
        blockTypeDropdown.setOnSelect(value -> {
            if ("all".equals(value)) {
                blocks.refreshBlockPreview();
            } else {
                blockSettings.setType(value);
                blocks.refreshBlockPreview();
            }
        });
        blockTypeDropdown.setVisible(false);
        addWidget(blockTypeDropdown);

        blockModeLabel = new TextTexture(
                () -> I18n.get(blocks.blockMode == EditMode.MODIFY ? UiKeys.MODE_MODIFY : UiKeys.MODE_NEW))
                .setType(TextTexture.TextType.LEFT_HIDE)
                .setWidth(columnW)
                .setColor(UiColors.TEXT_MUTED);

        blockPreview = new BlockPreviewWidget(
                columnX,
                layoutY,
                columnW,
                invY - layoutY - UiLayout.MACHINE_GAP - UiLayout.MACHINE_PAD);
        blockPreview.setVisible(false);
        addWidget(blockPreview);

        blockSettings = new BlockSettingsWidget(settingsX, searchY, UiLayout.MACHINE_W, settingsH);
        blockSettings.setClientSideWidget();
        blockSettings.setOnClear(() -> {
            if (blocks.blockMode == EditMode.MODIFY && blocks.blockModifyTarget != null) {
                BlockIndex.BlockEntry target = blocks.blockModifyTarget;
                blockSaver.send(BlockEditAction.RESET, target.id());
                blocks.exitBlockModifyMode();
            }
            blockSettings.setBuiltInOnly(false);
            blockSettings.applyValues(BlockFieldValues.defaults());
            blockSettings.applyTags(List.of());
            blockSettings.applyActions(List.of());
            blockSettings.setType("basic");
            blockTypeDropdown.setSelected("basic");
            blocks.refreshBlockPreview();
            blockSettings.setFields(blockSettings.fullFields());
            blockSettings.resetScroll();
        });
        blockSettings.setOnSave(() -> blockSaver.saveBlock());
        blockSettings.setVisible(false);
        addWidget(blockSettings);

        lootTypeDropdown = new OptionDropdownWidget(columnX, searchY, columnW, UiLayout.SEARCH_H);
        lootTypeDropdown.setClientSideWidget();
        {
            List<String> opts = new ArrayList<>();
            opts.add("all");
            opts.add(LootService.LOOT_TYPE_BLOCK);
            opts.add(LootService.LOOT_TYPE_ENTITY);
            opts.add(LootService.LOOT_TYPE_CHEST);
            opts.add(LootService.LOOT_TYPE_FISHING);
            opts.add(LootService.LOOT_TYPE_GIFT);
            opts.add(LootService.LOOT_TYPE_GENERIC);
            lootTypeDropdown.setOptions(opts);
            lootTypeDropdown.setLabelMapper(v -> "all".equals(v) ? "All" : v);
            lootTypeDropdown.setSelected("all");
        }
        lootTypeDropdown.setVisible(false);
        addWidget(lootTypeDropdown);

        lootModeLabel = new TextTexture(
                () -> I18n.get(loot.lootMode == EditMode.MODIFY ? UiKeys.MODE_MODIFY : UiKeys.MODE_NEW))
                .setType(TextTexture.TextType.LEFT_HIDE)
                .setWidth(columnW)
                .setColor(UiColors.TEXT_MUTED);

        lootPreview = new LootPreviewWidget(
                columnX,
                layoutY,
                columnW,
                invY - layoutY - UiLayout.MACHINE_GAP - UiLayout.MACHINE_PAD);
        lootPreview.setVisible(false);
        addWidget(lootPreview);

        lootSettings = new LootSettingsWidget(settingsX, searchY, UiLayout.MACHINE_W, settingsH);
        lootSettings.setClientSideWidget();
        lootSettings.setClearHandler(() -> {
            if (loot.lootMode == EditMode.MODIFY && loot.lootModifyTarget != null) {
                ResourceLocation target = loot.lootModifyTarget;
                lootSaver.send(LootEditAction.RESET, target);
                loot.exitLootModifyMode();
            }
            lootSettings.applyValues(LootFieldValues.defaults());
            lootSettings.setLootType(LootService.LOOT_TYPE_BLOCK);
            loot.refreshLootPreview();
            lootSettings.setFields(List.of());
            lootSettings.resetScroll();
        });
        lootSettings.setSaveHandler(() -> lootSaver.saveLoot());
        lootSettings.setPreviewListener(this.loot::refreshLootPreview);
        lootSettings.setVisible(false);
        addWidget(lootSettings);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int px = getPositionX();
        int py = getPositionY();
        int pw = getSizeWidth();
        int ph = getSizeHeight();
        int panelInset = UiLayout.PANEL_INSET;
        int tabH = UiLayout.TAB_H;
        int innerTopY = py + panelInset + tabH;
        int innerH = ph - panelInset - tabH - panelInset;

        PANEL_TEXTURE.draw(g, mx, my, px, py, pw, ph);

        int innerX = px + panelInset;
        int innerW = pw - panelInset * 2;

        if (isLeft) {
            INNER_TEXTURE.draw(g, mx, my, innerX, innerTopY, innerW, innerH);
        } else {
            INNER_TEXTURE.draw(g, mx, my, innerX, innerTopY, UiLayout.MACHINE_W, innerH);
            INNER_TEXTURE.draw(g, mx, my,
                    innerX + UiLayout.MACHINE_W + UiLayout.AREA_GAP, innerTopY,
                    UiLayout.MACHINE_W, innerH);
        }

        if (isLeft && tabs != null) {
            for (Tab tab : tabs) {
                if (!tab.isTabActive()) {
                    continue;
                }
                int eraseX = tab.getPositionX() + 1;
                int eraseW = tab.getSizeWidth() - 2;
                if (eraseW > 0) {
                    TAB_ERASE_TEX.draw(g, mx, my, eraseX, innerTopY, eraseW, 1);
                }
            }
        } else if (!isLeft && carouselTab != null) {
            int eraseX = carouselTab.getPositionX() + 1;
            int eraseW = carouselTab.getSizeWidth() - 2;
            if (eraseW > 0) {
                TAB_ERASE_TEX.draw(g, mx, my, eraseX, innerTopY, eraseW, 1);
            }
        }

        if (!isLeft) {
            SEPARATOR_TOP_BORDER.draw(g, mx, my, innerX + UiLayout.MACHINE_W, innerTopY + 1, UiLayout.AREA_GAP, 1);
        }

        super.drawInBackground(g, mx, my, pt);

        if (!isLeft && modeLabel != null && machineDropdown.isVisible()) {
            modeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                    columnW, UiLayout.MODE_LABEL_H);
        }
        if (!isLeft && itemModeLabel != null && itemTypeDropdown.isVisible()) {
            itemModeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                    columnW, UiLayout.MODE_LABEL_H);
        }
        if (!isLeft && blockModeLabel != null && blockTypeDropdown.isVisible()) {
            blockModeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                    columnW, UiLayout.MODE_LABEL_H);
        }
        if (!isLeft && lootModeLabel != null && lootTypeDropdown.isVisible()) {
            lootModeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                    columnW, UiLayout.MODE_LABEL_H);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != UiColors.MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);

        if (isLeft && tabs != null) {
            for (int i = 0; i < tabs.length; i++) {
                if (!tabKeys[i].isBlank() && tabs[i].isMouseOverElement(mouseX, mouseY)) {
                    selectTab(i);
                    return true;
                }
            }
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return false;
    }

    private void selectTab(int index) {
        if (isLeft && tabs != null) {
            if (tabs[index].isTabActive()) return;
            for (Tab tab : tabs) tab.setTabActive(false);
            tabs[index].setTabActive(true);
                ScreenSession.lastLeftTab = index;
            if (tabChangedListener != null) tabChangedListener.run();
        } else if (!isLeft && carouselTab != null) {
            int prev = carouselTab.getSelectedIndex();
            if (prev == index) return;
            carouselTab.setSelectedIndex(index);
        }
    }

    void selectTabIndex(int index) {
        if (index < 0 || index >= tabKeys.length) return;
        if (!tabKeys[index].isBlank()) {
            selectTab(index);
        }
    }

    void restoreSearchQuery(String query) {
        if (searchField == null) {
            return;
        }
        searchField.setCurrentString(query == null ? "" : query);
        onSearchChanged(query == null ? "" : query);
    }

    public int getSelectedTabIndex() {
        if (isLeft && tabs != null) {
            for (int i = 0; i < tabs.length; i++) {
                if (tabs[i].isTabActive()) {
                    return i;
                }
            }
            return 0;
        } else if (!isLeft && carouselTab != null) {
            return carouselTab.getSelectedIndex();
        }
        return 0;
    }

    void setRightPanel(WorkspacePanel rightPanel) {
        this.rightPanel = rightPanel;
    }

    void setTabChangedListener(Runnable tabChangedListener) {
        this.tabChangedListener = tabChangedListener;
        if (!isLeft && carouselTab != null) {
            carouselTab.setOnChanged(() -> {
                ScreenSession.lastRightTab = carouselTab.getSelectedIndex();
                if (this.tabChangedListener != null) this.tabChangedListener.run();
            });
        }
    }

    void setMachineChangedListener(Runnable machineChangedListener) {
        this.machineChangedListener = machineChangedListener;
        machineDropdown.setOnMachineChanged(machine -> {
            machineLayout.setMachine(machine);
            if (machine != null) {
                    ScreenSession.lastMachineUid = machine.recipeTypeUid();
            }
            if (this.machineChangedListener != null) this.machineChangedListener.run();
        });
    }

    void refreshMachineSelection() {
        machineDropdown.refreshSelection();
    }

    void updateRecipeView() {
        if (isLeft) {
            boolean showRecipeView = rightPanel != null && rightPanel.getSelectedTabIndex() == 0;
            boolean showItemView = rightPanel != null && rightPanel.getSelectedTabIndex() == 1;
            boolean showBlockView = rightPanel != null && rightPanel.getSelectedTabIndex() == 2;
            boolean showLootView = rightPanel != null && rightPanel.getSelectedTabIndex() == 3;
            searchField.setVisible(showRecipeView || showItemView || showBlockView || showLootView);
            recipeBrowser.setVisible(showRecipeView);
            itemBrowser.setVisible(showItemView);
            blockBrowser.setVisible(showBlockView);
            lootBrowser.setVisible(showLootView);
            if (showRecipeView) {
                boolean kubejsOnly = getSelectedTabIndex() == 1;
                recipeBrowser.setKubejsOnly(kubejsOnly);
                recipeBrowser.setMachineFilter(rightPanel.getMachineRecipeIds());
                recipeBrowser.setMachineUid(rightPanel.getSelectedMachineUid());
                recipeBrowser.rebuild();
            }
            if (showItemView) {
                itemBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                String type = rightPanel != null && rightPanel.itemTypeDropdown != null ? rightPanel.itemTypeDropdown.getSelected() : null;
                itemBrowser.setTypeFilter(type);
                itemBrowser.rebuild();
            }
            if (showBlockView) {
                blockBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                String type = rightPanel != null && rightPanel.blockTypeDropdown != null ? rightPanel.blockTypeDropdown.getSelected() : null;
                blockBrowser.setTypeFilter(type);
                blockBrowser.rebuild();
            }
            if (showLootView) {
                lootBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                String type = rightPanel != null && rightPanel.lootTypeDropdown != null ? rightPanel.lootTypeDropdown.getSelected() : null;
                lootBrowser.setLootTypeFilter(type);
                lootBrowser.rebuild();
            }
        } else {
            boolean recipeTabActive = getSelectedTabIndex() == 0;
            machineDropdown.setVisible(recipeTabActive);
            machineLayout.setVisible(recipeTabActive);
            settingsWidget.setVisible(recipeTabActive);
            if (recipeTabActive) {
                MachineView machine = machineDropdown.getSelectedMachine();
                machineLayout.setMachine(machine);
                RecipeHandler support = machine == null ? null : MachineRegistry.get(machine.recipeTypeUid());
                settingsWidget.setFields(support == null ? List.of() : support.fields());
            }
            boolean itemTabActive = getSelectedTabIndex() == 1;
            itemTypeDropdown.setVisible(itemTabActive);
            itemPreview.setVisible(itemTabActive);
            itemSettings.setVisible(itemTabActive);
            if (itemTabActive) {
                itemSettings.setFields(itemSettings.fullFields());
            }
            boolean blockTabActive = getSelectedTabIndex() == 2;
            blockTypeDropdown.setVisible(blockTabActive);
            blockPreview.setVisible(blockTabActive);
            blockSettings.setVisible(blockTabActive);
            if (blockTabActive) {
                    blockSettings.setBuiltInOnly(blocks.blockModifyTarget != null && !blocks.blockModifyTarget.kubejs());
                blockSettings.setFields(blockSettings.fullFields());
            }
            boolean lootTabActive = getSelectedTabIndex() == 3;
            lootTypeDropdown.setVisible(lootTabActive);
            lootPreview.setVisible(lootTabActive);
            lootSettings.setVisible(lootTabActive);
            if (lootTabActive) {
                if (lootSettings != null) {
                    lootSettings.setFields(List.of());
                }
            }
        }
    }

    RecipeBrowserWidget getRecipeBrowser() {
        return recipeBrowser;
    }

    Set<ResourceLocation> getMachineRecipeIds() {
        MachineView machine = machineDropdown.getSelectedMachine();
        return machine == null ? null : MachineCatalog.recipeIds(machine);
    }

    ResourceLocation getSelectedMachineUid() {
        MachineView machine = machineDropdown.getSelectedMachine();
        return machine == null ? null : machine.recipeTypeUid();
    }

    MachineView getSelectedMachine() {
        return machineDropdown.getSelectedMachine();
    }

    private void onSearchChanged(String value) {
            ScreenSession.lastQuery = value == null ? "" : value;
        recipeBrowser.setQuery(value);
        if (itemBrowser != null) {
            itemBrowser.setQuery(value);
        }
        if (blockBrowser != null) {
            blockBrowser.setQuery(value);
        }
        if (lootBrowser != null) {
            lootBrowser.setQuery(value);
        }
    }

    ItemBrowserWidget getItemBrowser() {
        return itemBrowser;
    }

    BlockBrowserWidget getBlockBrowser() {
        return blockBrowser;
    }

    LootBrowserWidget getLootBrowser() {
        return lootBrowser;
    }

}
