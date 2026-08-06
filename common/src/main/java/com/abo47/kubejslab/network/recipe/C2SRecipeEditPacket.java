package com.abo47.kubejslab.network.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.network.recipe.LabPacketCodecs;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;
import com.abo47.kubejslab.recipe.runtime.LabRecipeService;

public record C2SRecipeEditPacket(LabRecipeEditAction action, @Nullable ResourceLocation targetId,
        LabRecipePayload payload) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(action.ordinal());
        buf.writeBoolean(targetId != null);
        if (targetId != null) {
            buf.writeUtf(targetId.toString());
        }
        buf.writeBoolean(payload.machineUid() != null);
        if (payload.machineUid() != null) {
            buf.writeUtf(payload.machineUid().toString());
        }
        buf.writeVarInt(payload.inputs().size());
        for (ItemStack stack : payload.inputs()) {
            LabPacketCodecs.writeStack(buf, stack);
        }
        LabPacketCodecs.writeStack(buf, payload.output());
        buf.writeUtf(payload.name() == null ? "" : payload.name());
        LabRecipeFieldValues values = payload.values();
        buf.writeBoolean(values.shapeless());
        buf.writeFloat(values.experience());
        buf.writeVarInt(values.cookingTime());
        buf.writeVarInt(values.count());
        buf.writeVarInt(values.processingTime());
        buf.writeUtf(values.heatRequirement().name());
        buf.writeBoolean(values.keepHeldItem());
    }

    public static C2SRecipeEditPacket read(FriendlyByteBuf buf) {
        LabRecipeEditAction action = LabRecipeEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        ResourceLocation machineUid = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        int inputCount = Math.min(buf.readVarInt(), 9);
        List<ItemStack> inputs = new ArrayList<>(inputCount);
        for (int i = 0; i < inputCount; i++) {
            inputs.add(LabPacketCodecs.readStack(buf));
        }
        ItemStack output = LabPacketCodecs.readStack(buf);
        String name = buf.readUtf();
        boolean shapeless = buf.readBoolean();
        float experience = buf.readFloat();
        int cookingTime = Math.min(buf.readVarInt(), 200000);
        int count = Math.min(buf.readVarInt(), 64);
        int processingTime = Math.min(buf.readVarInt(), 200000);
        HeatRequirement heatRequirement = HeatRequirement.byName(buf.readUtf());
        boolean keepHeldItem = buf.readBoolean();
        return new C2SRecipeEditPacket(action, targetId,
                new LabRecipePayload(machineUid, inputs, output, name,
                        new LabRecipeFieldValues(shapeless, experience, cookingTime, count, processingTime,
                                heatRequirement, keepHeldItem)));
    }

    public void handle(ServerPlayer player) {
        if (!player.hasPermissions(2)) {
            return;
        }
        LabRecipeService.handle(player, action, targetId, payload);
    }
}
