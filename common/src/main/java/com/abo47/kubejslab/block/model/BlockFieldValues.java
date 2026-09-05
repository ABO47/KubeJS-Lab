package com.abo47.kubejslab.block.model;

import net.minecraft.network.FriendlyByteBuf;


public record BlockFieldValues(String displayName, String textureAll, String textureTop, String textureBottom,
        String textureSides, float hardness, float resistance, boolean unbreakable, int lightLevel, String soundType,
        boolean requiresTool, boolean noCollision, boolean waterlogged, boolean noDrops, boolean notSolid,
        boolean opaque, float slipperiness, float speedFactor, float jumpFactor, String tags, String creativeTab,
        String lootItem, int lootCountMin, int lootCountMax, float lootChance, String dustColor, String blockSetType,
        String woodType) {

    public static final float DEFAULT_HARDNESS = 1.5f;
    public static final float DEFAULT_RESISTANCE = 3f;

    public BlockFieldValues {
        displayName = displayName == null ? "" : displayName;
        textureAll = textureAll == null ? "" : textureAll;
        textureTop = textureTop == null ? "" : textureTop;
        textureBottom = textureBottom == null ? "" : textureBottom;
        textureSides = textureSides == null ? "" : textureSides;
        hardness = Math.max(-1f, hardness);
        resistance = Math.max(0f, resistance);
        lightLevel = Math.max(0, Math.min(15, lightLevel));
        soundType = soundType == null ? "" : soundType;
        slipperiness = Math.max(0f, Math.min(1f, slipperiness));
        speedFactor = Math.max(0f, speedFactor);
        jumpFactor = Math.max(0f, jumpFactor);
        tags = tags == null ? "" : tags;
        creativeTab = creativeTab == null ? "" : creativeTab;
        lootItem = lootItem == null ? "" : lootItem;
        lootCountMin = Math.max(0, lootCountMin);
        lootCountMax = Math.max(lootCountMin, lootCountMax);
        lootChance = Math.max(0f, Math.min(100f, lootChance));
        dustColor = dustColor == null ? "" : dustColor;
        blockSetType = blockSetType == null ? "" : blockSetType;
        woodType = woodType == null ? "" : woodType;
    }

    public static final int DEFAULT_LOOT_COUNT_MIN = 1;
    public static final int DEFAULT_LOOT_COUNT_MAX = 1;
    public static final float DEFAULT_LOOT_CHANCE = 100f;

    public static BlockFieldValues defaults() {
        return new BlockFieldValues("", "", "", "", "", DEFAULT_HARDNESS, DEFAULT_RESISTANCE, false, 0, "wood",
                false, false, false, false, false, true, 0f, 0f, 0f, "", "", "",
                DEFAULT_LOOT_COUNT_MIN, DEFAULT_LOOT_COUNT_MAX, DEFAULT_LOOT_CHANCE, "", "", "");
    }

    public static void write(FriendlyByteBuf buf, BlockFieldValues v) {
        buf.writeUtf(v.displayName(), 32767);
        buf.writeUtf(v.textureAll(), 32767);
        buf.writeUtf(v.textureTop(), 32767);
        buf.writeUtf(v.textureBottom(), 32767);
        buf.writeUtf(v.textureSides(), 32767);
        buf.writeFloat(v.hardness());
        buf.writeFloat(v.resistance());
        buf.writeBoolean(v.unbreakable());
        buf.writeVarInt(v.lightLevel());
        buf.writeUtf(v.soundType(), 32767);
        buf.writeBoolean(v.requiresTool());
        buf.writeBoolean(v.noCollision());
        buf.writeBoolean(v.waterlogged());
        buf.writeBoolean(v.noDrops());
        buf.writeBoolean(v.notSolid());
        buf.writeBoolean(v.opaque());
        buf.writeFloat(v.slipperiness());
        buf.writeFloat(v.speedFactor());
        buf.writeFloat(v.jumpFactor());
        buf.writeUtf(v.tags(), 32767);
        buf.writeUtf(v.creativeTab(), 32767);
        buf.writeUtf(v.lootItem(), 32767);
        buf.writeVarInt(Math.max(0, Math.min(64, v.lootCountMin())));
        buf.writeVarInt(Math.max(0, Math.min(64, v.lootCountMax())));
        buf.writeFloat(v.lootChance());
        buf.writeUtf(v.dustColor(), 32767);
        buf.writeUtf(v.blockSetType(), 32767);
        buf.writeUtf(v.woodType(), 32767);
    }

    public static BlockFieldValues read(FriendlyByteBuf buf) {
        return new BlockFieldValues(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                buf.readFloat(), buf.readFloat(), buf.readBoolean(),
                Math.max(0, Math.min(15, buf.readVarInt())), buf.readUtf(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                Math.max(0, Math.min(64, buf.readVarInt())), Math.max(0, Math.min(64, buf.readVarInt())),
                buf.readFloat(), buf.readUtf(), buf.readUtf(), buf.readUtf());
    }
}
