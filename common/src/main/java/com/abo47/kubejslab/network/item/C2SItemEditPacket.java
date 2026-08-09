package com.abo47.kubejslab.network.item;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.item.model.LabItemAction;
import com.abo47.kubejslab.item.model.LabItemEditAction;
import com.abo47.kubejslab.item.model.LabItemFieldValues;
import com.abo47.kubejslab.item.model.LabItemPayload;
import com.abo47.kubejslab.item.runtime.LabItemService;


public record C2SItemEditPacket(LabItemEditAction action, @Nullable ResourceLocation targetId,
        LabItemPayload payload) {

    public void write(FriendlyByteBuf buf) {
        int start = buf.writerIndex();
        buf.writeVarInt(action.ordinal());
        buf.writeBoolean(targetId != null);
        if (targetId != null) {
            buf.writeUtf(targetId.toString(), 32767);
        }
        buf.writeUtf(payload.type(), 32767);
        int afterIds = buf.writerIndex() - start;
        LabItemFieldValues.write(buf, payload.values());
        int afterValues = buf.writerIndex() - start;
        buf.writeVarInt(payload.tags().size());
        for (String tag : payload.tags()) {
            buf.writeUtf(tag, 32767);
        }
        buf.writeVarInt(payload.actions().size());
        for (LabItemAction itemAction : payload.actions()) {
            buf.writeVarInt(itemAction.ordinal());
        }
        int total = buf.writerIndex() - start;
        KubeJSLab.LOGGER.info(
                "[Net] C2SItemEditPacket encoded: ids={}b, values={}b, tags+actions={}b, total={}b (type={}, {} tags, {} actions)",
                afterIds, afterValues - afterIds, total - afterValues, total, payload.type(), payload.tags().size(),
                payload.actions().size());
    }

    public static C2SItemEditPacket read(FriendlyByteBuf buf) {
        LabItemEditAction action = LabItemEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        String type = buf.readUtf();
        LabItemFieldValues values = LabItemFieldValues.read(buf);
        int tagCount = Math.min(buf.readVarInt(), 32);
        List<String> tagList = new ArrayList<>(tagCount);
        for (int i = 0; i < tagCount; i++) {
            tagList.add(buf.readUtf());
        }
        int actionCount = Math.min(buf.readVarInt(), 16);
        List<LabItemAction> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            int ordinal = buf.readVarInt();
            if (ordinal < LabItemAction.values().length) {
                actions.add(LabItemAction.values()[ordinal]);
            }
        }
        return new C2SItemEditPacket(action, targetId, new LabItemPayload(targetId, type, values, tagList, actions));
    }

    public void handle(ServerPlayer player) {
        KubeJSLab.LOGGER.info("[Net] C2SItemEditPacket received from {}: action={}, targetId={}, type={}, tags={}, actions={}",
                player.getName().getString(), action, targetId, payload.type(), payload.tags().size(),
                payload.actions().size());
        if (!player.hasPermissions(2)) {
            KubeJSLab.LOGGER.warn("[Net] C2SItemEditPacket rejected: {} lacks permission level 2", player.getName().getString());
            return;
        }
        LabItemService.handle(player, action, targetId, payload);
    }
}