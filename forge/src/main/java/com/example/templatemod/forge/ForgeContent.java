package com.example.templatemod.forge;

import com.example.templatemod.TemplateMod;
import com.example.templatemod.content.TemplateContent;

import net.minecraft.world.item.Item;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ForgeContent {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TemplateMod.MOD_ID);

    private static final RegistryObject<Item> TEMPLATE_ITEM = ITEMS.register(
            TemplateContent.TEMPLATE_ITEM_ID,
            () -> {
                Item item = new Item(new Item.Properties());
                TemplateContent.templateItem = item;
                return item;
            });

    private ForgeContent() {
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
