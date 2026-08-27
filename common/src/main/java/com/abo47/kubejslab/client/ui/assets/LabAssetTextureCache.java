package com.abo47.kubejslab.client.ui.assets;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.DynamicTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import com.abo47.kubejslab.KubeJSLab;

import com.mojang.blaze3d.platform.NativeImage;


final class LabAssetTextureCache {
    private static final Map<String, IGuiTexture> TEXTURE_CACHE = new HashMap<>();
    private static final Map<String, IGuiTexture> THUMBNAIL_CACHE = new HashMap<>();
    private static final int MAX_TILE_SIZE = 64;
    private static final int TILED_SIZE = 256;

    private LabAssetTextureCache() {
    }

    static IGuiTexture chapterBackgroundTexture(Path assetsRoot, String background) {
        return chapterBackgroundTexture(assetsRoot, background, false);
    }

    static IGuiTexture chapterBackgroundTexture(Path assetsRoot, String background, boolean grayscale) {
        if (background == null || background.isBlank() || "default".equals(background)) {
            return null;
        }
        LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
        String cacheKey = textureCacheKey(background, grayscale);
        IGuiTexture cached = TEXTURE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Path path = LabAssetPathResolver.resolveAssetPath(assetsRoot, background);
        if (!availableAssetFile(assetsRoot, background, path)) {
            return null;
        }
        String ext = LabAssetPathResolver.extension(path.getFileName().toString());
        if (!LabAssetPathResolver.hasImageThumbnail(background)) {
            return null;
        }
        IGuiTexture out = "gif".equals(ext) ? loadGifTexture(path, cacheKey, grayscale) : loadStaticTexture(path, cacheKey, grayscale);
        if (out != null) {
            TEXTURE_CACHE.put(cacheKey, out);
        }
        return out;
    }

    static LabAssetLibrary.AssetDimensions assetDimensions(Path assetsRoot, String relativePath) {
        try {
            LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
            Path path = LabAssetPathResolver.resolveAssetPath(assetsRoot, relativePath);
            if (!availableAssetFile(assetsRoot, relativePath, path)) {
                return null;
            }
            String ext = LabAssetPathResolver.extension(path.getFileName().toString());
            if (!LabAssetPathResolver.hasImageThumbnail(relativePath)) {
                return null;
            }
            if ("gif".equals(ext)) {
                LabAssetLibrary.AssetDimensions gifDimensions = gifDimensions(path);
                if (gifDimensions != null) {
                    return gifDimensions;
                }
            }
            try (var stream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(stream);
                if (image != null) {
                    return new LabAssetLibrary.AssetDimensions(image.getWidth(), image.getHeight());
                }
            }
        } catch (Exception exception) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset dimensions failed root={} asset={} error={}",
                    assetsRoot, relativePath, exception.toString());
        }
        return null;
    }

    static IGuiTexture assetThumbnailTexture(Path assetsRoot, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
        IGuiTexture cached = THUMBNAIL_CACHE.get(relativePath);
        if (cached != null) {
            return cached;
        }
        Path path = LabAssetPathResolver.resolveAssetPath(assetsRoot, relativePath);
        if (!availableAssetFile(assetsRoot, relativePath, path)) {
            return null;
        }
        String ext = LabAssetPathResolver.extension(path.getFileName().toString());
        if (!LabAssetPathResolver.hasImageThumbnail(relativePath)) {
            return null;
        }
        IGuiTexture out = "gif".equals(ext) ? loadGifFallbackStatic(path, relativePath + "_thumb", false) : loadStaticTexture(path, relativePath + "_thumb", false);
        if (out != null) {
            THUMBNAIL_CACHE.put(relativePath, out);
        }
        return out;
    }

    static IGuiTexture preRenderedTileTexture(Path assetsRoot, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
        String cacheKey = "tile:" + relativePath;
        IGuiTexture cached = TEXTURE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String sanitized = LabAssetPathResolver.sanitizeAssetId(relativePath);
        Path tilesDir = assetsRoot.resolve("tiles");
        Path tilePath = tilesDir.resolve(sanitized + ".png");

        if (Files.exists(tilePath)) {
            IGuiTexture tex = loadStaticTexture(tilePath, cacheKey, false);
            if (tex != null) {
                TEXTURE_CACHE.put(cacheKey, tex);
                return tex;
            }
        }

        Path sourcePath = LabAssetPathResolver.resolveAssetPath(assetsRoot, relativePath);
        if (sourcePath == null || !Files.exists(sourcePath)) {
            KubeJSLab.LOGGER.warn("[Lab:UI] Tile source not found: root={} asset={} resolved={}", assetsRoot, relativePath, sourcePath);
            return null;
        }
        try {
            BufferedImage sourceBI = ImageIO.read(sourcePath.toFile());
            if (sourceBI == null) {
                KubeJSLab.LOGGER.warn("[Lab:UI] Tile source unreadable: root={} asset={} resolved={}", assetsRoot, relativePath, sourcePath);
                return null;
            }
            int tileW = sourceBI.getWidth();
            int tileH = sourceBI.getHeight();
            if (tileW > MAX_TILE_SIZE || tileH > MAX_TILE_SIZE) {
                float scale = Math.min((float) MAX_TILE_SIZE / tileW, (float) MAX_TILE_SIZE / tileH);
                int newW = Math.max(1, (int) (tileW * scale));
                int newH = Math.max(1, (int) (tileH * scale));
                BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = scaled.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(sourceBI, 0, 0, newW, newH, null);
                g.dispose();
                sourceBI = scaled;
                tileW = newW;
                tileH = newH;
            }
            BufferedImage tiledBI = new BufferedImage(TILED_SIZE, TILED_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = tiledBI.createGraphics();
            for (int tx = 0; tx < TILED_SIZE; tx += tileW) {
                for (int ty = 0; ty < TILED_SIZE; ty += tileH) {
                    tg.drawImage(sourceBI, tx, ty, null);
                }
            }
            tg.dispose();
            Files.createDirectories(tilesDir);
            ImageIO.write(tiledBI, "png", tilePath.toFile());
            KubeJSLab.LOGGER.info("[Lab:UI] Pre-rendered tile saved: root={} asset={} path={}", assetsRoot, relativePath, tilePath);
            try (var in = Files.newInputStream(tilePath)) {
                NativeImage nativeImage = NativeImage.read(in);
                if (nativeImage != null) {
                    ResourceLocation id = ResourceLocation.tryBuild(KubeJSLab.MOD_ID, "tiles/" + sanitized);
                    Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(nativeImage));
                    IGuiTexture result = new DynamicTexture(() -> new ResourceTexture(id));
                    TEXTURE_CACHE.put(cacheKey, result);
                    return result;
                }
            }
        } catch (Exception e) {
            KubeJSLab.LOGGER.warn("[Lab:UI] Failed pre-rendering tile {}", relativePath, e);
        }
        return null;
    }

    static void clearTileCache(Path assetsRoot, String relativePath) {
        TEXTURE_CACHE.remove("tile:" + relativePath);
        if (assetsRoot == null || relativePath == null) {
            return;
        }
        String sanitized = LabAssetPathResolver.sanitizeAssetId(relativePath);
        Path tilePath = assetsRoot.resolve("tiles").resolve(sanitized + ".png");
        try {
            Files.deleteIfExists(tilePath);
        } catch (Exception ignored) {
        }
    }

    static void clearTextureCache(String key) {
        TEXTURE_CACHE.remove(key);
        TEXTURE_CACHE.remove(textureCacheKey(key, true));
        THUMBNAIL_CACHE.remove(key);
    }

    static ResourceLocation staticTextureLocation(Path assetsRoot, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
        Path path = LabAssetPathResolver.resolveAssetPath(assetsRoot, relativePath);
        if (!availableAssetFile(assetsRoot, relativePath, path)) {
            return null;
        }
        String ext = LabAssetPathResolver.extension(path.getFileName().toString());
        if (!LabAssetPathResolver.hasImageThumbnail(relativePath)) {
            return null;
        }
        String sanitized = LabAssetPathResolver.sanitizeAssetId(relativePath + "_static");
        ResourceLocation id = ResourceLocation.tryBuild(KubeJSLab.MOD_ID, "asset/" + sanitized);
        if (id == null) {
            return null;
        }
        try (var stream = Files.newInputStream(path)) {
            NativeImage image = NativeImage.read(stream);
            if (image == null) {
                return null;
            }
            Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(image));
        } catch (Exception e) {
            KubeJSLab.LOGGER.warn("[Lab:UI] Failed loading static texture location {}", relativePath, e);
            return null;
        }
        return id;
    }

    static ResourceLocation tileTextureLocation(Path assetsRoot, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
        String sanitized = LabAssetPathResolver.sanitizeAssetId(relativePath);
        ResourceLocation id = ResourceLocation.tryBuild(KubeJSLab.MOD_ID, "skin_tile/" + sanitized);
        if (id == null) {
            return null;
        }
        Path sourcePath = LabAssetPathResolver.resolveAssetPath(assetsRoot, relativePath);
        if (sourcePath == null || !Files.exists(sourcePath)) {
            KubeJSLab.LOGGER.warn("[Lab:UI] Tile source not found for location: root={} asset={}", assetsRoot, relativePath);
            return null;
        }
        try (var stream = Files.newInputStream(sourcePath)) {
            NativeImage image = NativeImage.read(stream);
            if (image == null) {
                return null;
            }
            Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(image));
        } catch (Exception e) {
            KubeJSLab.LOGGER.warn("[Lab:UI] Failed loading tile texture location {}", relativePath, e);
            return null;
        }
        return id;
    }

    private static LabAssetLibrary.AssetDimensions gifDimensions(Path path) {
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(input, false, false);
                BufferedImage first = reader.read(0);
                if (first != null) {
                    return new LabAssetLibrary.AssetDimensions(first.getWidth(), first.getHeight());
                }
            }
        } catch (Exception exception) {
            KubeJSLab.LOGGER.warn("[Lab:UI] gif dimensions failed path={} error={}", path, exception.toString());
        }
        return null;
    }

    private static IGuiTexture loadStaticTexture(Path path, String key, boolean grayscale) {
        try (var stream = Files.newInputStream(path)) {
            NativeImage image = NativeImage.read(stream);
            if (image == null) {
                return null;
            }
            if (grayscale) {
                applyGrayscale(image);
            }
            ResourceLocation id = ResourceLocation.tryBuild(KubeJSLab.MOD_ID, "asset/" + LabAssetPathResolver.sanitizeAssetId(key));
            Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(image));
            return new DynamicTexture(() -> new ResourceTexture(id));
        } catch (Exception e) {
            KubeJSLab.LOGGER.warn("[Lab:UI] Failed loading texture {}", key, e);
            return null;
        }
    }

    private static IGuiTexture loadGifTexture(Path path, String key, boolean grayscale) {
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            reader.setInput(input, false, false);
            int count = reader.getNumImages(true);
            if (count <= 0) {
                return null;
            }
            List<ResourceTexture> frames = new ArrayList<>();
            List<Integer> delaysMs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                addGifFrame(reader, key, i, frames, delaysMs, grayscale);
            }
            if (frames.isEmpty()) {
                return null;
            }
            return new DynamicTexture(() -> frameAtCurrentTime(frames, delaysMs));
        } catch (Exception e) {
            KubeJSLab.LOGGER.warn("[Lab:UI] Failed loading gif {}", key, e);
            return loadGifFallbackStatic(path, key, grayscale);
        }
    }

    private static void addGifFrame(ImageReader reader, String key, int index, List<ResourceTexture> frames, List<Integer> delaysMs, boolean grayscale) throws java.io.IOException {
        BufferedImage frame = reader.read(index);
        if (frame == null) {
            return;
        }
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        ImageIO.write(frame, "png", pngOut);
        try (var frameIn = new ByteArrayInputStream(pngOut.toByteArray())) {
            NativeImage image = NativeImage.read(frameIn);
            if (image == null) {
                return;
            }
            if (grayscale) {
                applyGrayscale(image);
            }
            ResourceLocation id = ResourceLocation.tryBuild(KubeJSLab.MOD_ID, "asset/" + LabAssetPathResolver.sanitizeAssetId(key + "_f" + index));
            Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(image));
            frames.add(new ResourceTexture(id));
            delaysMs.add(Math.max(40, gifDelayMs(reader, key, index)));
        }
    }

    private static ResourceTexture frameAtCurrentTime(List<ResourceTexture> frames, List<Integer> delaysMs) {
        long loop = 0L;
        for (Integer delay : delaysMs) {
            loop += delay;
        }
        if (loop <= 0L) {
            return frames.get(0);
        }
        long phase = System.currentTimeMillis() % loop;
        long elapsed = 0L;
        for (int i = 0; i < frames.size(); i++) {
            elapsed += delaysMs.get(i);
            if (phase < elapsed) {
                return frames.get(i);
            }
        }
        return frames.get(frames.size() - 1);
    }

    private static String textureCacheKey(String background, boolean grayscale) {
        return grayscale ? background + "#grayscale" : background;
    }

    private static void applyGrayscale(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getPixelRGBA(x, y);
                int red = color & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = (color >>> 16) & 0xFF;
                int alpha = (color >>> 24) & 0xFF;
                int gray = Math.round(red * 0.299f + green * 0.587f + blue * 0.114f);
                int grayscale = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                image.setPixelRGBA(x, y, grayscale);
            }
        }
    }

    private static IGuiTexture loadGifFallbackStatic(Path path, String key, boolean grayscale) {
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                return null;
            }
            ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
            ImageIO.write(image, "png", pngOut);
            try (var frameIn = new ByteArrayInputStream(pngOut.toByteArray())) {
                NativeImage nativeImage = NativeImage.read(frameIn);
                if (nativeImage == null) {
                    return null;
                }
                if (grayscale) {
                    applyGrayscale(nativeImage);
                }
                ResourceLocation id = ResourceLocation.tryBuild(KubeJSLab.MOD_ID, "asset/" + LabAssetPathResolver.sanitizeAssetId(key + "_fallback"));
                Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(nativeImage));
                return new DynamicTexture(() -> new ResourceTexture(id));
            }
        } catch (Exception exception) {
            KubeJSLab.LOGGER.warn("[Lab:UI] gif fallback texture failed path={} key={} error={}",
                    path, key, exception.toString());
            return null;
        }
    }

    private static int gifDelayMs(ImageReader reader, String key, int index) {
        try {
            var metadata = reader.getImageMetadata(index);
            String format = metadata.getNativeMetadataFormatName();
            var root = metadata.getAsTree(format);
            var children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                var node = children.item(i);
                if (!"GraphicControlExtension".equals(node.getNodeName())) {
                    continue;
                }
                var attrs = node.getAttributes();
                var delayNode = attrs.getNamedItem("delayTime");
                if (delayNode == null) {
                    return 100;
                }
                int cs = Integer.parseInt(delayNode.getNodeValue());
                return Math.max(10, cs) * 10;
            }
        } catch (Exception exception) {
            KubeJSLab.LOGGER.warn("[Lab:UI] gif frame delay failed key={} frame={} error={}",
                    key, index, exception.toString());
        }
        return 100;
    }

    private static boolean availableAssetFile(Path assetsRoot, String relativePath, Path path) {
        if (path == null) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset file skipped root={} asset={} reason=invalid_path", assetsRoot, relativePath);
            return false;
        }
        if (!Files.exists(path)) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset file skipped root={} asset={} resolved={} reason=missing_file",
                    assetsRoot, relativePath, path);
            return false;
        }
        if (Files.isDirectory(path)) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset file skipped root={} asset={} resolved={} reason=directory",
                    assetsRoot, relativePath, path);
            return false;
        }
        return true;
    }
}
