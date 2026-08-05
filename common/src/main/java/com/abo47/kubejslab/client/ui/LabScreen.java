package com.abo47.kubejslab.client.ui;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

public final class LabScreen {
    static final Set<ResourceLocation> NEW_RECIPE_IDS = new HashSet<>();
    private static final IGuiTexture ROOT_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE);
    private static final IGuiTexture PANEL_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL, LabColors.BORDER_BASE);
    private static final IGuiTexture INNER_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE);
    private static final ColorRectTexture DIVIDER_TEX = new ColorRectTexture(LabColors.BORDER_BASE);
    private static final ColorRectTexture DIVIDER_FILL_TEX = new ColorRectTexture(LabColors.SURFACE_PANEL);
    private static final ColorRectTexture TAB_ERASE_TEX = new ColorRectTexture(LabColors.SURFACE_BASE);

    private LabScreen() {
    }

    public static ModularUI createUI(BlockPos holder, Player player) {
        LabRootWidget root = new LabRootWidget();
        LabPanelWidget leftPanel = new LabPanelWidget(true);
        LabPanelWidget rightPanel = new LabPanelWidget(false);

        root.setPanels(leftPanel, rightPanel);
        root.addWidget(leftPanel);
        root.addWidget(rightPanel);

        leftPanel.setRightPanel(rightPanel);
        Runnable updateViews = () -> {
            leftPanel.updateRecipeView();
            rightPanel.updateRecipeView();
        };
        leftPanel.setTabChangedListener(updateViews);
        rightPanel.setTabChangedListener(updateViews);
        leftPanel.getRecipeBrowser().setRecipeClickListener(rightPanel::showRecipe);
        rightPanel.setMachineChangedListener(updateViews);

        return new ModularUI(root, IUIHolder.EMPTY, player);
    }

    public static void activateClient(ModularUI ui) {
        LabRootWidget root = (LabRootWidget) ui.mainGroup;
        LabPanelWidget leftPanel = root.getLeftPanel();
        LabPanelWidget rightPanel = root.getRightPanel();
        leftPanel.updateRecipeView();
        rightPanel.updateRecipeView();
        rightPanel.refreshMachineSelection();
    }

    public static final class LabRootWidget extends WidgetGroup {
        private LabPanelWidget leftPanel;
        private LabPanelWidget rightPanel;

        LabRootWidget() {
            super(0, 0, LabLayout.ROOT_W, LabLayout.ROOT_H);
        }

        void setPanels(LabPanelWidget leftPanel, LabPanelWidget rightPanel) {
            this.leftPanel = leftPanel;
            this.rightPanel = rightPanel;
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
    }

    public static final class LabPanelWidget extends WidgetGroup {
        private final boolean isLeft;
        private final LabTab[] tabs;
        private LabPanelWidget rightPanel;
        private Runnable tabChangedListener;
        private Runnable machineChangedListener;
        private TextFieldWidget searchField;
        private LabRecipeBrowserWidget recipeBrowser;
        private LabMachineDropdownWidget machineDropdown;
        private LabMachineLayoutWidget machineLayout;
        private LabRecipeSettingsWidget settingsWidget;
        private PlayerInventoryWidget inventory;

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
                    : new String[]{LabGuiKeys.TAB_RECIPE, "", "", ""};
            this.tabs = new LabTab[keys.length];

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
                buildLeftContent();
            } else {
                buildRightContent();
            }
        }

        private void buildLeftContent() {
            int innerW = getSizeWidth() - LabLayout.PANEL_INSET * 2;
            int innerTop = LabLayout.PANEL_INSET + LabLayout.TAB_H;
            int searchY = innerTop + LabLayout.SEARCH_GAP;

            searchField = new TextFieldWidget(
                    LabLayout.PANEL_INSET + LabLayout.LIST_INSET,
                    searchY,
                    LabLayout.recipeCardWidth(innerW),
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
        }

        private void buildRightContent() {
            int innerTop = LabLayout.PANEL_INSET + LabLayout.TAB_H;
            int searchY = innerTop + LabLayout.SEARCH_GAP;

            int leftAreaW = LabLayout.MACHINE_W;
            int columnW = LabLayout.MACHINE_W - LabLayout.MACHINE_PAD * 2;
            int columnX = LabLayout.PANEL_INSET + (leftAreaW - columnW) / 2;

            machineDropdown = new LabMachineDropdownWidget(
                    columnX,
                    searchY,
                    columnW,
                    LabLayout.SEARCH_H);
            machineDropdown.setClientSideWidget();
            addWidget(machineDropdown);

            int layoutY = searchY + LabLayout.SEARCH_H + LabLayout.MACHINE_GAP;
            int invY = LabLayout.inventoryY(getSizeHeight());
            int layoutH = invY - layoutY - LabLayout.MACHINE_GAP;
            machineLayout = new LabMachineLayoutWidget(
                    columnX,
                    layoutY + LabLayout.MACHINE_PAD,
                    columnW,
                    layoutH - LabLayout.MACHINE_PAD * 2);
            machineLayout.setClientSideWidget();
            addWidget(machineLayout);

            int settingsX = LabLayout.PANEL_INSET + LabLayout.MACHINE_W + LabLayout.AREA_GAP;
            int settingsH = invY + LabLayout.INV_H - searchY;
            settingsWidget = new LabRecipeSettingsWidget(
                    settingsX,
                    searchY,
                    LabLayout.MACHINE_W,
                    settingsH);
            settingsWidget.setClientSideWidget();
            settingsWidget.setOnClear(() -> machineLayout.clearPhantoms());
            settingsWidget.setOnSave(this::saveRecipe);
            addWidget(settingsWidget);

            inventory = new PlayerInventoryWidget();
            inventory.setSelfPosition(new Position(LabLayout.PANEL_INSET + (leftAreaW - LabLayout.INV_W) / 2, invY));
            addWidget(inventory);
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
            INNER_TEXTURE.draw(g, mx, my, innerX, innerTopY, innerW, innerH);

            if (!isLeft) {
                int dividerX = innerX + LabLayout.MACHINE_W + LabLayout.AREA_GAP / 2 - 3;
                DIVIDER_TEX.draw(g, mx, my, dividerX, innerTopY, 1, innerH);
                DIVIDER_FILL_TEX.draw(g, mx, my, dividerX + 1, innerTopY, 4, innerH);
                DIVIDER_TEX.draw(g, mx, my, dividerX + 5, innerTopY, 1, innerH);
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
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != LabColors.MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);

            for (int i = 0; i < tabs.length; i++) {
                if (tabs[i].isMouseOverElement(mouseX, mouseY)) {
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
            if (tabChangedListener != null) tabChangedListener.run();
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
                if (this.machineChangedListener != null) this.machineChangedListener.run();
            });
        }

        void refreshMachineSelection() {
            machineDropdown.refreshSelection();
        }

        private void updateRecipeView() {
            if (isLeft) {
                boolean showRecipeView = rightPanel != null && rightPanel.getSelectedTabIndex() == 0;
                searchField.setVisible(showRecipeView);
                recipeBrowser.setVisible(showRecipeView);
                if (showRecipeView) {
                    recipeBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                    recipeBrowser.setMachineFilter(rightPanel.getMachineRecipeIds());
                    recipeBrowser.setNewRecipeIds(NEW_RECIPE_IDS);
                    recipeBrowser.rebuild();
                }
            } else {
                boolean recipeTabActive = getSelectedTabIndex() == 0;
                machineDropdown.setVisible(recipeTabActive);
                machineLayout.setVisible(recipeTabActive);
                settingsWidget.setVisible(recipeTabActive);
                if (recipeTabActive) {
                    LabMachine machine = machineDropdown.getSelectedMachine();
                    machineLayout.setMachine(machine);
                    settingsWidget.setShapelessSupported(machine != null && machine.supported());
                }
            }
        }

        LabRecipeBrowserWidget getRecipeBrowser() {
            return recipeBrowser;
        }

        LabMachine getSelectedMachine() {
            return machineDropdown.getSelectedMachine();
        }

        Set<ResourceLocation> getMachineRecipeIds() {
            LabMachine machine = machineDropdown.getSelectedMachine();
            return machine == null ? null : LabMachineCatalog.recipeIds(machine);
        }

        private void showRecipe(LabRecipeIndex.LabRecipeEntry entry) {
            machineLayout.showRecipe(entry);
        }

        private void saveRecipe() {
            ItemStack output = machineLayout.getOutput();
            if (output.isEmpty()) return;

            ItemStack[][] grid = machineLayout.getGrid();
            boolean hasInput = false;
            for (int r = 0; r < 3 && !hasInput; r++) {
                for (int c = 0; c < 3 && !hasInput; c++) {
                    hasInput = !grid[r][c].isEmpty();
                }
            }
            if (!hasInput) return;

            ResourceLocation id;
            if (settingsWidget.isShapeless()) {
                java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        if (!grid[r][c].isEmpty()) inputs.add(grid[r][c]);
                    }
                }
                id = LabRecipeSaver.saveShapeless(inputs.toArray(new ItemStack[0]), output);
            } else {
                id = LabRecipeSaver.saveShaped(grid, output);
            }

            if (id != null) {
                NEW_RECIPE_IDS.add(id);
                LabRecipeSaver.triggerReload();
            }
        }

        private void onSearchChanged(String value) {
            recipeBrowser.setQuery(value);
        }
    }
}