package com.abo47.kubejslab.reload;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;


public final class CleanResources {
    private CleanResources() {
    }

    public static CloseableResourceManager openClean(MinecraftServer server) {
        return new MultiPackResourceManager(PackType.SERVER_DATA,
                server.getPackRepository().openAllSelected());
    }
}
