package com.abo47.kubejslab.reload;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.server.ServerScriptManager;


public final class KubeJSResources {
    private KubeJSResources() {
    }

    public static CloseableResourceManager openReload(MinecraftServer server) {
        if (Platform.isModLoaded("kubejs")) {
            CloseableResourceManager clean = CleanResources.openClean(server);
            ResourceManager wrapped = ServerScriptManager.instance.wrapResourceManager(clean);
            return new ClosingResources(wrapped, clean);
        }
        return CleanResources.openClean(server);
    }

    private static final class ClosingResources implements CloseableResourceManager {
        private final ResourceManager wrapped;
        private final CloseableResourceManager clean;

        private ClosingResources(ResourceManager wrapped, CloseableResourceManager clean) {
            this.wrapped = wrapped;
            this.clean = clean;
        }

        @Override
        public void close() {
            clean.close();
        }

        @Override
        public Optional<Resource> getResource(ResourceLocation location) {
            return wrapped.getResource(location);
        }

        @Override
        public Set<String> getNamespaces() {
            return wrapped.getNamespaces();
        }

        @Override
        public List<Resource> getResourceStack(ResourceLocation location) {
            return wrapped.getResourceStack(location);
        }

        @Override
        public Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter) {
            return wrapped.listResources(path, filter);
        }

        @Override
        public Map<ResourceLocation, List<Resource>> listResourceStacks(String path,
                Predicate<ResourceLocation> filter) {
            return wrapped.listResourceStacks(path, filter);
        }

        @Override
        public Stream<PackResources> listPacks() {
            return wrapped.listPacks();
        }
    }
}
