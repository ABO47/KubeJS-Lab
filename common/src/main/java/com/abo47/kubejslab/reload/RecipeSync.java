package com.abo47.kubejslab.reload;

import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.item.crafting.RecipeManager;


public final class RecipeSync {
    private RecipeSync() {
    }

    public static void toAllClients(MinecraftServer server, RecipeManager recipes) {
        ClientboundUpdateRecipesPacket recipesPacket = new ClientboundUpdateRecipesPacket(recipes.getRecipes());
        ClientboundUpdateTagsPacket tagsPacket = new ClientboundUpdateTagsPacket(
                TagNetworkSerialization.serializeTagsToNetwork(server.registries()));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(tagsPacket);
            player.connection.send(recipesPacket);
            player.getRecipeBook().sendInitialRecipeBook(player);
        }
    }
}
