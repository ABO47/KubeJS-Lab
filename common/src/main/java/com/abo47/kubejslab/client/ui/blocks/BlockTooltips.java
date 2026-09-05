package com.abo47.kubejslab.client.ui.blocks;

import java.util.Locale;

import com.abo47.kubejslab.block.model.BlockField;


public final class BlockTooltips {
    private BlockTooltips() {
    }

    public static String key(BlockField field) {
        return BlockKeys.BLOCK_TIP_PREFIX + field.name().toLowerCase(Locale.ROOT);
    }
}
