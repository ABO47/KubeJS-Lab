package com.abo47.kubejslab.client.ui.shell;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.block.model.BlockEditAction;
import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.client.ui.widgets.PickTarget;
import com.abo47.kubejslab.item.model.ItemEditAction;
import com.abo47.kubejslab.loot.model.LootEditAction;

public final class RootPanel extends WidgetGroup {
    private static final IGuiTexture ROOT_TEXTURE =
            UiColors.bordered(UiColors.SURFACE_BASE, UiColors.BORDER_BASE);

    private WorkspacePanel leftPanel;
    private WorkspacePanel rightPanel;
    private final WidgetGroup contextMenuLayer =
            new WidgetGroup(0, 0, UiLayout.ROOT_W, UiLayout.ROOT_H);
    private WidgetGroup modalLayer;
    private Widget pickerWidget;
    private boolean menuOpen;
    private List<ContextAction> menuActions = List.of();
    private int menuX;
    private int menuY;
    private int menuW;
    private int menuH;
    private long menuAnimStartMs;

    RootPanel() {
        super(0, 0, UiLayout.ROOT_W, UiLayout.ROOT_H);
    }

    void setPanels(WorkspacePanel leftPanel, WorkspacePanel rightPanel) {
        this.leftPanel = leftPanel;
        this.rightPanel = rightPanel;
    }

    void attachMenuLayer() {
        addWidget(contextMenuLayer);
    }

    void setModalLayer(WidgetGroup modalLayer) {
        this.modalLayer = modalLayer;
    }

    void setPickerWidget(Widget pickerWidget) {
        this.pickerWidget = pickerWidget;
    }

    boolean isModalOpen() {
        return modalLayer != null && modalLayer.isVisible();
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        if (isModalOpen()) {
            modalLayer.drawInForeground(g, mx, my, pt);
            if (pickerWidget != null && pickerWidget.isVisible()) {
                pickerWidget.drawInForeground(g, mx, my, pt);
            }
            return;
        }
        super.drawInForeground(g, mx, my, pt);
    }

    WorkspacePanel getLeftPanel() {
        return leftPanel;
    }

    WorkspacePanel getRightPanel() {
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
        if (button == UiColors.MOUSE_BUTTON_LEFT) {
            Widget hover = getHoverElement(mouseX, mouseY);
            if (!(hover instanceof PickTile) && !(hover instanceof PickTarget)) {
                clearPendingPicks();
            }
        }
        if (!handled && gui != null) {
            gui.getModularUIContainer().setCarried(ItemStack.EMPTY);
            return true;
        }
        return handled;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (isModalOpen()) {
            if (pickerWidget != null && pickerWidget.isVisible() && pickerWidget.isActive()
                    && pickerWidget.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
            if (modalLayer.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isModalOpen()) {
            if (pickerWidget != null && pickerWidget.isVisible() && pickerWidget.isActive()
                    && pickerWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
            if (modalLayer.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isModalOpen()) {
            boolean handled = false;
            if (pickerWidget != null && pickerWidget.isVisible() && pickerWidget.isActive()
                    && pickerWidget.mouseReleased(mouseX, mouseY, button)) {
                handled = true;
            }
            if (modalLayer.mouseReleased(mouseX, mouseY, button)) {
                handled = true;
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isModalOpen()) {
            if (pickerWidget != null && pickerWidget.isVisible() && pickerWidget.isActive()
                    && pickerWidget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (modalLayer.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (isModalOpen()) {
            if (pickerWidget != null && pickerWidget.isVisible() && pickerWidget.isActive()
                    && pickerWidget.charTyped(codePoint, modifiers)) {
                return true;
            }
            if (modalLayer.charTyped(codePoint, modifiers)) {
                return true;
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void clearPendingPicks() {
        if (rightPanel == null) {
            return;
        }
        rightPanel.machineLayout.clearPendingPick();
        if (rightPanel.poolModal != null) {
            rightPanel.poolModal.clearPendingPick();
        }
    }

    void openContextMenu(RecipeIndex.RecipeEntry entry, double mx, double my) {
        leftPanel.recipes.selectRecipe(entry);
        openActionsMenu(RecipeActions.forEntry(rightPanel, entry), mx, my);
    }

    void openItemContextMenu(ItemIndex.ItemEntry entry, double mx, double my) {
        rightPanel.items.selectItem(entry);
        openActionsMenu(ItemActions.forEntry(entry, new ItemActions.ItemActionCallbacks() {
            @Override
            public void openModify(ItemIndex.ItemEntry target) {
                rightPanel.items.enterItemModifyMode(target);
            }

            @Override
            public void send(ItemEditAction action, @Nonnull ResourceLocation targetId) {
                rightPanel.itemSaver.send(action, targetId);
            }
        }), mx, my);
    }

    void openBlockContextMenu(BlockIndex.BlockEntry entry, double mx, double my) {
        rightPanel.blocks.selectBlock(entry);
        openActionsMenu(BlockActions.forEntry(entry, new BlockActions.BlockActionCallbacks() {
            @Override
            public void openModify(BlockIndex.BlockEntry target) {
                rightPanel.blocks.enterBlockModifyMode(target);
            }

            @Override
            public void send(BlockEditAction action, @Nonnull ResourceLocation targetId) {
                rightPanel.blockSaver.send(action, targetId);
            }
        }), mx, my);
    }

    void openLootContextMenu(LootIndex.LootEntry entry, double mx, double my) {
        rightPanel.loot.selectLoot(entry);
        openActionsMenu(LootActions.forEntry(entry, new LootActions.LootActionCallbacks() {
            @Override
            public void openModify(LootIndex.LootEntry target) {
                rightPanel.loot.enterLootModifyMode(target);
            }

            @Override
            public void send(LootEditAction action, @Nonnull ResourceLocation targetId) {
                rightPanel.lootSaver.send(action, targetId);
            }
        }), mx, my);
    }

    void openActionsMenu(List<ContextAction> actions, double mx, double my) {
        menuActions = actions;
        menuW = ContextMenuPanel.menuWidth(actions);
        menuH = ContextMenuPanel.menuHeight(actions);
        menuX = (int) Math.max(4, Math.min(mx - getPositionX(), UiLayout.ROOT_W - menuW - 4));
        menuY = (int) Math.max(4, Math.min(my - getPositionY(), UiLayout.ROOT_H - menuH - 4));
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
        contextMenuLayer.addWidget(ContextMenuAnimation.wrap(
                ContextMenuPanel.build(menuX, menuY, menuActions, this::closeMenu),
                () -> menuAnimStartMs));
    }
}
