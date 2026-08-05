package com.abo47.kubejslab.recipe.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.architectury.platform.Platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;
import com.abo47.kubejslab.recipe.model.LabRecipeStateEntry;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;

public final class LabRecipeService {
    private static final String KUBEJS_NAMESPACE = "kubejs";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, LabRecipeStateEntry> STATE = new LinkedHashMap<>();
    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();
    private static boolean stateLoaded;

    private LabRecipeService() {
    }

    public static void handle(ServerPlayer player, LabRecipeEditAction action, ResourceLocation targetId,
            LabRecipePayload payload) {
        loadStateIfNeeded();
        try {
            switch (action) {
                case SAVE_NEW -> saveNew(payload);
                case OVERRIDE -> override(targetId, payload);
                case DISABLE -> disable(targetId, payload);
                case ENABLE -> enable(targetId);
                case RESET -> reset(targetId);
                case DELETE -> delete(targetId);
            }
            saveState();
            if (action != LabRecipeEditAction.SAVE_NEW) {
                writeDisabledScript();
            }
            player.getServer().getCommands()
                    .performPrefixedCommand(player.getServer().createCommandSourceStack(), "reload");
            ModNetwork.sendRecipeState(player, statePacket());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static S2CRecipeStatePacket statePacket() {
        return new S2CRecipeStatePacket(new HashMap<>(STATE));
    }

    private static void saveNew(LabRecipePayload payload) throws IOException {
        ItemStack output = payload.output();
        if (output.isEmpty()) {
            return;
        }
        ResourceLocation id = generateId(output);
        Path dir = recipesDir(id);
        Files.createDirectories(dir);
        Path file = dir.resolve(id.getPath() + ".json");
        int suffix = 2;
        while (Files.exists(file) || SESSION_CREATED_IDS.contains(id)) {
            id = new ResourceLocation(id.getNamespace(), id.getPath() + "_" + suffix);
            file = dir.resolve(id.getPath() + ".json");
            suffix++;
        }
        Files.writeString(file, GSON.toJson(buildJson(payload)) + "\n");
        SESSION_CREATED_IDS.add(id);
    }

    private static void override(ResourceLocation targetId, LabRecipePayload payload) throws IOException {
        if (targetId == null) {
            return;
        }
        Path file = fileFor(targetId);
        Files.createDirectories(file.getParent());
        LabRecipeStateEntry entry = STATE.get(targetId);
        if (isLabOwned(targetId) && (entry == null || !entry.wasModified()) && Files.exists(file)) {
            Path backup = backupFor(targetId);
            Files.createDirectories(backup.getParent());
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.writeString(file, GSON.toJson(buildJson(payload)) + "\n");
        STATE.put(targetId,
                new LabRecipeStateEntry(targetId, LabRecipeStatus.MODIFIED, payload.output(), payload.name(), true));
    }

    private static void disable(ResourceLocation targetId, LabRecipePayload payload) throws IOException {
        if (targetId == null) {
            return;
        }
        LabRecipeStateEntry entry = STATE.get(targetId);
        boolean wasModified = entry != null && entry.wasModified();
        STATE.put(targetId, new LabRecipeStateEntry(targetId, LabRecipeStatus.DISABLED, payload.output(), payload.name(),
                wasModified));
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabRecipeStateEntry entry = STATE.get(targetId);
        if (entry != null && entry.wasModified()) {
            STATE.put(targetId, new LabRecipeStateEntry(targetId, LabRecipeStatus.MODIFIED, entry.output(), entry.name(),
                    true));
        } else {
            STATE.remove(targetId);
        }
    }

    private static void reset(ResourceLocation targetId) throws IOException {
        if (targetId == null) {
            return;
        }
        Path file = fileFor(targetId);
        if (isLabOwned(targetId)) {
            Path backup = backupFor(targetId);
            if (Files.exists(backup)) {
                Files.move(backup, file, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.deleteIfExists(file);
            }
        } else {
            Files.deleteIfExists(file);
        }
        STATE.remove(targetId);
    }

    private static void delete(ResourceLocation targetId) throws IOException {
        if (targetId == null || !isLabOwned(targetId)) {
            return;
        }
        Files.deleteIfExists(fileFor(targetId));
        Files.deleteIfExists(backupFor(targetId));
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
    }

    private static void writeDisabledScript() throws IOException {
        StringBuilder sb = new StringBuilder("ServerEvents.recipes(event => {\n");
        STATE.values().stream()
                .filter(e -> e.status() == LabRecipeStatus.DISABLED)
                .map(e -> e.id().toString())
                .sorted()
                .forEach(id -> sb.append("    event.remove({ id: '").append(id).append("' });\n"));
        sb.append("});\n");
        Path dir = kubejsDir().resolve("server_scripts").resolve("lab");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("disabled.js"), sb.toString());
    }

    private static JsonObject buildJson(LabRecipePayload payload) {
        JsonObject json = new JsonObject();
        if (payload.shapeless()) {
            json.addProperty("type", KUBEJS_NAMESPACE + ":shapeless");
            JsonArray ingredients = new JsonArray();
            for (ItemStack stack : payload.grid()) {
                if (!stack.isEmpty()) {
                    ingredients.add(itemJson(stack));
                }
            }
            json.add("ingredients", ingredients);
        } else {
            json.addProperty("type", KUBEJS_NAMESPACE + ":shaped");
            JsonArray pattern = new JsonArray();
            JsonObject key = new JsonObject();
            Map<String, Character> charByItem = new LinkedHashMap<>();
            char nextChar = 'A';
            for (int row = 0; row < 3; row++) {
                StringBuilder rowStr = new StringBuilder();
                for (int col = 0; col < 3; col++) {
                    ItemStack stack = payload.grid()[row * 3 + col];
                    if (stack.isEmpty()) {
                        rowStr.append(' ');
                        continue;
                    }
                    String itemKey = stack.getItem().builtInRegistryHolder().key().location().toString()
                            + (stack.hasTag() ? "|" + stack.getTag() : "");
                    Character c = charByItem.get(itemKey);
                    if (c == null) {
                        c = nextChar++;
                        charByItem.put(itemKey, c);
                    }
                    rowStr.append((char) c);
                    key.add(String.valueOf((char) c), itemJson(stack));
                }
                pattern.add(rowStr.toString());
            }
            json.add("pattern", pattern);
            json.add("key", key);
        }
        json.add("result", itemWithCount(payload.output()));
        return json;
    }

    private static JsonObject itemJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", stack.getItem().builtInRegistryHolder().key().location().toString());
        if (stack.hasTag()) {
            obj.addProperty("nbt", stack.getTag().toString());
        }
        return obj;
    }

    private static JsonObject itemWithCount(ItemStack stack) {
        JsonObject obj = itemJson(stack);
        if (stack.getCount() > 1) {
            obj.addProperty("count", stack.getCount());
        }
        return obj;
    }

    private static ResourceLocation generateId(ItemStack output) {
        String path = output.getItem().builtInRegistryHolder().key().location().getPath();
        return new ResourceLocation(KUBEJS_NAMESPACE, "lab/" + path);
    }

    private static Path kubejsDir() {
        return Platform.getGameFolder().resolve("kubejs");
    }

    private static Path recipesDir(ResourceLocation id) {
        return kubejsDir().resolve("data").resolve(id.getNamespace()).resolve("recipes");
    }

    private static Path fileFor(ResourceLocation id) {
        return recipesDir(id).resolve(id.getPath() + ".json");
    }

    private static Path backupFor(ResourceLocation id) {
        return kubejsDir().resolve("lab").resolve("backups").resolve(id.getPath() + ".json");
    }

    private static boolean isLabOwned(ResourceLocation id) {
        return KUBEJS_NAMESPACE.equals(id.getNamespace()) && id.getPath().startsWith("lab/");
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        try {
            Path file = kubejsDir().resolve("lab").resolve("state.json");
            if (!Files.exists(file)) {
                return;
            }
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            for (String key : root.keySet()) {
                JsonObject obj = root.getAsJsonObject(key);
                ResourceLocation id = new ResourceLocation(key);
                LabRecipeStatus status = LabRecipeStatus.valueOf(obj.get("status").getAsString());
                ItemStack output = decodeStack(obj);
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                STATE.put(id, new LabRecipeStateEntry(id, status, output, name, wasModified));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveState() throws IOException {
        JsonObject root = new JsonObject();
        for (LabRecipeStateEntry entry : STATE.values()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("status", entry.status().name());
            obj.addProperty("item", entry.output().getItem().builtInRegistryHolder().key().location().toString());
            obj.addProperty("count", entry.output().getCount());
            if (entry.output().hasTag()) {
                obj.addProperty("nbt", entry.output().getTag().toString());
            }
            obj.addProperty("name", entry.name());
            obj.addProperty("wasModified", entry.wasModified());
            root.add(entry.id().toString(), obj);
        }
        Path file = kubejsDir().resolve("lab").resolve("state.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(root));
    }

    private static ItemStack decodeStack(JsonObject obj) {
        String itemId = obj.get("item").getAsString();
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(itemId)));
        stack.setCount(obj.has("count") ? obj.get("count").getAsInt() : 1);
        if (obj.has("nbt")) {
            try {
                stack.setTag(TagParser.parseTag(obj.get("nbt").getAsString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stack;
    }
}
