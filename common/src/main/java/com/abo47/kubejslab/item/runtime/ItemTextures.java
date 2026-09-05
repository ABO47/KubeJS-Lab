package com.abo47.kubejslab.item.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class ItemTextures {

    private ItemTextures() {
    }

    static boolean copyTextures(Map<ResourceLocation, ItemSaveEntry> states) throws IOException {
        boolean copied = false;
        Path root = WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures");
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            ItemSaveEntry entry = item.getValue();
            String rel = entry.values().texture();
            if (rel.isBlank()) {
                continue;
            }
            Path source = root.resolve(rel).normalize();
            Path dest = textureFile(item.getKey());
            if (source.equals(dest) || !source.startsWith(root.normalize()) || !Files.isRegularFile(source)
                    || !extension(source.getFileName().toString()).equals("png")) {
                continue;
            }
            Files.createDirectories(dest.getParent());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            copied = true;
        }
        return copied;
    }

    static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    static Path textureFile(ResourceLocation id) {
        return WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures").resolve("item")
                .resolve(id.getPath() + ".png");
    }
}
