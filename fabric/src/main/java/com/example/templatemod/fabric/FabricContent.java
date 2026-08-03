package com.example.templatemod.fabric;

import com.example.templatemod.TemplateMod;
import com.example.templatemod.content.TemplateContent;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class FabricContent {
    private FabricContent() {
    }

    static void register() {
        Item item = new Item(new Item.Properties());
        TemplateContent.templateItem = item;
        Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(TemplateMod.MOD_ID, TemplateContent.TEMPLATE_ITEM_ID), item);
    }
}
