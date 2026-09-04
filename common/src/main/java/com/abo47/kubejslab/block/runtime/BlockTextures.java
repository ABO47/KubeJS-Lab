package com.abo47.kubejslab.block.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class BlockTextures {

    private BlockTextures() {
    }

    static boolean copyTextures(Map<ResourceLocation, BlockSaveEntry> states) throws IOException {
        boolean copied = false;
        Path root = WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures");
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : states.entrySet()) {
            BlockSaveEntry entry = item.getValue();
            BlockFieldValues v = entry.values();
            String path = item.getKey().getPath();
            copied |= copyOne(root, v.textureAll(), texturesDir().resolve(path + ".png"));
            copied |= copyOne(root, v.textureTop(), texturesDir().resolve(path + "_top.png"));
            copied |= copyOne(root, v.textureBottom(), texturesDir().resolve(path + "_bottom.png"));
            copied |= copyOne(root, v.textureSides(), texturesDir().resolve(path + "_side.png"));
            if ("crop".equals(entry.type()) && !v.textureAll().isBlank()) {
                Path source = root.resolve(v.textureAll()).normalize();
                if (Files.isRegularFile(source)) {
                    for (int age = 0; age < BlockService.CROP_AGES; age++) {
                        Path dest = texturesDir().resolve(path + age + ".png");
                        Files.createDirectories(dest.getParent());
                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                        copied = true;
                    }
                }
            }
        }
        return copied;
    }

    static boolean copyOne(Path root, String rel, Path dest) throws IOException {
        if (rel.isBlank()) {
            return false;
        }
        Path source = root.resolve(rel).normalize();
        if (source.equals(dest) || !source.startsWith(root.normalize()) || !Files.isRegularFile(source)
                || !"png".equals(extension(source.getFileName().toString()))) {
            return false;
        }
        Files.createDirectories(dest.getParent());
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    static Path texturesDir() {
        return WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures").resolve("block");
    }

    static void deleteCopiedTextures(String path) throws IOException {
        Path dir = texturesDir();
        Files.deleteIfExists(dir.resolve(path + ".png"));
        Files.deleteIfExists(dir.resolve(path + "_top.png"));
        Files.deleteIfExists(dir.resolve(path + "_bottom.png"));
        Files.deleteIfExists(dir.resolve(path + "_side.png"));
        for (int age = 0; age < BlockService.CROP_AGES; age++) {
            Files.deleteIfExists(dir.resolve(path + age + ".png"));
        }
    }
}
