package com.abo47.kubejslab.reload;

import java.util.concurrent.CompletableFuture;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;


public final class LootReload {
    private LootReload() {
    }

    public static void reload(MinecraftServer server) {
        LootDataManager loot = server.getLootData();
        PreparableReloadListener.PreparationBarrier barrier = new PreparationBarrier();
        try (CloseableResourceManager resources = KubeJSResources.openReload(server)) {
            loot.reload(barrier, resources, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE,
                    Util.backgroundExecutor(), Runnable::run).join();
        }
    }

    public static int entryCount(MinecraftServer server) {
        LootDataManager loot = server.getLootData();
        return loot.getKeys(LootDataType.TABLE).size()
                + loot.getKeys(LootDataType.PREDICATE).size()
                + loot.getKeys(LootDataType.MODIFIER).size();
    }

    private static final class PreparationBarrier implements PreparableReloadListener.PreparationBarrier {
        @Override
        public <T> CompletableFuture<T> wait(T value) {
            return CompletableFuture.completedFuture(value);
        }
    }
}
