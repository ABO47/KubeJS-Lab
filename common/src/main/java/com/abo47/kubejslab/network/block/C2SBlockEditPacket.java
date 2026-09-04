package com.abo47.kubejslab.network.block;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.block.model.BlockAction;
import com.abo47.kubejslab.block.model.BlockEditAction;
import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.block.model.BlockPayload;
import com.abo47.kubejslab.block.runtime.BlockService;


public record C2SBlockEditPacket(BlockEditAction action, @Nullable ResourceLocation targetId,
        BlockPayload payload) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(action.ordinal());
        buf.writeBoolean(targetId != null);
        if (targetId != null) {
            buf.writeUtf(targetId.toString(), 32767);
        }
        buf.writeUtf(payload.type(), 32767);
        BlockFieldValues.write(buf, payload.values());
        buf.writeVarInt(payload.tags().size());
        for (String tag : payload.tags()) {
            buf.writeUtf(tag, 32767);
        }
        buf.writeVarInt(payload.actions().size());
        for (BlockAction blockAction : payload.actions()) {
            buf.writeVarInt(blockAction.ordinal());
        }
    }

    public static C2SBlockEditPacket read(FriendlyByteBuf buf) {
        BlockEditAction action = BlockEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        String type = buf.readUtf();
        BlockFieldValues values = BlockFieldValues.read(buf);
        int tagCount = Math.min(buf.readVarInt(), 32);
        List<String> tagList = new ArrayList<>(tagCount);
        for (int i = 0; i < tagCount; i++) {
            tagList.add(buf.readUtf());
        }
        int actionCount = Math.min(buf.readVarInt(), 16);
        List<BlockAction> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            int ordinal = buf.readVarInt();
            if (ordinal < BlockAction.values().length) {
                actions.add(BlockAction.values()[ordinal]);
            }
        }
        return new C2SBlockEditPacket(action, targetId, new BlockPayload(targetId, type, values, tagList, actions));
    }

    public void handle(ServerPlayer player) {
        if (!player.hasPermissions(2)) {
            KubeJSLab.LOGGER.warn("[Net] C2SBlockEditPacket rejected: {} lacks permission level 2",
                    player.getName().getString());
            return;
        }
        BlockService.handle(player, action, targetId, payload);
    }
}
