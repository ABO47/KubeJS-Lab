package com.abo47.kubejslab.network.loot;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.loot.model.LabLootAction;
import com.abo47.kubejslab.loot.model.LabLootEditAction;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.model.LabLootPayload;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public record C2SLootEditPacket(LabLootEditAction action, @Nullable ResourceLocation targetId,
        LabLootPayload payload) {

    public void write(FriendlyByteBuf buf) {
        int start = buf.writerIndex();
        buf.writeVarInt(action.ordinal());
        buf.writeBoolean(targetId != null);
        if (targetId != null) {
            buf.writeUtf(targetId.toString(), 32767);
        }
        buf.writeUtf(payload.lootType(), 32767);
        int afterIds = buf.writerIndex() - start;
        LabLootFieldValues.write(buf, payload.values());
        int afterValues = buf.writerIndex() - start;
        buf.writeVarInt(payload.tags().size());
        for (String tag : payload.tags()) {
            buf.writeUtf(tag, 32767);
        }
        buf.writeVarInt(payload.actions().size());
        for (LabLootAction lootAction : payload.actions()) {
            buf.writeVarInt(lootAction.ordinal());
        }
        int total = buf.writerIndex() - start;
        KubeJSLab.LOGGER.info(
                "[Net] C2SLootEditPacket encoded: ids={}b, values={}b, tags+actions={}b, total={}b (type={}, {} tags, {} actions)",
                afterIds, afterValues - afterIds, total - afterValues, total, payload.lootType(), payload.tags().size(),
                payload.actions().size());
    }

    public static C2SLootEditPacket read(FriendlyByteBuf buf) {
        LabLootEditAction action = LabLootEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        String lootType = buf.readUtf();
        LabLootFieldValues values = LabLootFieldValues.read(buf);
        int tagCount = Math.min(buf.readVarInt(), 32);
        List<String> tagList = new ArrayList<>(tagCount);
        for (int i = 0; i < tagCount; i++) {
            tagList.add(buf.readUtf());
        }
        int actionCount = Math.min(buf.readVarInt(), 16);
        List<LabLootAction> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            int ordinal = buf.readVarInt();
            if (ordinal < LabLootAction.values().length) {
                actions.add(LabLootAction.values()[ordinal]);
            }
        }
        return new C2SLootEditPacket(action, targetId,
                new LabLootPayload(targetId, lootType, values, tagList, actions));
    }

    public void handle(ServerPlayer player) {
        KubeJSLab.LOGGER.info(
                "[Net] C2SLootEditPacket received from {}: action={}, targetId={}, type={}, tags={}, actions={}",
                player.getName().getString(), action, targetId, payload.lootType(), payload.tags().size(),
                payload.actions().size());
        if (!player.hasPermissions(2)) {
            KubeJSLab.LOGGER.warn("[Net] C2SLootEditPacket rejected: {} lacks permission level 2",
                    player.getName().getString());
            return;
        }
        LabLootService.handle(player, action, targetId, payload);
    }
}
