package com.abo47.kubejslab.client.ui.assets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.picker.LabSearchFilter;

final class LabAssetSearchIndex {
    private LabAssetSearchIndex() {
    }

    static List<LabAssetLibrary.AssetEntry> listAssetEntries(Path assetsRoot, String relativeDir) {
        List<LabAssetLibrary.AssetEntry> result = new ArrayList<>();
        try {
            LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
            Path dir = LabAssetPathResolver.resolveDirectory(assetsRoot, relativeDir);
            if (!availableDirectory(assetsRoot, relativeDir, dir)) {
                return result;
            }
            String base = LabAssetPathResolver.normalizeRelative(relativeDir);
            try (Stream<Path> files = Files.list(dir)) {
                files.forEach(path -> addDirectEntry(result, base, path));
            }
            sortByName(result);
        } catch (Exception exception) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset list failed root={} dir={} error={}", assetsRoot, relativeDir, exception.toString());
        }
        return result;
    }

    static List<LabAssetLibrary.AssetEntry> searchAssetEntries(Path assetsRoot, String relativeDir, String query) {
        String normalizedQuery = LabSearchFilter.normalize(query);
        if (normalizedQuery.isBlank()) {
            return listAssetEntries(assetsRoot, relativeDir);
        }
        Map<String, LabAssetLibrary.AssetEntry> result = new LinkedHashMap<>();
        try {
            LabAssetPathResolver.ensureAssetsDirs(assetsRoot);
            Path dir = LabAssetPathResolver.resolveDirectory(assetsRoot, relativeDir);
            if (!availableDirectory(assetsRoot, relativeDir, dir)) {
                return List.of();
            }
            String base = LabAssetPathResolver.normalizeRelative(relativeDir);
            try (Stream<Path> direct = Files.list(dir)) {
                direct.forEach(path -> addMatchingDirectEntry(result, base, path, normalizedQuery));
            }
            try (Stream<Path> nested = Files.walk(dir)) {
                nested.filter(path -> !path.equals(dir))
                        .filter(Files::isRegularFile)
                        .forEach(path -> addMatchingNestedEntry(result, base, dir, path, normalizedQuery));
            }
        } catch (Exception exception) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset search failed root={} dir={} query={} error={}",
                    assetsRoot, relativeDir, normalizedQuery, exception.toString());
        }
        List<LabAssetLibrary.AssetEntry> values = new ArrayList<>(result.values());
        sortByRelativePath(values);
        return values;
    }

    private static boolean availableDirectory(Path assetsRoot, String relativeDir, Path dir) {
        String key = assetsRoot + "|" + relativeDir;
        if (dir == null) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset directory skipped root={} dir={} reason=invalid_dir", assetsRoot, relativeDir);
            return false;
        }
        if (!Files.exists(dir)) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset directory skipped root={} dir={} resolved={} reason=missing_dir",
                    assetsRoot, relativeDir, dir);
            return false;
        }
        if (!Files.isDirectory(dir)) {
            KubeJSLab.LOGGER.warn("[Lab:UI] asset directory skipped root={} dir={} resolved={} reason=not_directory",
                    assetsRoot, relativeDir, dir);
            return false;
        }
        return true;
    }

    private static void addDirectEntry(List<LabAssetLibrary.AssetEntry> result, String base, Path path) {
        String name = path.getFileName().toString();
        boolean isDir = Files.isDirectory(path);
        if (isDir && LabAssetPathResolver.isStubAssetDirectory(name)) {
            return;
        }
        if (!isDir && !LabAssetPathResolver.isSupportedAsset(base, name)) {
            return;
        }
        String rel = base.isBlank() ? name : (base + "/" + name);
        result.add(new LabAssetLibrary.AssetEntry(name, rel, isDir));
    }

    private static void addMatchingDirectEntry(Map<String, LabAssetLibrary.AssetEntry> result, String base, Path path, String query) {
        String name = path.getFileName().toString();
        boolean isDir = Files.isDirectory(path);
        if (isDir && LabAssetPathResolver.isStubAssetDirectory(name)) {
            return;
        }
        if (!isDir && !LabAssetPathResolver.isSupportedAsset(base, name)) {
            return;
        }
        String rel = base.isBlank() ? name : (base + "/" + name);
        if (LabSearchFilter.matches(query, rel, name)) {
            result.putIfAbsent(rel, new LabAssetLibrary.AssetEntry(name, rel, isDir));
        }
    }

    private static void addMatchingNestedEntry(Map<String, LabAssetLibrary.AssetEntry> result, String base, Path dir, Path path, String query) {
        String name = path.getFileName().toString();
        if (!LabAssetPathResolver.isSupportedAsset(base, name)) {
            return;
        }
        String relativeFromDir = dir.relativize(path).toString().replace('\\', '/');
        String rel = base.isBlank() ? relativeFromDir : (base + "/" + relativeFromDir);
        if (LabSearchFilter.matches(query, rel, name)) {
            result.putIfAbsent(rel, new LabAssetLibrary.AssetEntry(name, rel, false));
        }
    }

    private static void sortByName(List<LabAssetLibrary.AssetEntry> result) {
        result.sort((a, b) -> {
            if (a.directory() != b.directory()) {
                return a.directory() ? -1 : 1;
            }
            return a.name().compareToIgnoreCase(b.name());
        });
    }

    private static void sortByRelativePath(List<LabAssetLibrary.AssetEntry> result) {
        result.sort((a, b) -> {
            if (a.directory() != b.directory()) {
                return a.directory() ? -1 : 1;
            }
            return a.relativePath().compareToIgnoreCase(b.relativePath());
        });
    }
}
