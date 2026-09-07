package com.abo47.kubejslab.client.ui.picker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;


public record SearchQuery(String mod, String tag, boolean onlyDisabled, boolean onlyModified, String text) {
    public static SearchQuery parse(String query) {
        if (query == null || query.isBlank()) {
            return new SearchQuery("", "", false, false, "");
        }
        String mod = "";
        String tag = "";
        boolean onlyDisabled = false;
        boolean onlyModified = false;
        List<String> textTokens = new ArrayList<>();
        for (String rawToken : query.trim().split("\\s+")) {
            if (rawToken.isBlank()) {
                continue;
            }
            String lower = rawToken.toLowerCase(Locale.ROOT);
            if (lower.equals("disabled:") || lower.startsWith("disabled:")) {
                onlyDisabled = true;
                continue;
            }
            if (lower.equals("modified:") || lower.startsWith("modified:")) {
                onlyModified = true;
                continue;
            }
            if (rawToken.startsWith("@")) {
                String modToken = stripBraces(rawToken.substring(1).toLowerCase(Locale.ROOT).trim());
                if (modToken.contains(":")) {
                    int colon = modToken.indexOf(':');
                    String modPart = modToken.substring(0, colon).trim();
                    String rest = modToken.substring(colon + 1).trim();
                    if (!modPart.isBlank()) {
                        mod = modPart;
                    }
                    if (!rest.isBlank()) {
                        textTokens.add(rest);
                    }
                } else if (!modToken.isBlank()) {
                    mod = modToken;
                }
                continue;
            }
            if (rawToken.startsWith("#")) {
                String tagToken = stripBraces(rawToken.substring(1).toLowerCase(Locale.ROOT).trim());
                if (!tagToken.isBlank()) {
                    tag = tagToken;
                }
                continue;
            }
            textTokens.add(rawToken);
        }
        String text = SearchNormalizer.normalizeUserSearch(String.join(" ", textTokens));
        return new SearchQuery(mod.trim(), tag.trim(), onlyDisabled, onlyModified, text);
    }

    private static String stripBraces(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.startsWith("{")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.endsWith("}")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    public boolean matchesMod(ResourceLocation id) {
        if (mod == null || mod.isBlank()) {
            return true;
        }
        return id != null && mod.equals(id.getNamespace().toLowerCase(Locale.ROOT));
    }

    public boolean matchesText(String normalizedId, String normalizedName) {
        if (text == null || text.isBlank()) {
            return true;
        }
        String nid = normalizedId == null ? "" : normalizedId;
        String nname = normalizedName == null ? "" : normalizedName;
        return nid.contains(text) || nname.contains(text);
    }

    public boolean matchesItemTag(ResourceLocation itemId) {
        if (tag == null || tag.isBlank()) {
            return true;
        }
        if (itemId == null) {
            return false;
        }
        try {
            var item = BuiltInRegistries.ITEM.get(itemId);
            if (item == null) {
                return false;
            }
            String needle = tag.toLowerCase(Locale.ROOT);
            for (var tagKey : item.builtInRegistryHolder().tags().toList()) {
                String tagId = tagKey.location().toString().toLowerCase(Locale.ROOT);
                if (tagId.contains(needle)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public boolean matchesBlockTag(ResourceLocation blockId) {
        if (tag == null || tag.isBlank()) {
            return true;
        }
        if (blockId == null) {
            return false;
        }
        try {
            var block = BuiltInRegistries.BLOCK.get(blockId);
            if (block == null) {
                return false;
            }
            String needle = tag.toLowerCase(Locale.ROOT);
            for (var tagKey : block.builtInRegistryHolder().tags().toList()) {
                String tagId = tagKey.location().toString().toLowerCase(Locale.ROOT);
                if (tagId.contains(needle)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }
}
