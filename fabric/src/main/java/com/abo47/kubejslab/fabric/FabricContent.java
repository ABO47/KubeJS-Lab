package com.abo47.kubejslab.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.block.LabTable;
import com.abo47.kubejslab.content.LabContent;
import com.abo47.kubejslab.item.LabTabletItem;

public final class FabricContent {
    public static Block LAB_TABLE;
    public static Item LAB_TABLE_ITEM;
    public static Item LAB_TABLET;
    public static CreativeModeTab MAIN_TAB;

    private FabricContent() {
    }

    public static void register() {
        LAB_TABLE = Registry.register(
                BuiltInRegistries.BLOCK,
                id("lab_table"),
                new LabTable(BlockBehaviour.Properties.of().strength(3.0F, 3.0F)));

        LAB_TABLE_ITEM = Registry.register(
                BuiltInRegistries.ITEM,
                id("lab_table"),
                new BlockItem(LAB_TABLE, new Item.Properties()));

        LAB_TABLET = Registry.register(
                BuiltInRegistries.ITEM,
                id("lab_tablet"),
                new LabTabletItem(new Item.Properties().stacksTo(1)));

        LabContent.registerContent(() -> LAB_TABLE, () -> LAB_TABLE_ITEM, () -> LAB_TABLET);

        MAIN_TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                id("main"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup." + KubeJSLab.MOD_ID + ".main"))
                        .icon(() -> LAB_TABLET.getDefaultInstance())
                        .displayItems((parameters, output) -> {
                            output.accept(LAB_TABLE_ITEM);
                            output.accept(LAB_TABLET);
                        })
                        .build());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(KubeJSLab.MOD_ID, path);
    }
}
