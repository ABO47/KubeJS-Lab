package com.abo47.kubejslab.loot.runtime;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;


public final class LootTableScanner {
    public static final int MAX_TABLES = 512;

    private LootTableScanner() {
    }

    public static List<ResourceLocation> scan(MinecraftServer server) {
        Set<ResourceLocation> tables = new LinkedHashSet<>();
        if (server == null) {
            return List.of();
        }
        PackRepository repository = server.getPackRepository();
        for (String packId : repository.getSelectedIds()) {
            Pack pack = repository.getPack(packId);
            if (pack == null) {
                continue;
            }
            try (PackResources resources = pack.open()) {
                for (String namespace : resources.getNamespaces(PackType.SERVER_DATA)) {
                    resources.listResources(PackType.SERVER_DATA, namespace, "loot_tables",
                            (location, supplier) -> {
                                if (!location.getPath().endsWith(".json")) {
                                    return;
                                }
                                String path = location.getPath().substring("loot_tables/".length(),
                                        location.getPath().length() - ".json".length());
                                if (!path.isBlank()) {
                                    tables.add(new ResourceLocation(location.getNamespace(), path));
                                }
                            });
                }
            } catch (Exception ignored) {
            }
            if (tables.size() >= MAX_TABLES) {
                break;
            }
        }
        List<ResourceLocation> result = new ArrayList<>(tables);
        return result.size() > MAX_TABLES ? result.subList(0, MAX_TABLES) : result;
    }
}
