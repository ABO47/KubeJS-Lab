package com.abo47.kubejslab.network.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.HeatRequirement;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;
import com.abo47.kubejslab.recipe.runtime.LabRecipeService;

public record C2SRecipeEditPacket(LabRecipeEditAction action, @Nullable ResourceLocation targetId,
        LabRecipePayload payload) {

    public void write(FriendlyByteBuf buf) {
        int start = buf.writerIndex();
        buf.writeVarInt(action.ordinal());
        buf.writeBoolean(targetId != null);
        if (targetId != null) {
            buf.writeUtf(targetId.toString(), 32767);
        }
        buf.writeBoolean(payload.machineUid() != null);
        if (payload.machineUid() != null) {
            buf.writeUtf(payload.machineUid().toString(), 32767);
        }
        int afterIds = buf.writerIndex() - start;
        buf.writeVarInt(payload.inputs().size());
        for (LabIngredient ingredient : payload.inputs()) {
            LabPacketCodecs.writeIngredient(buf, ingredient);
        }
        int afterInputs = buf.writerIndex() - start;
        buf.writeVarInt(payload.outputs().size());
        for (LabRecipeOutput output : payload.outputs()) {
            LabPacketCodecs.writeOutput(buf, output);
        }
        int afterOutputs = buf.writerIndex() - start;
        buf.writeUtf(payload.name() == null ? "" : payload.name(), 32767);
        LabRecipeFieldValues values = payload.values();
        buf.writeBoolean(values.shapeless());
        buf.writeFloat(values.experience());
        buf.writeVarInt(values.cookingTime());
        buf.writeVarInt(values.count());
        buf.writeVarInt(values.processingTime());
        buf.writeUtf(values.heatRequirement().name(), 32767);
        buf.writeBoolean(values.keepHeldItem());
        buf.writeBoolean(values.acceptMirrored());
        buf.writeVarInt(values.gridWidth());
        buf.writeVarInt(values.gridHeight());
        buf.writeVarInt(values.outputCount());
        buf.writeVarInt(values.energy());
        buf.writeVarInt(values.creosoteAmount());
        buf.writeUtf(values.mold(), 32767);
        buf.writeUtf(values.blueprintCategory(), 32767);
        buf.writeUtf(values.clocheRenderType().name(), 32767);
        buf.writeUtf(values.clocheRenderBlock(), 32767);
        int total = buf.writerIndex() - start;
        KubeJSLab.LOGGER.info(
                "[Net] C2SRecipeEditPacket encoded: ids={}b, inputs={}b, outputs={}b, name+values={}b, total={}b ({} inputs, {} outputs, grid={}x{}, outputCount={}, energy={})",
                afterIds, afterInputs - afterIds, afterOutputs - afterInputs, total - afterOutputs, total,
                payload.inputs().size(), payload.outputs().size(), values.gridWidth(), values.gridHeight(),
                values.outputCount(), values.energy());
    }

    public static C2SRecipeEditPacket read(FriendlyByteBuf buf) {
        LabRecipeEditAction action = LabRecipeEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        ResourceLocation machineUid = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        int inputCount = Math.min(buf.readVarInt(), 81);
        List<LabIngredient> inputs = new ArrayList<>(inputCount);
        for (int i = 0; i < inputCount; i++) {
            inputs.add(LabPacketCodecs.readIngredient(buf));
        }
        int outputCount = Math.min(buf.readVarInt(), 12);
        List<LabRecipeOutput> outputs = new ArrayList<>(outputCount);
        for (int i = 0; i < outputCount; i++) {
            outputs.add(LabPacketCodecs.readOutput(buf));
        }
        String name = buf.readUtf();
        boolean shapeless = buf.readBoolean();
        float experience = buf.readFloat();
        int cookingTime = Math.min(buf.readVarInt(), 200000);
        int count = Math.min(buf.readVarInt(), 64);
        int processingTime = Math.min(buf.readVarInt(), 200000);
        HeatRequirement heatRequirement = HeatRequirement.byName(buf.readUtf());
        boolean keepHeldItem = buf.readBoolean();
        boolean acceptMirrored = buf.readBoolean();
        int gridWidth = Math.max(1, Math.min(9, buf.readVarInt()));
        int gridHeight = Math.max(1, Math.min(9, buf.readVarInt()));
        int recipeOutputCount = Math.max(1, Math.min(6, buf.readVarInt()));
        int energy = Math.max(0, Math.min(100000, buf.readVarInt()));
        int creosoteAmount = Math.max(0, Math.min(200000, buf.readVarInt()));
        String mold = buf.readUtf();
        String blueprintCategory = buf.readUtf();
        com.abo47.kubejslab.recipe.model.ClocheRenderType clocheRenderType =
                com.abo47.kubejslab.recipe.model.ClocheRenderType.byName(buf.readUtf());
        String clocheRenderBlock = buf.readUtf();
        return new C2SRecipeEditPacket(action, targetId,
                new LabRecipePayload(machineUid, inputs, outputs, name,
                        new LabRecipeFieldValues(shapeless, experience, cookingTime, count, processingTime,
                                heatRequirement, keepHeldItem, acceptMirrored, gridWidth, gridHeight, recipeOutputCount,
                                energy, creosoteAmount, mold, blueprintCategory, clocheRenderType, clocheRenderBlock)));
    }

    public void handle(ServerPlayer player) {
        KubeJSLab.LOGGER.info("[Net] C2SRecipeEditPacket received from {}: action={}, targetId={}, machineUid={}, inputs={}, outputs={}, name={}, grid={}x{}, outputCount={}",
                player.getName().getString(), action, targetId, payload.machineUid(), payload.inputs().size(),
                payload.outputs().size(), payload.name(), payload.values().gridWidth(), payload.values().gridHeight(),
                payload.values().outputCount());
        if (!player.hasPermissions(2)) {
            KubeJSLab.LOGGER.warn("[Net] C2SRecipeEditPacket rejected: {} lacks permission level 2", player.getName().getString());
            return;
        }
        LabRecipeService.handle(player, action, targetId, payload);
    }
}
