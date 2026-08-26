package com.abo47.kubejslab.block.model;

import net.minecraft.network.FriendlyByteBuf;


public record LabBlockFieldValues(String displayName, String textureAll, String textureTop, String textureBottom,
        String textureSides, float hardness, float resistance, boolean unbreakable, int lightLevel, String soundType,
        boolean requiresTool, boolean noCollision, boolean waterlogged, boolean noDrops, boolean notSolid,
        boolean opaque, float slipperiness, float speedFactor, float jumpFactor, String tags) {

    public static final float DEFAULT_HARDNESS = 1.5f;
    public static final float DEFAULT_RESISTANCE = 3f;

    public LabBlockFieldValues {
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
    }

    public static LabBlockFieldValues defaults() {
        return new LabBlockFieldValues("", "", "", "", "", DEFAULT_HARDNESS, DEFAULT_RESISTANCE, false, 0, "wood",
                false, false, false, false, false, true, 0f, 0f, 0f, "");
    }

    public static void write(FriendlyByteBuf buf, LabBlockFieldValues v) {
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
    }

    public static LabBlockFieldValues read(FriendlyByteBuf buf) {
        return new LabBlockFieldValues(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                buf.readFloat(), buf.readFloat(), buf.readBoolean(),
                Math.max(0, Math.min(15, buf.readVarInt())), buf.readUtf(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readUtf());
    }
}
