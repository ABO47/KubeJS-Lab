package com.abo47.kubejslab.content;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class LabContent {
    private static Supplier<Block> labTable = unregistered("lab_table");
    private static Supplier<Item> labTableItem = unregistered("lab_table_item");
    private static Supplier<Item> labTablet = unregistered("lab_tablet");

    public static final Supplier<Block> LAB_TABLE = () -> labTable.get();
    public static final Supplier<Item> LAB_TABLE_ITEM = () -> labTableItem.get();
    public static final Supplier<Item> LAB_TABLET = () -> labTablet.get();

    private LabContent() {
    }

    public static void registerContent(Supplier<Block> blockSupplier, Supplier<Item> blockItemSupplier, Supplier<Item> tabletSupplier) {
        labTable = Objects.requireNonNull(blockSupplier, "blockSupplier");
        labTableItem = Objects.requireNonNull(blockItemSupplier, "blockItemSupplier");
        labTablet = Objects.requireNonNull(tabletSupplier, "tabletSupplier");
    }

    private static <T> Supplier<T> unregistered(String name) {
        return () -> {
            throw new IllegalStateException("Content not registered: " + name);
        };
    }
}
