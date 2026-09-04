package com.abo47.kubejslab.client.ui.shell;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.block.model.BlockField;
import com.abo47.kubejslab.client.assets.AssetPickerModal;
import com.abo47.kubejslab.client.assets.ColorPickerModal;
import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.blocks.BlockKeys;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.items.ItemKeys;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.recipes.RecipeKeys;
import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.RecipePayload;
import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class ScreenFactory {
    private ScreenFactory() {
    }

    static RecipePayload emptyPayload(RecipeIndex.RecipeEntry entry, ResourceLocation machineUid) {
        return new RecipePayload(machineUid, List.of(),
                List.of(new RecipeOutput.Item(entry.output(), 1f)), entry.name(),
                RecipeFieldValues.defaults());
    }

    public static ModularUI createUI(BlockPos holder, Player player) {
        RootPanel root = new RootPanel();
        WorkspacePanel leftPanel = new WorkspacePanel(true);
        WorkspacePanel rightPanel = new WorkspacePanel(false);

        root.setPanels(leftPanel, rightPanel);
        root.addWidget(leftPanel);
        root.addWidget(rightPanel);
        root.attachMenuLayer();
        PickerWindowWidget picker = PickerWindowWidget.create();
        picker.setPickListener(pick -> {
            if (rightPanel.poolModal != null && rightPanel.poolModal.offerPick(pick)) {
                return;
            }
            rightPanel.machineLayout.setPendingPick(pick);
        });

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
        rightPanel.itemTypeDropdown.setOnSelect(value -> {
            if ("all".equals(value)) {
                rightPanel.refreshItemPreview();
            } else {
                rightPanel.itemSettings.setType(value);
                rightPanel.refreshItemPreview();
            }
            updateViews.run();
        });
        rightPanel.blockTypeDropdown.setOnSelect(value -> {
            if ("all".equals(value)) {
                rightPanel.refreshBlockPreview();
            } else {
                rightPanel.blockSettings.setType(value);
                rightPanel.refreshBlockPreview();
            }
            updateViews.run();
        });
        rightPanel.settingsWidget.setCategoryContextRequester((option, mx, my) -> root.openActionsMenu(
                List.of(new ContextAction(I18n.get(RecipeKeys.RECIPE_DELETE), "delete", ActionTone.DANGER,
                        () -> rightPanel.settingsWidget.deleteBlueprintCategory(option))), mx, my));
        rightPanel.settingsWidget.setMoldContextRequester((option, mx, my) -> root.openActionsMenu(
                List.of(new ContextAction(I18n.get(RecipeKeys.RECIPE_DELETE), "delete", ActionTone.DANGER,
                        () -> rightPanel.settingsWidget.deleteCustomMold(option))), mx, my));

        WidgetGroup assetLayer = new WidgetGroup(0, 0, UiLayout.ROOT_W, UiLayout.ROOT_H);
        assetLayer.setVisible(false);
        root.addWidget(assetLayer);
        root.setModalLayer(assetLayer);
        root.addWidget(picker);
        root.setPickerWidget(picker);
        rightPanel.itemSettings.setOnTexturePick(() -> {
            rightPanel.itemSettings.closeAllPopups();
            AssetPickerModal.open(assetLayer,
                    WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures"),
                    I18n.get(ItemKeys.ITEM_TEXTURE),
                    path -> {
                        rightPanel.itemSettings.setTextureValue(path);
                        rightPanel.refreshItemPreview();
                    });
        });
        rightPanel.blockSettings.setOnTexturePick(field -> {
            rightPanel.blockSettings.closeAllPopups();
            if (field == BlockField.DUST_COLOR) {
                ColorPickerModal.open(assetLayer,
                        I18n.get(BlockKeys.BLOCK_DUST_COLOR),
                        rightPanel.blockSettings.getDustColor(),
                        hex -> rightPanel.blockSettings.setDustValue(hex));
                return;
            }
            AssetPickerModal.open(assetLayer,
                    WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures"),
                    I18n.get(ItemKeys.ITEM_TEXTURE),
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
        leftPanel.getLootBrowser().setLootClickListener(entry -> {
            leftPanel.selectLoot(entry);
            rightPanel.showLootSettings(entry);
        });
        leftPanel.getLootBrowser().setLootRightClickListener(
                (entry, mouseX, mouseY) -> root.openLootContextMenu(entry, mouseX, mouseY));
        rightPanel.lootTypeDropdown.setOnSelect(value -> updateViews.run());
        rightPanel.lootSettings.setOnEditPool((index, snapshot, lootType) -> {
            rightPanel.lootSettings.closeAllPopups();
            rightPanel.poolModal = LootPoolModal.open(assetLayer,
                    rightPanel.lootSettings.poolTitle(index), snapshot, lootType,
                    () -> {
                        rightPanel.lootSettings.deletePoolAt(index);
                        rightPanel.refreshLootPreview();
                    },
                    values -> {
                        rightPanel.lootSettings.applyPoolEdit(index, values);
                        rightPanel.refreshLootPreview();
                    });
            rightPanel.poolModal.setOnClose(() -> rightPanel.poolModal = null);
        });
        rightPanel.lootPreview.setOnDropClick((poolIndex, entryIndex) -> {
            LootFieldValues current = rightPanel.lootSettings.getValues();
            if (poolIndex < 0 || poolIndex >= current.pools().size()) {
                return;
            }
            rightPanel.lootSettings.closeAllPopups();
            LootPoolValues snapshot = current.pools().get(poolIndex);
            String lootType = rightPanel.lootSettings.getLootType();
            int targetPool = poolIndex;
            rightPanel.poolModal = LootPoolModal.open(assetLayer,
                    rightPanel.lootSettings.poolTitle(targetPool), snapshot, lootType,
                    () -> {
                        rightPanel.lootSettings.deletePoolAt(targetPool);
                        rightPanel.refreshLootPreview();
                    },
                    values -> {
                        rightPanel.lootSettings.applyPoolEdit(targetPool, values);
                        rightPanel.refreshLootPreview();
                    });
            rightPanel.poolModal.selectEntry(entryIndex);
            rightPanel.poolModal.setOnClose(() -> rightPanel.poolModal = null);
        });

        return new ModularUI(root, IUIHolder.EMPTY, player);
    }

    public static void activateClient(ModularUI ui) {
        RootPanel root = (RootPanel) ui.mainGroup;
        WorkspacePanel leftPanel = root.getLeftPanel();
        WorkspacePanel rightPanel = root.getRightPanel();
        rightPanel.machineDropdown.selectMachineByUid(ScreenSession.lastMachineUid);
        leftPanel.selectTabIndex(ScreenSession.lastLeftTab);
        rightPanel.selectTabIndex(ScreenSession.lastRightTab);
        leftPanel.restoreSearchQuery(ScreenSession.lastQuery);
        leftPanel.updateRecipeView();
        rightPanel.updateRecipeView();
        rightPanel.refreshMachineSelection();
    }

    public static void refreshOpen() {
        if (!(Minecraft.getInstance().screen instanceof ScreenContainer gui)) {
            return;
        }
        if (gui.modularUI.mainGroup instanceof RootPanel root) {
            root.getLeftPanel().updateRecipeView();
            root.getRightPanel().updateRecipeView();
        }
    }
}
