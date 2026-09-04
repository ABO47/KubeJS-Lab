package com.abo47.kubejslab.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.block.WorktableBlock;
import com.abo47.kubejslab.content.ContentRegistry;
import com.abo47.kubejslab.item.TabletItem;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public final class ForgeContent {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, KubeJSLab.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KubeJSLab.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KubeJSLab.MOD_ID);

    public static final RegistryObject<Block> LAB_TABLE = BLOCKS.register("lab_table",
            () -> new WorktableBlock(BlockBehaviour.Properties.of().strength(3.0F, 3.0F)));

    public static final RegistryObject<Item> LAB_TABLE_ITEM = ITEMS.register("lab_table",
            () -> new BlockItem(LAB_TABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> LAB_TABLET = ITEMS.register("lab_tablet",
            () -> new TabletItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + KubeJSLab.MOD_ID + ".main"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> LAB_TABLET.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(LAB_TABLE_ITEM.get());
                        output.accept(LAB_TABLET.get());
                    })
                    .build());

    private ForgeContent() {
    }

    public static void register(IEventBus modBus) {
        ContentRegistry.registerContent(LAB_TABLE, LAB_TABLE_ITEM, LAB_TABLET);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
