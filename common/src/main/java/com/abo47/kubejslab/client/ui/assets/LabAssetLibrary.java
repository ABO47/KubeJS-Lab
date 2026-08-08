package com.abo47.kubejslab.client.ui.assets;

import java.nio.file.Path;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

public final class LabAssetLibrary {
    private LabAssetLibrary() {
    }

    public enum AssetKind {
        DIRECTORY,
        IMAGE,
        GIF,
        SOUND,
        BLUEPRINT,
        UNKNOWN;

        public boolean hasImageThumbnail() {
            return this == IMAGE || this == GIF;
        }
    }

    public record AssetEntry(String name, String relativePath, boolean directory, AssetKind kind) {
        public AssetEntry(String name, String relativePath, boolean directory) {
            this(name, relativePath, directory, LabAssetPathResolver.assetKind(relativePath, directory));
        }
    }

    public record AssetDimensions(int width, int height) {
    }

    public static IGuiTexture chapterBackgroundTexture(Path assetsRoot, String background) {
        return LabAssetTextureCache.chapterBackgroundTexture(assetsRoot, background);
    }

    public static IGuiTexture chapterBackgroundTexture(Path assetsRoot, String background, boolean grayscale) {
        return LabAssetTextureCache.chapterBackgroundTexture(assetsRoot, background, grayscale);
    }

    public static List<AssetEntry> listAssetEntries(Path assetsRoot, String relativeDir) {
        return LabAssetSearchIndex.listAssetEntries(assetsRoot, relativeDir);
    }

    public static List<AssetEntry> searchAssetEntries(Path assetsRoot, String relativeDir, String query) {
        return LabAssetSearchIndex.searchAssetEntries(assetsRoot, relativeDir, query);
    }

    public static AssetKind assetKind(String relativePath) {
        return LabAssetPathResolver.assetKind(relativePath, false);
    }

    public static AssetDimensions assetDimensions(Path assetsRoot, String relativePath) {
        return LabAssetTextureCache.assetDimensions(assetsRoot, relativePath);
    }

    public static IGuiTexture assetThumbnailTexture(Path assetsRoot, String relativePath) {
        return LabAssetTextureCache.assetThumbnailTexture(assetsRoot, relativePath);
    }

    public static ResourceLocation staticTextureLocation(Path assetsRoot, String relativePath) {
        return LabAssetTextureCache.staticTextureLocation(assetsRoot, relativePath);
    }

    public static ResourceLocation tileTextureLocation(Path assetsRoot, String relativePath) {
        return LabAssetTextureCache.tileTextureLocation(assetsRoot, relativePath);
    }

    public static IGuiTexture preRenderedTileTexture(Path assetsRoot, String relativePath) {
        return LabAssetTextureCache.preRenderedTileTexture(assetsRoot, relativePath);
    }

    public static void clearTileCache(Path assetsRoot, String relativePath) {
        LabAssetTextureCache.clearTileCache(assetsRoot, relativePath);
    }

    public static void ensureAssetsDirs(Path assetsRoot) {
        LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
    }

    public static void deleteAssetFile(Path assetsRoot, String relativePath) {
        LabAssetPathResolver.deleteAssetFile(assetsRoot, relativePath, key -> {
            LabAssetTextureCache.clearTextureCache(key);
            LabAssetTextureCache.clearTileCache(assetsRoot, key);
        });
    }

    public static void renameAssetFile(Path assetsRoot, String relativePath, String targetNameRaw) {
        LabAssetPathResolver.renameAssetFile(assetsRoot, relativePath, targetNameRaw, key -> {
            LabAssetTextureCache.clearTextureCache(key);
            LabAssetTextureCache.clearTileCache(assetsRoot, key);
        });
    }
}
