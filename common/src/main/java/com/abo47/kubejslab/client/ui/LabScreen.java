package com.abo47.kubejslab.client.ui;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import com.abo47.kubejslab.client.ui.base.*;
import com.abo47.kubejslab.client.ui.assets.LabAssetPickerModal;
import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.block.model.LabBlockEditAction;
import com.abo47.kubejslab.block.model.LabBlockField;
import com.abo47.kubejslab.block.model.LabBlockFieldValues;
import com.abo47.kubejslab.block.model.LabBlockState;
import com.abo47.kubejslab.block.runtime.LabBlockService;
import com.abo47.kubejslab.item.model.LabItemEditAction;
import com.abo47.kubejslab.item.model.LabItemField;
import com.abo47.kubejslab.item.model.LabItemFieldValues;
import com.abo47.kubejslab.item.model.LabItemState;
import com.abo47.kubejslab.lab.LabPathResolver;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.LabRecipeMachines;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;


public final class LabScreen {
    private static final IGuiTexture ROOT_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE);
    private static final IGuiTexture PANEL_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL, LabColors.BORDER_BASE);
    private static final IGuiTexture INNER_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE);
    private static final ColorRectTexture TAB_ERASE_TEX = new ColorRectTexture(LabColors.SURFACE_BASE);

    private static int lastLeftTab;
    private static int lastRightTab;
    private static ResourceLocation lastMachineUid;
    private static String lastQuery = "";

    private LabScreen() {
    }

    static LabRecipePayload emptyPayload(LabRecipeIndex.LabRecipeEntry entry, ResourceLocation machineUid) {
        return new LabRecipePayload(machineUid, List.of(),
                List.of(new LabRecipeOutput.Item(entry.output(), 1f)), entry.name(),
                LabRecipeFieldValues.defaults());
    }

    public static ModularUI createUI(BlockPos holder, Player player) {
        LabRootWidget root = new LabRootWidget();
        LabPanelWidget leftPanel = new LabPanelWidget(true);
        LabPanelWidget rightPanel = new LabPanelWidget(false);

        root.setPanels(leftPanel, rightPanel);
        root.addWidget(leftPanel);
        root.addWidget(rightPanel);
        root.attachMenuLayer();
        LabPickerWindowWidget picker = LabPickerWindowWidget.create();
        picker.setPickListener(pick -> rightPanel.machineLayout.setPendingPick(pick));
        root.addWidget(picker);

        leftPanel.setRightPanel(rightPanel);
        Runnable updateViews = () -> {
            leftPanel.updateRecipeView();
            rightPanel.updateRecipeView();
        };
        leftPanel.setTabChangedListener(updateViews);
        rightPanel.setTabChangedListener(updateViews);
        leftPanel.getRecipeBrowser().setRecipeClickListener(entry -> {
            leftPanel.selectRecipe(entry);
            rightPanel.showRecipe(entry);
        });
        leftPanel.getRecipeBrowser().setRecipeRightClickListener(
                (entry, mouseX, mouseY) -> root.openContextMenu(entry, mouseX, mouseY));
        rightPanel.setMachineChangedListener(updateViews);
        rightPanel.settingsWidget.setCategoryContextRequester((option, mx, my) -> root.openActionsMenu(
                List.of(new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_DELETE), "delete", LabActionTone.DANGER,
                        () -> rightPanel.settingsWidget.deleteBlueprintCategory(option))), mx, my));
        rightPanel.settingsWidget.setMoldContextRequester((option, mx, my) -> root.openActionsMenu(
                List.of(new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_DELETE), "delete", LabActionTone.DANGER,
                        () -> rightPanel.settingsWidget.deleteCustomMold(option))), mx, my));

        WidgetGroup assetLayer = new WidgetGroup(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H);
        root.addWidget(assetLayer);
        rightPanel.itemSettings.setOnTexturePick(() -> {
            rightPanel.itemSettings.closeAllPopups();
            LabAssetPickerModal.open(assetLayer,
                    LabPathResolver.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures"),
                    I18n.get(LabGuiKeys.LAB_ITEM_TEXTURE),
                    path -> {
                        rightPanel.itemSettings.setTextureValue(path);
                        rightPanel.refreshItemPreview();
                    });
        });
        rightPanel.blockSettings.setOnTexturePick(field -> {
            rightPanel.blockSettings.closeAllPopups();
            LabAssetPickerModal.open(assetLayer,
                    LabPathResolver.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures"),
                    I18n.get(LabGuiKeys.LAB_ITEM_TEXTURE),
                    path -> {
                        rightPanel.blockSettings.setTextureValue(field, path);
                        rightPanel.refreshBlockPreview();
                    });
        });
        leftPanel.getItemBrowser().setItemClickListener(entry -> {
            leftPanel.selectItem(entry);
            rightPanel.showItemSettings(entry);
        });
        leftPanel.getItemBrowser().setItemRightClickListener(
                (entry, mouseX, mouseY) -> root.openItemContextMenu(entry, mouseX, mouseY));
        leftPanel.getBlockBrowser().setBlockClickListener(entry -> {
            leftPanel.selectBlock(entry);
            rightPanel.showBlockSettings(entry);
        });
        leftPanel.getBlockBrowser().setBlockRightClickListener(
                (entry, mouseX, mouseY) -> root.openBlockContextMenu(entry, mouseX, mouseY));

        return new ModularUI(root, IUIHolder.EMPTY, player);
    }

    public static void activateClient(ModularUI ui) {
        LabRootWidget root = (LabRootWidget) ui.mainGroup;
        LabPanelWidget leftPanel = root.getLeftPanel();
        LabPanelWidget rightPanel = root.getRightPanel();
        rightPanel.machineDropdown.selectMachineByUid(lastMachineUid);
        leftPanel.selectTabIndex(lastLeftTab);
        rightPanel.selectTabIndex(lastRightTab);
        leftPanel.restoreSearchQuery(lastQuery);
        leftPanel.updateRecipeView();
        rightPanel.updateRecipeView();
        rightPanel.refreshMachineSelection();
    }

    public static void refreshOpen() {
        if (!(Minecraft.getInstance().screen instanceof LabGuiContainer gui)) {
            return;
        }
        if (gui.modularUI.mainGroup instanceof LabRootWidget root) {
            root.getLeftPanel().updateRecipeView();
            root.getRightPanel().updateRecipeView();
        }
    }

    public static final class LabRootWidget extends WidgetGroup {
        private LabPanelWidget leftPanel;
        private LabPanelWidget rightPanel;
        private final WidgetGroup contextMenuLayer =
                new WidgetGroup(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H);
        private boolean menuOpen;
        private List<LabContextAction> menuActions = List.of();
        private int menuX;
        private int menuY;
        private int menuW;
        private int menuH;
        private long menuAnimStartMs;

        LabRootWidget() {
            super(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H);
        }

        void setPanels(LabPanelWidget leftPanel, LabPanelWidget rightPanel) {
            this.leftPanel = leftPanel;
            this.rightPanel = rightPanel;
        }

        void attachMenuLayer() {
            addWidget(contextMenuLayer);
        }

        LabPanelWidget getLeftPanel() {
            return leftPanel;
        }

        LabPanelWidget getRightPanel() {
            return rightPanel;
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
            ROOT_TEXTURE.draw(g, mx, my, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            super.drawInBackground(g, mx, my, pt);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (menuOpen && !menuHits(mouseX, mouseY)) {
                closeMenu();
            }
            boolean handled = super.mouseClicked(mouseX, mouseY, button);
            if (!handled && gui != null) {
                gui.getModularUIContainer().setCarried(ItemStack.EMPTY);
                return true;
            }
            return handled;
        }

        void openContextMenu(LabRecipeIndex.LabRecipeEntry entry, double mx, double my) {
            leftPanel.selectRecipe(entry);
            openActionsMenu(LabRecipeActions.forEntry(rightPanel, entry), mx, my);
        }

        void openItemContextMenu(LabItemIndex.LabItemEntry entry, double mx, double my) {
            rightPanel.selectItem(entry);
            openActionsMenu(LabItemActions.forEntry(entry, new LabItemActions.ItemActionCallbacks() {
                @Override
                public void openModify(LabItemIndex.LabItemEntry target) {
                    rightPanel.enterItemModifyMode(target);
                }

                @Override
                public void send(LabItemEditAction action, @Nonnull ResourceLocation targetId) {
                    rightPanel.itemSaver.send(action, targetId);
                }
            }), mx, my);
        }

        void openBlockContextMenu(LabBlockIndex.LabBlockEntry entry, double mx, double my) {
            rightPanel.selectBlock(entry);
            openActionsMenu(LabBlockActions.forEntry(entry, new LabBlockActions.BlockActionCallbacks() {
                @Override
                public void openModify(LabBlockIndex.LabBlockEntry target) {
                    rightPanel.enterBlockModifyMode(target);
                }

                @Override
                public void send(LabBlockEditAction action, @Nonnull ResourceLocation targetId) {
                    rightPanel.blockSaver.send(action, targetId);
                }
            }), mx, my);
        }

        void openActionsMenu(List<LabContextAction> actions, double mx, double my) {
            menuActions = actions;
            menuW = LabContextMenuPanel.menuWidth(actions);
            menuH = LabContextMenuPanel.menuHeight(actions);
            menuX = (int) Math.max(4, Math.min(mx - getPositionX(), LabLayout.ROOT_W - menuW - 4));
            menuY = (int) Math.max(4, Math.min(my - getPositionY(), LabLayout.ROOT_H - menuH - 4));
            menuOpen = true;
            menuAnimStartMs = System.currentTimeMillis();
            rebuildMenuLayer();
        }

        private boolean menuHits(double mouseX, double mouseY) {
            return menuOpen
                    && mouseX - getPositionX() >= menuX && mouseX - getPositionX() < menuX + menuW
                    && mouseY - getPositionY() >= menuY && mouseY - getPositionY() < menuY + menuH;
        }

        private void closeMenu() {
            if (!menuOpen) return;
            menuOpen = false;
            rebuildMenuLayer();
        }

        private void rebuildMenuLayer() {
            contextMenuLayer.clearAllWidgets();
            if (!menuOpen) return;
            contextMenuLayer.addWidget(LabContextMenuAnimation.wrap(
                    LabContextMenuPanel.build(menuX, menuY, menuActions, this::closeMenu),
                    () -> menuAnimStartMs));
        }
    }

    public static final class LabPanelWidget extends WidgetGroup {
        enum EditMode {
            NEW, MODIFY
        }

        private final boolean isLeft;
        private final LabTab[] tabs;
        private final String[] tabKeys;
        private LabPanelWidget rightPanel;
        private Runnable tabChangedListener;
        private Runnable machineChangedListener;
        private TextFieldWidget searchField;
        private LabRecipeBrowserWidget recipeBrowser;
        private LabItemBrowserWidget itemBrowser;
        private LabBlockBrowserWidget blockBrowser;
        LabMachineDropdownWidget machineDropdown;
        LabMachineLayoutWidget machineLayout;
        LabRecipeSettingsWidget settingsWidget;
        LabItemSettingsWidget itemSettings;
        LabOptionDropdownWidget itemTypeDropdown;
        LabItemPreviewWidget itemPreview;
        LabBlockSettingsWidget blockSettings;
        LabOptionDropdownWidget blockTypeDropdown;
        LabBlockPreviewWidget blockPreview;
        final LabRecipeSaver saver;
        final LabItemSaver itemSaver;
        final LabBlockSaver blockSaver;
        private PlayerInventoryWidget inventory;
        EditMode mode = EditMode.NEW;
        EditMode itemMode = EditMode.NEW;
        EditMode blockMode = EditMode.NEW;
        LabRecipeIndex.LabRecipeEntry modifyTarget;
        LabItemIndex.LabItemEntry itemModifyTarget;
        LabItemIndex.LabItemEntry itemSelection;
        LabBlockIndex.LabBlockEntry blockModifyTarget;
        LabBlockIndex.LabBlockEntry blockSelection;
        private TextTexture modeLabel;
        private TextTexture itemModeLabel;
        private TextTexture blockModeLabel;
        private int columnX;
        private int columnW;
        private int modeLabelY;

        LabPanelWidget(boolean isLeft) {
            super(
                    isLeft ? LabLayout.BODY_X : LabLayout.BODY_X + LabLayout.LEFT_PANEL_W + LabLayout.GAP,
                    LabLayout.BODY_Y,
                    isLeft ? LabLayout.LEFT_PANEL_W : LabLayout.BODY_W - LabLayout.LEFT_PANEL_W - LabLayout.GAP,
                    LabLayout.BODY_H);
            this.isLeft = isLeft;

            int tabInset = LabLayout.TAB_INSET;
            int tabH = LabLayout.TAB_H;
            int tabGap = LabLayout.TAB_GAP;

            String[] keys = isLeft
                    ? new String[]{LabGuiKeys.TAB_BUILT_IN, LabGuiKeys.TAB_CUSTOM}
                    : new String[]{LabGuiKeys.TAB_RECIPE, LabGuiKeys.TAB_ITEMS, LabGuiKeys.TAB_BLOCKS, ""};
            this.tabs = new LabTab[keys.length];
            this.tabKeys = keys;

            int tabCount = keys.length;
            int totalTabGap = tabGap * (tabCount - 1);
            int panelW = isLeft ? LabLayout.LEFT_PANEL_W : (LabLayout.BODY_W - LabLayout.LEFT_PANEL_W - LabLayout.GAP);
            int areaW = Math.max(1, panelW - tabInset * 2);
            int baseW = (areaW - totalTabGap) / tabCount;
            int remainder = (areaW - totalTabGap) % tabCount;
            int tabX = tabInset;

            for (int i = 0; i < keys.length; i++) {
                int w = baseW + (i < remainder ? 1 : 0);
                tabs[i] = new LabTab(tabX, LabLayout.PANEL_INSET, w, tabH, keys[i], i == 0);
                addWidget(tabs[i]);
                tabX += w + tabGap;
            }

            if (isLeft) {
                tabs[0].setRecipeCategory(false);
                tabs[1].setRecipeCategory(true);
            }

            if (isLeft) {
                buildLeftContent();
            } else {
                buildRightContent();
            }
            this.saver = new LabRecipeSaver(this);
            this.itemSaver = new LabItemSaver(this);
            this.blockSaver = new LabBlockSaver(this);
        }

        private void buildLeftContent() {
            int innerW = getSizeWidth() - LabLayout.PANEL_INSET * 2;
            int innerTop = LabLayout.PANEL_INSET + LabLayout.TAB_H;
            int searchY = innerTop + LabLayout.SEARCH_GAP;

            searchField = new TextFieldWidget(
                    LabLayout.PANEL_INSET + LabLayout.LIST_INSET,
                    searchY,
                    innerW - LabLayout.LIST_INSET * 2,
                    LabLayout.SEARCH_H,
                    null,
                    this::onSearchChanged);
            searchField.setClientSideWidget();
            searchField.setMaxStringLength(Integer.MAX_VALUE);
            searchField.setValidator(LabRecipeIndex::normalizeUserSearch);
            searchField.setBordered(false);
            searchField.setBackground(LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE));
            searchField.setTextColor(LabColors.TEXT_PRIMARY);
            searchField.setVisible(false);
            addWidget(searchField);

            int browserY = searchY + LabLayout.SEARCH_H + LabLayout.SEARCH_LIST_GAP;
            int browserH = getSizeHeight() - LabLayout.PANEL_INSET - browserY - LabLayout.SEARCH_GAP;
            recipeBrowser = new LabRecipeBrowserWidget(LabLayout.PANEL_INSET, browserY, innerW, browserH);
            recipeBrowser.setVisible(false);
            addWidget(recipeBrowser);
            itemBrowser = new LabItemBrowserWidget(LabLayout.PANEL_INSET, browserY, innerW, browserH);
            itemBrowser.setVisible(false);
            addWidget(itemBrowser);
            blockBrowser = new LabBlockBrowserWidget(LabLayout.PANEL_INSET, browserY, innerW, browserH);
            blockBrowser.setVisible(false);
            addWidget(blockBrowser);
        }

        private void buildRightContent() {
            int innerTop = LabLayout.PANEL_INSET + LabLayout.TAB_H;
            int searchY = innerTop + LabLayout.SEARCH_GAP;

            int leftAreaW = LabLayout.MACHINE_W;
            columnW = LabLayout.MACHINE_W - LabLayout.MACHINE_PAD * 2;
            columnX = LabLayout.PANEL_INSET + (leftAreaW - columnW) / 2;

            machineDropdown = new LabMachineDropdownWidget(
                    columnX,
                    searchY,
                    columnW,
                    LabLayout.SEARCH_H);
            machineDropdown.setClientSideWidget();
            addWidget(machineDropdown);

            modeLabelY = searchY + LabLayout.SEARCH_H + LabLayout.MACHINE_GAP;
            modeLabel = new TextTexture(
                    () -> I18n.get(mode == EditMode.MODIFY ? LabGuiKeys.LAB_MODE_MODIFY : LabGuiKeys.LAB_MODE_NEW))
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(columnW)
                    .setColor(LabColors.TEXT_MUTED);

            int layoutY = modeLabelY + LabLayout.MODE_LABEL_H + LabLayout.MACHINE_GAP;
            int invY = LabLayout.inventoryY(getSizeHeight());
            int layoutH = invY - layoutY - LabLayout.MACHINE_GAP;
            machineLayout = new LabMachineLayoutWidget(
                    columnX,
                    layoutY + LabLayout.MACHINE_PAD,
                    columnW,
                    layoutH - LabLayout.MACHINE_PAD * 2);
            machineLayout.setClientSideWidget();
            machineLayout.setOutputsChangedListener(() -> {
                settingsWidget.setOutputRows(machineLayout.getOutputRows());
                settingsWidget.consumeSurfaceSlots(machineLayout.surfaceSlots());
            });
            addWidget(machineLayout);

            int settingsX = LabLayout.PANEL_INSET + LabLayout.MACHINE_W + LabLayout.AREA_GAP;
            int settingsH = invY + LabLayout.INV_H - searchY;
            settingsWidget = new LabRecipeSettingsWidget(
                    settingsX,
                    searchY,
                    LabLayout.MACHINE_W,
                    settingsH);
            settingsWidget.setClientSideWidget();
            settingsWidget.setOnClear(() -> {
                machineLayout.clearPhantoms();
                if (mode != EditMode.MODIFY || modifyTarget == null) return;
                LabRecipeIndex.LabRecipeEntry target = modifyTarget;
                saver.sendRecipeEdit(LabRecipeEditAction.RESET, target.id(),
                        emptyPayload(target, getSelectedMachineUid()));
                exitModifyMode();
                showRecipe(target);
            });
            settingsWidget.setOnSave(() -> saver.saveRecipe());
            settingsWidget.setGridSizeListener(() -> machineLayout
                    .setGridSize(settingsWidget.gridWidthValue(), settingsWidget.gridHeightValue()));
            addWidget(settingsWidget);

            inventory = new PlayerInventoryWidget();
            inventory.setSelfPosition(new Position(LabLayout.PANEL_INSET + (leftAreaW - LabLayout.INV_W) / 2, invY));
            addWidget(inventory);

            itemTypeDropdown = new LabOptionDropdownWidget(columnX, searchY, columnW, LabLayout.SEARCH_H);
            itemTypeDropdown.setClientSideWidget();
            itemTypeDropdown.setOptions(LabItemSettingsWidget.types());
            itemTypeDropdown.setOnSelect(value -> {
                itemSettings.setType(value);
                refreshItemPreview();
            });
            itemTypeDropdown.setVisible(false);
            addWidget(itemTypeDropdown);

            itemModeLabel = new TextTexture(
                    () -> I18n.get(itemMode == EditMode.MODIFY ? LabGuiKeys.LAB_MODE_MODIFY : LabGuiKeys.LAB_MODE_NEW))
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(columnW)
                    .setColor(LabColors.TEXT_MUTED);

            itemPreview = new LabItemPreviewWidget(
                    columnX,
                    layoutY,
                    columnW,
                    invY - layoutY - LabLayout.MACHINE_GAP - LabLayout.MACHINE_PAD);
            itemPreview.setVisible(false);
            addWidget(itemPreview);

            itemSettings = new LabItemSettingsWidget(settingsX, searchY, LabLayout.MACHINE_W, settingsH);
            itemSettings.setClientSideWidget();
            itemSettings.setOnClear(() -> {
                if (itemMode == EditMode.MODIFY && itemModifyTarget != null) {
                    LabItemIndex.LabItemEntry target = itemModifyTarget;
                    itemSaver.send(LabItemEditAction.RESET, target.id());
                    exitItemModifyMode();
                }
                itemSettings.applyValues(LabItemFieldValues.defaults());
                itemSettings.applyTags(List.of());
                itemSettings.applyActions(List.of());
                itemSettings.setType("basic");
                itemTypeDropdown.setSelected("basic");
                refreshItemPreview();
            });
            itemSettings.setOnSave(() -> itemSaver.saveItem());
            itemSettings.setVisible(false);
            addWidget(itemSettings);

            blockTypeDropdown = new LabOptionDropdownWidget(columnX, searchY, columnW, LabLayout.SEARCH_H);
            blockTypeDropdown.setClientSideWidget();
            blockTypeDropdown.setOptions(LabBlockService.TYPES);
            blockTypeDropdown.setOnSelect(value -> {
                blockSettings.setType(value);
                refreshBlockPreview();
            });
            blockTypeDropdown.setVisible(false);
            addWidget(blockTypeDropdown);

            blockModeLabel = new TextTexture(
                    () -> I18n.get(blockMode == EditMode.MODIFY ? LabGuiKeys.LAB_MODE_MODIFY : LabGuiKeys.LAB_MODE_NEW))
                    .setType(TextTexture.TextType.LEFT_HIDE)
                    .setWidth(columnW)
                    .setColor(LabColors.TEXT_MUTED);

            blockPreview = new LabBlockPreviewWidget(
                    columnX,
                    layoutY,
                    columnW,
                    invY - layoutY - LabLayout.MACHINE_GAP - LabLayout.MACHINE_PAD);
            blockPreview.setVisible(false);
            addWidget(blockPreview);

            blockSettings = new LabBlockSettingsWidget(settingsX, searchY, LabLayout.MACHINE_W, settingsH);
            blockSettings.setClientSideWidget();
            blockSettings.setOnClear(() -> {
                if (blockMode == EditMode.MODIFY && blockModifyTarget != null) {
                    LabBlockIndex.LabBlockEntry target = blockModifyTarget;
                    blockSaver.send(LabBlockEditAction.RESET, target.id());
                    exitBlockModifyMode();
                }
                blockSettings.applyValues(LabBlockFieldValues.defaults());
                blockSettings.applyTags(List.of());
                blockSettings.applyActions(List.of());
                blockSettings.setType("basic");
                blockTypeDropdown.setSelected("basic");
                refreshBlockPreview();
            });
            blockSettings.setOnSave(() -> blockSaver.saveBlock());
            blockSettings.setVisible(false);
            addWidget(blockSettings);
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
            int px = getPositionX();
            int py = getPositionY();
            int pw = getSizeWidth();
            int ph = getSizeHeight();
            int panelInset = LabLayout.PANEL_INSET;
            int tabH = LabLayout.TAB_H;
            int innerTopY = py + panelInset + tabH;
            int innerH = ph - panelInset - tabH - panelInset;

            PANEL_TEXTURE.draw(g, mx, my, px, py, pw, ph);

            int innerX = px + panelInset;
            int innerW = pw - panelInset * 2;

            if (isLeft) {
                INNER_TEXTURE.draw(g, mx, my, innerX, innerTopY, innerW, innerH);
            } else {
                INNER_TEXTURE.draw(g, mx, my, innerX, innerTopY, LabLayout.MACHINE_W, innerH);
                INNER_TEXTURE.draw(g, mx, my,
                        innerX + LabLayout.MACHINE_W + LabLayout.AREA_GAP, innerTopY,
                        LabLayout.MACHINE_W, innerH);
            }

            for (LabTab tab : tabs) {
                if (!tab.isTabActive()) {
                    continue;
                }
                int eraseX = tab.getPositionX() + 1;
                int eraseW = tab.getSizeWidth() - 2;
                if (eraseW > 0) {
                    TAB_ERASE_TEX.draw(g, mx, my, eraseX, innerTopY, eraseW, 1);
                }
            }

            super.drawInBackground(g, mx, my, pt);

            if (!isLeft && modeLabel != null && machineDropdown.isVisible()) {
                modeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                        columnW, LabLayout.MODE_LABEL_H);
            }
            if (!isLeft && itemModeLabel != null && itemTypeDropdown.isVisible()) {
                itemModeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                        columnW, LabLayout.MODE_LABEL_H);
            }
            if (!isLeft && blockModeLabel != null && blockTypeDropdown.isVisible()) {
                blockModeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                        columnW, LabLayout.MODE_LABEL_H);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != LabColors.MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);

            for (int i = 0; i < tabs.length; i++) {
                if (!tabKeys[i].isBlank() && tabs[i].isMouseOverElement(mouseX, mouseY)) {
                    selectTab(i);
                    return true;
                }
            }

            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return false;
        }

        private void selectTab(int index) {
            if (tabs[index].isTabActive()) return;
            for (LabTab tab : tabs) tab.setTabActive(false);
            tabs[index].setTabActive(true);
            if (isLeft) {
                lastLeftTab = index;
            } else {
                lastRightTab = index;
            }
            if (tabChangedListener != null) tabChangedListener.run();
        }

        void selectTabIndex(int index) {
            if (index >= 0 && index < tabs.length && !tabKeys[index].isBlank()) {
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
            for (int i = 0; i < tabs.length; i++) {
                if (tabs[i].isTabActive()) {
                    return i;
                }
            }
            return 0;
        }

        void setRightPanel(LabPanelWidget rightPanel) {
            this.rightPanel = rightPanel;
        }

        void setTabChangedListener(Runnable tabChangedListener) {
            this.tabChangedListener = tabChangedListener;
        }

        void setMachineChangedListener(Runnable machineChangedListener) {
            this.machineChangedListener = machineChangedListener;
            machineDropdown.setOnMachineChanged(machine -> {
                machineLayout.setMachine(machine);
                if (machine != null) {
                    lastMachineUid = machine.recipeTypeUid();
                }
                if (this.machineChangedListener != null) this.machineChangedListener.run();
            });
        }

        void refreshMachineSelection() {
            machineDropdown.refreshSelection();
        }

        void selectRecipe(LabRecipeIndex.LabRecipeEntry entry) {
            if (recipeBrowser == null) {
                return;
            }
            recipeBrowser.setSelectedRecipeId(entry.id());
        }

        void enterModifyMode(LabRecipeIndex.LabRecipeEntry entry) {
            mode = EditMode.MODIFY;
            modifyTarget = entry;
            refreshModeLabel();
            ResourceLocation uid = saver.resolveModifyUid(entry);
            if (uid != null) {
                machineDropdown.selectMachineByUid(uid);
            }
            showRecipe(entry);
            if (uid == null) {
                settingsWidget.setFields(List.of());
            }
        }

        void exitModifyMode() {
            if (mode != EditMode.MODIFY) return;
            mode = EditMode.NEW;
            modifyTarget = null;
            refreshModeLabel();
        }

        void exitModifyModeIfTarget(LabRecipeIndex.LabRecipeEntry entry) {
            if (modifyTarget != null && modifyTarget.id().equals(entry.id())) {
                exitModifyMode();
            }
        }

        private void refreshModeLabel() {
            if (modeLabel == null) return;
            modeLabel.setColor(mode == EditMode.MODIFY ? LabColors.INTERACTIVE : LabColors.TEXT_MUTED);
        }

        private void updateRecipeView() {
            if (isLeft) {
                boolean showRecipeView = rightPanel != null && rightPanel.getSelectedTabIndex() == 0;
                boolean showItemView = rightPanel != null && rightPanel.getSelectedTabIndex() == 1;
                boolean showBlockView = rightPanel != null && rightPanel.getSelectedTabIndex() == 2;
                searchField.setVisible(showRecipeView || showItemView || showBlockView);
                recipeBrowser.setVisible(showRecipeView);
                itemBrowser.setVisible(showItemView);
                blockBrowser.setVisible(showBlockView);
                if (showRecipeView) {
                    recipeBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                    recipeBrowser.setMachineFilter(rightPanel.getMachineRecipeIds());
                    recipeBrowser.setMachineUid(rightPanel.getSelectedMachineUid());
                    recipeBrowser.rebuild();
                }
                if (showItemView) {
                    itemBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                    itemBrowser.rebuild();
                }
                if (showBlockView) {
                    blockBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                    blockBrowser.rebuild();
                }
            } else {
                boolean recipeTabActive = getSelectedTabIndex() == 0;
                machineDropdown.setVisible(recipeTabActive);
                machineLayout.setVisible(recipeTabActive);
                settingsWidget.setVisible(recipeTabActive);
                if (recipeTabActive) {
                    LabMachine machine = machineDropdown.getSelectedMachine();
                    machineLayout.setMachine(machine);
                    LabRecipeMachine support = machine == null ? null : LabRecipeMachines.get(machine.recipeTypeUid());
                    settingsWidget.setFields(support == null ? List.of() : support.fields());
                }
                boolean itemTabActive = getSelectedTabIndex() == 1;
                itemTypeDropdown.setVisible(itemTabActive);
                itemPreview.setVisible(itemTabActive);
                itemSettings.setVisible(itemTabActive);
                if (itemTabActive) {
                    List<LabItemField> fields = itemModifyTarget == null
                            ? itemSettings.fullFields()
                            : (itemModifyTarget.kubejs() ? itemSettings.fullFields() : itemSettings.builtInFields());
                    itemSettings.setFields(fields);
                }
                boolean blockTabActive = getSelectedTabIndex() == 2;
                blockTypeDropdown.setVisible(blockTabActive);
                blockPreview.setVisible(blockTabActive);
                blockSettings.setVisible(blockTabActive);
                if (blockTabActive) {
                    List<LabBlockField> fields = blockModifyTarget == null
                            ? blockSettings.fullFields()
                            : (blockModifyTarget.kubejs() ? blockSettings.fullFields() : blockSettings.builtInFields());
                    blockSettings.setFields(fields);
                }
            }
        }

        LabRecipeBrowserWidget getRecipeBrowser() {
            return recipeBrowser;
        }

        Set<ResourceLocation> getMachineRecipeIds() {
            LabMachine machine = machineDropdown.getSelectedMachine();
            return machine == null ? null : LabMachineCatalog.recipeIds(machine);
        }

        ResourceLocation getSelectedMachineUid() {
            LabMachine machine = machineDropdown.getSelectedMachine();
            return machine == null ? null : machine.recipeTypeUid();
        }

        LabMachine getSelectedMachine() {
            return machineDropdown.getSelectedMachine();
        }

        void selectItem(LabItemIndex.LabItemEntry entry) {
            if (itemBrowser == null) {
                return;
            }
            itemBrowser.setSelectedItemId(entry.id());
        }

        void enterItemModifyMode(LabItemIndex.LabItemEntry entry) {
            itemMode = EditMode.MODIFY;
            itemModifyTarget = entry;
            showItemSettings(entry);
        }

        void exitItemModifyMode() {
            if (itemMode != EditMode.MODIFY) return;
            itemMode = EditMode.NEW;
            itemModifyTarget = null;
            refreshItemModeLabel();
        }

        void exitItemModifyModeIfTarget(LabItemIndex.LabItemEntry entry) {
            if (itemModifyTarget != null && itemModifyTarget.id().equals(entry.id())) {
                exitItemModifyMode();
            }
        }

        void showItemSettings(LabItemIndex.LabItemEntry entry) {
            itemSelection = entry;
            List<LabItemField> fields = entry.kubejs()
                    ? itemSettings.fullFields()
                    : itemSettings.builtInFields();
            itemSettings.setFields(fields);
            LabItemState state = LabItemStates.stateOf(entry.id());
            String type = state != null && state.type() != null && !state.type().isBlank()
                    ? state.type()
                    : LabItemIndex.typeOf(entry.id());
            itemSettings.setType(type);
            itemTypeDropdown.setSelected(type);
            if (state != null) {
                itemSettings.applyValues(state.values());
                itemSettings.applyTags(state.tags());
                itemSettings.applyActions(state.actions());
            } else {
                itemSettings.applyValues(LabItemIndex.prefillValues(entry.id()));
                itemSettings.applyTags(List.of());
                itemSettings.applyActions(List.of());
            }
            refreshItemModeLabel();
            refreshItemPreview();
        }

        private void refreshItemModeLabel() {
            if (itemModeLabel == null) return;
            itemModeLabel.setColor(itemMode == EditMode.MODIFY ? LabColors.INTERACTIVE : LabColors.TEXT_MUTED);
        }

        private void refreshItemPreview() {
            if (itemSettings == null || itemPreview == null) {
                return;
            }
            ItemStack previewStack = itemSelection != null ? itemSelection.stack() : ItemStack.EMPTY;
            itemPreview.setItem(itemSettings.getType(), previewStack);
            itemPreview.setTexture(itemSettings.getTexture());
        }

        private void showRecipe(LabRecipeIndex.LabRecipeEntry entry) {
            machineLayout.showRecipe(entry);
            LabMachine machine = machineDropdown.getSelectedMachine();
            if (machine == null) {
                return;
            }
            LabRecipeMachine support = LabRecipeMachines.get(machine.recipeTypeUid());
            if (support != null) {
                Recipe<?> original = LabRecipeIndex.recipeById(entry.id());
                settingsWidget.applyValues(support.prefill(settingsWidget.getValues(), original));
                machineLayout.setGridSize(settingsWidget.gridWidthValue(), settingsWidget.gridHeightValue());
                if (support.supportsFluidOutputAmount()) {
                    int amount = 0;
                    for (LabRecipeOutput output : machineLayout.getOutputs()) {
                        if (output instanceof LabRecipeOutput.Fluid fluid) {
                            amount = (int) fluid.fluid().getAmount();
                            break;
                        }
                    }
                    settingsWidget.setFluidOutputAmount(amount);
                }
            }
        }

        private void onSearchChanged(String value) {
            lastQuery = value == null ? "" : value;
            recipeBrowser.setQuery(value);
            if (itemBrowser != null) {
                itemBrowser.setQuery(value);
            }
            if (blockBrowser != null) {
                blockBrowser.setQuery(value);
            }
        }

        LabItemBrowserWidget getItemBrowser() {
            return itemBrowser;
        }

        LabBlockBrowserWidget getBlockBrowser() {
            return blockBrowser;
        }

        void selectBlock(LabBlockIndex.LabBlockEntry entry) {
            if (blockBrowser == null) {
                return;
            }
            blockBrowser.setSelectedBlockId(entry.id());
        }

        void enterBlockModifyMode(LabBlockIndex.LabBlockEntry entry) {
            blockMode = EditMode.MODIFY;
            blockModifyTarget = entry;
            showBlockSettings(entry);
        }

        void exitBlockModifyMode() {
            if (blockMode != EditMode.MODIFY) return;
            blockMode = EditMode.NEW;
            blockModifyTarget = null;
            refreshBlockModeLabel();
        }

        void showBlockSettings(LabBlockIndex.LabBlockEntry entry) {
            blockSelection = entry;
            List<LabBlockField> fields = entry.kubejs()
                    ? blockSettings.fullFields()
                    : blockSettings.builtInFields();
            blockSettings.setFields(fields);
            LabBlockState state = LabBlockStates.stateOf(entry.id());
            String type = state != null && state.type() != null && !state.type().isBlank()
                    ? state.type()
                    : LabBlockIndex.typeOf(entry.id());
            blockSettings.setType(type);
            blockTypeDropdown.setSelected(type);
            if (state != null) {
                blockSettings.applyValues(state.values());
                blockSettings.applyTags(state.tags());
                blockSettings.applyActions(state.actions());
            } else {
                blockSettings.applyValues(LabBlockIndex.prefillValues(entry.id()));
                blockSettings.applyTags(List.of());
                blockSettings.applyActions(List.of());
            }
            refreshBlockModeLabel();
            refreshBlockPreview();
        }

        private void refreshBlockModeLabel() {
            if (blockModeLabel == null) return;
            blockModeLabel.setColor(blockMode == EditMode.MODIFY ? LabColors.INTERACTIVE : LabColors.TEXT_MUTED);
        }

        private void refreshBlockPreview() {
            if (blockSettings == null || blockPreview == null) {
                return;
            }
            ItemStack previewStack = blockSelection != null ? blockSelection.stack() : ItemStack.EMPTY;
            blockPreview.setBlock(blockSettings.getType(), previewStack);
            blockPreview.setTexture(blockSettings.getAllTexture());
        }
    }
}

