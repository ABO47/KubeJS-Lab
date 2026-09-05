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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.recipe.MachineRegistry;
import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeEditAction;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.RecipePayload;
import com.abo47.kubejslab.recipe.model.RecipeStateEntry;
import com.abo47.kubejslab.recipe.model.RecipeStatus;
import com.abo47.kubejslab.workspace.JsonStateFile;
import com.abo47.kubejslab.workspace.ScriptWriter;
import com.abo47.kubejslab.workspace.ServerCommands;
import com.abo47.kubejslab.workspace.UniqueIds;
import com.abo47.kubejslab.workspace.WorkspacePaths;

import com.google.gson.JsonObject;


public final class RecipeService {
    private static final Map<ResourceLocation, RecipeStateEntry> STATE = new LinkedHashMap<>();
    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();
    private static boolean stateLoaded;

    private RecipeService() {
    }

    public static void handle(ServerPlayer player, RecipeEditAction action, ResourceLocation targetId,
            RecipePayload payload) {
        KubeJSLab.LOGGER.info("[RecipeService] handle: action={}, targetId={}, machineUid={}, inputs={}, outputs={}, name={}, values={}",
                action, targetId, payload.machineUid(), payload.inputs().size(), payload.outputs().size(),
                payload.name(), payload.values());
        loadStateIfNeeded();
        try {
            switch (action) {
                case SAVE_NEW -> saveNew(payload);
                case OVERRIDE -> override(targetId, payload, targetId == null ? null
                        : player.getServer().getRecipeManager().byKey(targetId).orElse(null), player.getServer());
                case DISABLE -> disable(targetId, payload);
                case ENABLE -> enable(targetId);
                case RESET -> reset(targetId);
                case DELETE -> delete(targetId);
            }
            saveState();
            if (action != RecipeEditAction.SAVE_NEW) {
                writeDisabledScript();
            }
            ServerCommands.reload(player.getServer());
            KubeJSLab.LOGGER.info("[RecipeService] sent /reload after {}", action);
            NetworkRegistry.sendRecipeState(player, statePacket());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to save recipe: " + e.getMessage()));
        }
    }

    public static S2CRecipeStatePacket statePacket() {
        loadStateIfNeeded();
        return new S2CRecipeStatePacket(new HashMap<>(STATE));
    }

    private static void saveNew(RecipePayload payload) throws IOException {
        requireUsableInputs(payload);
        requireUsableOutputs(payload);
        ItemStack output = RecipeOutput.displayStack(payload.outputs());
        ResourceLocation id = generateId(output);
        Path file = fileFor(id);
        int suffix = 2;
        while (Files.exists(file) || SESSION_CREATED_IDS.contains(id)) {
            id = new ResourceLocation(id.getNamespace(), id.getPath() + "_" + suffix);
            file = fileFor(id);
            suffix++;
        }
        JsonObject json = buildJson(payload, null);
        if (json == null) {
            throw new IllegalArgumentException("Unsupported recipe type: " + payload.machineUid());
        }
        Files.createDirectories(file.getParent());
        Files.writeString(file, RecipeJson.toPrettyString(json));
        KubeJSLab.LOGGER.info("[RecipeService] SAVE_NEW wrote {} with json={}", file, json);
        SESSION_CREATED_IDS.add(id);
    }

    private static void override(ResourceLocation targetId, RecipePayload payload, Recipe<?> original,
            MinecraftServer server)
            throws IOException {
        if (targetId == null) {
            throw new IllegalArgumentException("Target recipe is required");
        }
        requireUsableInputs(payload);
        Path file = fileFor(targetId);
        Files.createDirectories(file.getParent());
        RecipeStateEntry entry = STATE.get(targetId);
        if (isLabOwned(targetId) && (entry == null || !entry.wasModified()) && Files.exists(file)) {
            Path backup = backupFor(targetId);
            Files.createDirectories(backup.getParent());
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        JsonObject json;
        if (payload.machineUid() == null || !MachineRegistry.supports(payload.machineUid())) {
            requireUsableOutputs(payload);
            JsonObject originalJson = GenericRecipeModifier.originalFor(server, targetId);
            if (originalJson == null) {
                throw new IllegalArgumentException("Original recipe not found: " + targetId);
            }
            json = GenericRecipeModifier.modify(originalJson, payload);
            if (json == null) {
                throw new IllegalArgumentException("Unsupported recipe shape: " + targetId);
            }
        } else {
            RecipeHandler machine = MachineRegistry.get(payload.machineUid());
            if (!hasUsableOutput(payload) && (original == null || !machine.allowsEmptyResult(original))) {
                throw new IllegalArgumentException("At least one output is required");
            }
            json = buildJson(payload, original);
            if (json == null) {
                throw new IllegalArgumentException("Unsupported recipe type: " + payload.machineUid());
            }
        }
        Files.writeString(file, RecipeJson.toPrettyString(json));
        KubeJSLab.LOGGER.info("[RecipeService] OVERRIDE wrote {} with json={}", file, json);
        ItemStack display = RecipeOutput.displayStack(payload.outputs());
        STATE.put(targetId,
                new RecipeStateEntry(targetId, RecipeStatus.MODIFIED, display, payload.name(), true,
                        payload.machineUid()));
    }

    private static void disable(ResourceLocation targetId, RecipePayload payload) throws IOException {
        if (targetId == null) {
            return;
        }
        RecipeStateEntry entry = STATE.get(targetId);
        boolean wasModified = entry != null && entry.wasModified();
        ItemStack display = RecipeOutput.displayStack(payload.outputs());
        STATE.put(targetId, new RecipeStateEntry(targetId, RecipeStatus.DISABLED, display, payload.name(),
                wasModified, payload.machineUid()));
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        RecipeStateEntry entry = STATE.get(targetId);
        if (entry != null && entry.wasModified()) {
            STATE.put(targetId, new RecipeStateEntry(targetId, RecipeStatus.MODIFIED, entry.output(), entry.name(),
                    true, entry.machineUid()));
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
                .filter(e -> e.status() == RecipeStatus.DISABLED)
                .map(e -> e.id().toString())
                .sorted()
                .forEach(id -> sb.append("    event.remove({ id: '").append(id).append("' });\n"));
        sb.append("});\n");
        ScriptWriter.write("server_scripts", "disabled.js", sb.toString());
    }

    private static void requireUsableInputs(RecipePayload payload) {
        if (payload == null || !hasUsableInput(payload)) {
            throw new IllegalArgumentException("At least one input ingredient is required");
        }
    }

    private static void requireUsableOutputs(RecipePayload payload) {
        if (payload == null || !hasUsableOutput(payload)) {
            throw new IllegalArgumentException("At least one output is required");
        }
    }

    private static boolean hasUsableInput(RecipePayload payload) {
        for (RecipeIngredient input : payload.inputs()) {
            if (input != null && !input.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasUsableOutput(RecipePayload payload) {
        for (RecipeOutput output : payload.outputs()) {
            if (output != null && !output.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject buildJson(RecipePayload payload, Recipe<?> original) {
        if (payload.machineUid() == null) {
            return null;
        }
        RecipeHandler machine = MachineRegistry.get(payload.machineUid());
        if (machine == null) {
            return null;
        }
        return machine.buildJson(machine.jsonTypeFor(original), payload.inputs(), payload.outputs(), payload.values());
    }

    private static ResourceLocation generateId(ItemStack output) {
        String path = output.getItem().builtInRegistryHolder().key().location().getPath();
        return UniqueIds.uniqueId(UniqueIds.labId(path),
                taken -> Files.exists(fileFor(taken)) || SESSION_CREATED_IDS.contains(taken));
    }

    private static Path fileFor(ResourceLocation id) {
        return WorkspacePaths.dataFile(id, "recipes");
    }

    private static Path backupFor(ResourceLocation id) {
        return WorkspacePaths.backupFile(id);
    }

    private static boolean isLabOwned(ResourceLocation id) {
        return WorkspacePaths.isLabOwned(id);
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        JsonObject root = JsonStateFile.load(WorkspacePaths.recipeStateFile());
        if (root == null) {
            root = JsonStateFile.load(WorkspacePaths.legacyStateFile());
        }
        if (root == null) {
            return;
        }
        for (String key : root.keySet()) {
            try {
                JsonObject obj = root.getAsJsonObject(key);
                if (!obj.has("item")) {
                    continue;
                }
                ResourceLocation id = new ResourceLocation(key);
                RecipeStatus status = RecipeStatus.valueOf(obj.get("status").getAsString());
                ItemStack output = decodeStack(obj);
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                ResourceLocation machineUid = obj.has("machineUid")
                        ? new ResourceLocation(obj.get("machineUid").getAsString())
                        : null;
                STATE.put(id, new RecipeStateEntry(id, status, output, name, wasModified, machineUid));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveState() throws IOException {
        JsonObject root = new JsonObject();
        for (RecipeStateEntry entry : STATE.values()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("status", entry.status().name());
            obj.addProperty("item", entry.output().getItem().builtInRegistryHolder().key().location().toString());
            obj.addProperty("count", entry.output().getCount());
            if (entry.output().hasTag()) {
                obj.addProperty("nbt", entry.output().getTag().toString());
            }
            obj.addProperty("name", entry.name());
            obj.addProperty("wasModified", entry.wasModified());
            if (entry.machineUid() != null) {
                obj.addProperty("machineUid", entry.machineUid().toString());
            }
            root.add(entry.id().toString(), obj);
        }
        JsonStateFile.save(WorkspacePaths.recipeStateFile(), root);
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