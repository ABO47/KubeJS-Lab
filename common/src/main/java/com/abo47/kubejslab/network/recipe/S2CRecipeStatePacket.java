package com.abo47.kubejslab.network.recipe;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.recipes.RecipeStates;
import com.abo47.kubejslab.client.ui.shell.ScreenFactory;
import com.abo47.kubejslab.recipe.model.RecipeStateEntry;
import com.abo47.kubejslab.recipe.model.RecipeStatus;


public record S2CRecipeStatePacket(Map<ResourceLocation, RecipeStateEntry> states) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(states.size());
        for (RecipeStateEntry entry : states.values()) {
            buf.writeUtf(entry.id().toString());
            buf.writeVarInt(entry.status().ordinal());
            RecipePacketCodecs.writeStack(buf, entry.output());
            buf.writeUtf(entry.name());
            buf.writeBoolean(entry.wasModified());
            buf.writeBoolean(entry.machineUid() != null);
            if (entry.machineUid() != null) {
                buf.writeUtf(entry.machineUid().toString());
            }
        }
        KubeJSLab.LOGGER.info("[Net] S2CRecipeStatePacket write: {} bytes, {} entries", buf.readableBytes(), states.size());
        if (buf.readableBytes() > 2048) {
            KubeJSLab.LOGGER.warn("S2CRecipeStatePacket is large ({} bytes, {} entries); may exceed the channel's per-string limit",
                    buf.readableBytes(), states.size());
        }
    }

    public static S2CRecipeStatePacket read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, RecipeStateEntry> states = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = new ResourceLocation(buf.readUtf());
            RecipeStatus status = RecipeStatus.values()[buf.readVarInt()];
            ItemStack output = RecipePacketCodecs.readStack(buf);
            String name = buf.readUtf();
            boolean wasModified = buf.readBoolean();
            ResourceLocation machineUid = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
            states.put(id, new RecipeStateEntry(id, status, output, name, wasModified, machineUid));
        }
        return new S2CRecipeStatePacket(states);
    }

    public void handleClient() {
        RecipeStates.apply(states);
        ScreenFactory.refreshOpen();
    }
}
