package com.abo47.kubejslab.network.block;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.block.model.LabBlockAction;
import com.abo47.kubejslab.block.model.LabBlockEditAction;
import com.abo47.kubejslab.block.model.LabBlockFieldValues;
import com.abo47.kubejslab.block.model.LabBlockPayload;
import com.abo47.kubejslab.block.runtime.LabBlockService;
import com.abo47.kubejslab.KubeJSLab;


public record C2SBlockEditPacket(LabBlockEditAction action, @Nullable ResourceLocation targetId,
        LabBlockPayload payload) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(action.ordinal());
        buf.writeBoolean(targetId != null);
        if (targetId != null) {
            buf.writeUtf(targetId.toString(), 32767);
        }
        buf.writeUtf(payload.type(), 32767);
        LabBlockFieldValues.write(buf, payload.values());
        buf.writeVarInt(payload.tags().size());
        for (String tag : payload.tags()) {
            buf.writeUtf(tag, 32767);
        }
        buf.writeVarInt(payload.actions().size());
        for (LabBlockAction blockAction : payload.actions()) {
            buf.writeVarInt(blockAction.ordinal());
        }
    }

    public static C2SBlockEditPacket read(FriendlyByteBuf buf) {
        LabBlockEditAction action = LabBlockEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        String type = buf.readUtf();
        LabBlockFieldValues values = LabBlockFieldValues.read(buf);
        int tagCount = Math.min(buf.readVarInt(), 32);
        List<String> tagList = new ArrayList<>(tagCount);
        for (int i = 0; i < tagCount; i++) {
            tagList.add(buf.readUtf());
        }
        int actionCount = Math.min(buf.readVarInt(), 16);
        List<LabBlockAction> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            int ordinal = buf.readVarInt();
            if (ordinal < LabBlockAction.values().length) {
                actions.add(LabBlockAction.values()[ordinal]);
            }
        }
        return new C2SBlockEditPacket(action, targetId, new LabBlockPayload(targetId, type, values, tagList, actions));
    }

    public void handle(ServerPlayer player) {
        if (!player.hasPermissions(2)) {
            KubeJSLab.LOGGER.warn("[Net] C2SBlockEditPacket rejected: {} lacks permission level 2",
                    player.getName().getString());
            return;
        }
        LabBlockService.handle(player, action, targetId, payload);
    }
}
