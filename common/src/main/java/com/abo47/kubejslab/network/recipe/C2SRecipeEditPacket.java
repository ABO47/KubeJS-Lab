package com.abo47.kubejslab.network.recipe;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
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
        buf.writeBoolean(payload.shapeless());
        buf.writeBoolean(payload.hasGrid());
        if (payload.hasGrid()) {
            for (ItemStack stack : payload.grid()) {
                LabPacketCodecs.writeStack(buf, stack);
            }
        }
        LabPacketCodecs.writeStack(buf, payload.output());
        buf.writeUtf(payload.name() == null ? "" : payload.name());
    }

    public static C2SRecipeEditPacket read(FriendlyByteBuf buf) {
        LabRecipeEditAction action = LabRecipeEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        boolean shapeless = buf.readBoolean();
        ItemStack[] grid = buf.readBoolean() ? new ItemStack[9] : null;
        if (grid != null) {
            for (int i = 0; i < grid.length; i++) {
                grid[i] = LabPacketCodecs.readStack(buf);
            }
        }
        ItemStack output = LabPacketCodecs.readStack(buf);
        String name = buf.readUtf();
        return new C2SRecipeEditPacket(action, targetId, new LabRecipePayload(shapeless, grid, output, name));
    }

    public void handle(ServerPlayer player) {
        if (!player.hasPermissions(2)) {
            return;
        }
        LabRecipeService.handle(player, action, targetId, payload);
    }
}
