package com.abo47.kubejslab.reload;

import net.minecraft.server.MinecraftServer;

import dev.architectury.platform.Platform;

import com.abo47.kubejslab.KubeJSLab;


public final class ReloadDispatcher {
    private ReloadDispatcher() {
    }

    public static void reload(MinecraftServer server, ReloadKind kind) {
        long started = System.nanoTime();
        int count = switch (kind) {
            case RECIPES -> reloadRecipes(server);
            case LOOT -> reloadLoot(server);
        };
        long millis = (System.nanoTime() - started) / 1_000_000L;
        KubeJSLab.LOGGER.info("[{}] selective {} reload finished: {} entries in {} ms", KubeJSLab.MOD_ID, kind,
                count, millis);
    }

    private static int reloadRecipes(MinecraftServer server) {
        if (Platform.isModLoaded("kubejs")) {
            try {
                KubeJSRecipeReload.reload(server);
            } catch (Exception e) {
                KubeJSLab.LOGGER.warn("[{}] KubeJS recipe reload failed, falling back to vanilla", KubeJSLab.MOD_ID,
                        e);
                VanillaRecipeReload.reload(server);
            }
        } else {
            VanillaRecipeReload.reload(server);
        }
        RecipeSync.toAllClients(server, server.getRecipeManager());
        return server.getRecipeManager().getRecipes().size();
    }

    private static int reloadLoot(MinecraftServer server) {
        LootReload.reload(server);
        return LootReload.entryCount(server);
    }
}
