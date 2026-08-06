package com.abo47.kubejslab.client.ui;
import com.abo47.kubejslab.client.ui.base.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.picker.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.LabRecipeMachines;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

public final class LabScreen {
    private static final IGuiTexture ROOT_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE);
    private static final IGuiTexture PANEL_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_PANEL, LabColors.BORDER_BASE);
    private static final IGuiTexture INNER_TEXTURE =
            LabColors.bordered(LabColors.SURFACE_BASE, LabColors.BORDER_BASE);
    private static final ColorRectTexture DIVIDER_TEX = new ColorRectTexture(LabColors.BORDER_BASE);
    private static final ColorRectTexture DIVIDER_FILL_TEX = new ColorRectTexture(LabColors.SURFACE_PANEL);
    private static final ColorRectTexture TAB_ERASE_TEX = new ColorRectTexture(LabColors.SURFACE_BASE);

    private static int lastLeftTab;
    private static int lastRightTab;
    private static ResourceLocation lastMachineUid;
    private static String lastQuery = "";

    private LabScreen() {
    }

    private static LabRecipePayload emptyPayload(LabRecipeIndex.LabRecipeEntry entry, ResourceLocation machineUid) {
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
            LabRecipeStatus status = LabRecipeStates.statusOf(entry.id());
            boolean custom = entry.kubejs();
            leftPanel.selectRecipe(entry);
            List<LabContextAction> actions = new ArrayList<>();
            if (status == LabRecipeStatus.NORMAL) {
                actions.add(modifyAction(entry));
                actions.add(disableAction(entry));
                if (custom) actions.add(deleteAction(entry));
            } else if (status == LabRecipeStatus.MODIFIED) {
                actions.add(resetAction(entry));
                actions.add(disableAction(entry));
                if (custom) actions.add(deleteAction(entry));
            } else {
                actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_ENABLE), LabActionTone.SUCCESS,
                        () -> sendEdit(LabRecipeEditAction.ENABLE, entry)));
                if (custom) actions.add(deleteAction(entry));
            }
            menuActions = actions;
            menuW = LabContextMenuPanel.menuWidth(actions);
            menuH = LabContextMenuPanel.menuHeight(actions);
            menuX = (int) Math.max(4, Math.min(mx - getPositionX(), LabLayout.ROOT_W - menuW - 4));
            menuY = (int) Math.max(4, Math.min(my - getPositionY(), LabLayout.ROOT_H - menuH - 4));
            menuOpen = true;
            menuAnimStartMs = System.currentTimeMillis();
            rebuildMenuLayer();
        }

        private LabContextAction modifyAction(LabRecipeIndex.LabRecipeEntry entry) {
            return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_MODIFY), LabActionTone.PRIMARY,
                    () -> rightPanel.enterModifyMode(entry));
        }

        private LabContextAction disableAction(LabRecipeIndex.LabRecipeEntry entry) {
            return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_DISABLE), LabActionTone.WARNING,
                    () -> sendEdit(LabRecipeEditAction.DISABLE, entry));
        }

        private LabContextAction resetAction(LabRecipeIndex.LabRecipeEntry entry) {
            return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_RESET), LabActionTone.SUCCESS,
                    () -> {
                        sendEdit(LabRecipeEditAction.RESET, entry);
                        rightPanel.exitModifyMode();
                    });
        }

        private LabContextAction deleteAction(LabRecipeIndex.LabRecipeEntry entry) {
            return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_DELETE), LabActionTone.DANGER,
                    () -> {
                        sendEdit(LabRecipeEditAction.DELETE, entry);
                        rightPanel.exitModifyModeIfTarget(entry);
                    });
        }

        private void sendEdit(LabRecipeEditAction action, LabRecipeIndex.LabRecipeEntry entry) {
            rightPanel.sendRecipeEdit(action, entry.id(), emptyPayload(entry, rightPanel.getSelectedMachineUid()));
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
        private enum EditMode {
            NEW, MODIFY
        }

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
        private EditMode mode = EditMode.NEW;
        private LabRecipeIndex.LabRecipeEntry modifyTarget;
        private TextTexture modeLabel;
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
            machineLayout.setOutputsChangedListener(
                    () -> settingsWidget.setOutputRows(machineLayout.getOutputRows()));
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
                sendRecipeEdit(LabRecipeEditAction.RESET, target.id(), emptyPayload(target, getSelectedMachineUid()));
                exitModifyMode();
                showRecipe(target);
            });
            settingsWidget.setOnSave(this::saveRecipe);
            settingsWidget.setGridSizeListener(() -> machineLayout
                    .setGridSize(settingsWidget.gridWidthValue(), settingsWidget.gridHeightValue()));
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

            if (!isLeft && modeLabel != null && machineDropdown.isVisible()) {
                modeLabel.draw(g, mx, my, getPositionX() + columnX, getPositionY() + modeLabelY,
                        columnW, LabLayout.MODE_LABEL_H);
            }
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
            if (isLeft) {
                lastLeftTab = index;
            } else {
                lastRightTab = index;
            }
            if (tabChangedListener != null) tabChangedListener.run();
        }

        void selectTabIndex(int index) {
            if (index >= 0 && index < tabs.length) {
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
            ResourceLocation uid = resolveModifyUid(entry);
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
                searchField.setVisible(showRecipeView);
                recipeBrowser.setVisible(showRecipeView);
                if (showRecipeView) {
                    recipeBrowser.setKubejsOnly(getSelectedTabIndex() == 1);
                    recipeBrowser.setMachineFilter(rightPanel.getMachineRecipeIds());
                    recipeBrowser.setMachineUid(rightPanel.getSelectedMachineUid());
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
                    LabRecipeMachine support = machine == null ? null : LabRecipeMachines.get(machine.recipeTypeUid());
                    settingsWidget.setFields(support == null ? List.of() : support.fields());
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
            }
        }

        private void saveRecipe() {
            boolean overriding = mode == EditMode.MODIFY && modifyTarget != null;
            if (!overriding) {
                saveNewRecipe();
                return;
            }
            ResourceLocation uid = resolveModifyUid(modifyTarget);
            if (uid == null) {
                saveGenericOverride();
                return;
            }
            LabRecipeMachine support = LabRecipeMachines.get(uid);
            if (support == null) {
                return;
            }
            List<LabIngredient> inputs = machineLayout.getInputs();
            if (!hasInput(inputs)) {
                return;
            }
            List<LabRecipeOutput> outputs = machineLayout.getOutputs();
            Recipe<?> original = LabRecipeIndex.recipeById(modifyTarget.id());
            if (outputs.isEmpty() && (original == null || !support.allowsEmptyResult(original))) {
                return;
            }
            sendRecipeEdit(LabRecipeEditAction.OVERRIDE, modifyTarget.id(),
                    new LabRecipePayload(uid, inputs, outputs, outputName(outputs), settingsWidget.getValues()));
        }

        private void saveNewRecipe() {
            LabMachine machine = machineDropdown.getSelectedMachine();
            if (machine == null) {
                return;
            }
            LabRecipeMachine support = LabRecipeMachines.get(machine.recipeTypeUid());
            if (support == null) {
                return;
            }
            List<LabIngredient> inputs = machineLayout.getInputs();
            if (!hasInput(inputs)) {
                return;
            }
            List<LabRecipeOutput> outputs = machineLayout.getOutputs();
            if (outputs.isEmpty()) {
                return;
            }
            sendRecipeEdit(LabRecipeEditAction.SAVE_NEW, null,
                    new LabRecipePayload(machine.recipeTypeUid(), inputs, outputs,
                            outputName(outputs), settingsWidget.getValues()));
        }

        private void saveGenericOverride() {
            List<LabIngredient> inputs = machineLayout.getInputs();
            if (!hasInput(inputs)) {
                return;
            }
            List<LabRecipeOutput> outputs = machineLayout.getOutputs();
            if (outputs.isEmpty()) {
                return;
            }
            sendRecipeEdit(LabRecipeEditAction.OVERRIDE, modifyTarget.id(),
                    new LabRecipePayload(null, inputs, outputs,
                            outputName(outputs), LabRecipeFieldValues.defaults()));
        }

        private static boolean hasInput(List<LabIngredient> inputs) {
            for (LabIngredient input : inputs) {
                if (!input.isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        private static String outputName(List<LabRecipeOutput> outputs) {
            ItemStack item = LabRecipeOutput.firstItem(outputs);
            if (!item.isEmpty()) {
                return item.getHoverName().getString();
            }
            for (LabRecipeOutput output : outputs) {
                if (output instanceof LabRecipeOutput.Fluid fluid && !fluid.fluid().isEmpty()) {
                    return fluid.fluid().getDisplayName().getString();
                }
            }
            return "";
        }

        private ResourceLocation resolveModifyUid(LabRecipeIndex.LabRecipeEntry entry) {
            ResourceLocation uid = LabRecipeStates.machineUidOf(entry.id());
            if (uid != null && LabRecipeMachines.supports(uid)) {
                return uid;
            }
            LabMachine machine = LabMachineCatalog.machineFor(entry.id());
            if (machine != null && machine.supported()) {
                return machine.recipeTypeUid();
            }
            return null;
        }

        private void sendRecipeEdit(LabRecipeEditAction action, ResourceLocation targetId, LabRecipePayload payload) {
            ModNetwork.sendRecipeEdit(new C2SRecipeEditPacket(action, targetId, payload));
        }

        private void onSearchChanged(String value) {
            lastQuery = value == null ? "" : value;
            recipeBrowser.setQuery(value);
        }
    }
}

