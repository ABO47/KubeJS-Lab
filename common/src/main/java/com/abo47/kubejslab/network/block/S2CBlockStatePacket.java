package com.abo47.kubejslab.network.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.block.model.LabBlockAction;
import com.abo47.kubejslab.block.model.LabBlockFieldValues;
import com.abo47.kubejslab.block.model.LabBlockState;
import com.abo47.kubejslab.block.model.LabBlockStatus;
import com.abo47.kubejslab.client.ui.LabScreen;
import com.abo47.kubejslab.client.ui.blocks.LabBlockStates;


public record S2CBlockStatePacket(Map<ResourceLocation, LabBlockState> states, List<ResourceLocation> pendingOnly) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(states.size());
        for (LabBlockState entry : states.values()) {
            buf.writeUtf(entry.id().toString());
            buf.writeUtf(entry.type(), 32767);
            buf.writeVarInt(entry.status().ordinal());
            buf.writeBoolean(entry.pendingRestart());
            buf.writeUtf(entry.name());
            buf.writeBoolean(entry.wasModified());
            LabBlockFieldValues.write(buf, entry.values());
            buf.writeVarInt(entry.tags().size());
            for (String tag : entry.tags()) {
                buf.writeUtf(tag, 32767);
            }
            buf.writeVarInt(entry.actions().size());
            for (LabBlockAction action : entry.actions()) {
                buf.writeVarInt(action.ordinal());
            }
        }
        buf.writeVarInt(pendingOnly.size());
        for (ResourceLocation id : pendingOnly) {
            buf.writeUtf(id.toString());
        }
        if (buf.readableBytes() > 2048) {
            KubeJSLab.LOGGER.warn("S2CBlockStatePacket is large ({} bytes, {} entries)", buf.readableBytes(),
                    states.size());
        }
    }

    public static S2CBlockStatePacket read(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 65536);
        Map<ResourceLocation, LabBlockState> states = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = new ResourceLocation(buf.readUtf());
            String type = buf.readUtf();
            int statusOrdinal = Math.min(buf.readVarInt(), LabBlockStatus.values().length - 1);
            boolean pendingRestart = buf.readBoolean();
            String name = buf.readUtf();
            boolean wasModified = buf.readBoolean();
            LabBlockFieldValues values = LabBlockFieldValues.read(buf);
            int tagCount = Math.min(buf.readVarInt(), 32);
            List<String> tagList = new ArrayList<>(tagCount);
            for (int t = 0; t < tagCount; t++) {
                tagList.add(buf.readUtf());
            }
            int actionCount = Math.min(buf.readVarInt(), 16);
            List<LabBlockAction> actions = new ArrayList<>(actionCount);
            for (int a = 0; a < actionCount; a++) {
                int ordinal = buf.readVarInt();
                if (ordinal < LabBlockAction.values().length) {
                    actions.add(LabBlockAction.values()[ordinal]);
                }
            }
            states.put(id, new LabBlockState(id, type, LabBlockStatus.values()[statusOrdinal], pendingRestart, name,
                    wasModified, values, tagList, actions));
        }
        int pendingCount = Math.min(buf.readVarInt(), 65536);
        List<ResourceLocation> pendingOnly = new ArrayList<>(pendingCount);
        for (int i = 0; i < pendingCount; i++) {
            pendingOnly.add(new ResourceLocation(buf.readUtf()));
        }
        return new S2CBlockStatePacket(states, pendingOnly);
    }

    public void handleClient() {
        LabBlockStates.apply(states, pendingOnly);
        LabScreen.refreshOpen();
    }
}
