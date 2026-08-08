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
import com.abo47.kubejslab.lab.LabPathResolver;
import com.abo47.kubejslab.lab.LabScriptWriter;
import com.abo47.kubejslab.lab.LabServerCommands;
import com.abo47.kubejslab.lab.LabStateFile;
import com.abo47.kubejslab.lab.LabUniqueNames;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.LabRecipeMachines;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;
import com.abo47.kubejslab.recipe.model.LabRecipeStateEntry;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;

import com.google.gson.JsonObject;

public final class LabRecipeService {
    private static final Map<ResourceLocation, LabRecipeStateEntry> STATE = new LinkedHashMap<>();
    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();
    private static boolean stateLoaded;

    private LabRecipeService() {
    }

    public static void handle(ServerPlayer player, LabRecipeEditAction action, ResourceLocation targetId,
            LabRecipePayload payload) {
        KubeJSLab.LOGGER.info("[LabRecipeService] handle: action={}, targetId={}, machineUid={}, inputs={}, outputs={}, name={}, values={}",
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
            if (action != LabRecipeEditAction.SAVE_NEW) {
                writeDisabledScript();
            }
            LabServerCommands.reload(player.getServer());
            KubeJSLab.LOGGER.info("[LabRecipeService] sent /reload after {}", action);
            ModNetwork.sendRecipeState(player, statePacket());
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

    private static void saveNew(LabRecipePayload payload) throws IOException {
        ItemStack output = LabRecipeOutput.displayStack(payload.outputs());
        if (output.isEmpty()) {
            return;
        }
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
            return;
        }
        Files.createDirectories(file.getParent());
        Files.writeString(file, json.toString());
        KubeJSLab.LOGGER.info("[LabRecipeService] SAVE_NEW wrote {} with json={}", file, json);
        SESSION_CREATED_IDS.add(id);
    }

    private static void override(ResourceLocation targetId, LabRecipePayload payload, Recipe<?> original,
            MinecraftServer server)
            throws IOException {
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
        JsonObject json;
        if (payload.machineUid() == null || !LabRecipeMachines.supports(payload.machineUid())) {
            JsonObject originalJson = GenericRecipeModifier.originalFor(server, targetId);
            json = originalJson == null ? null : GenericRecipeModifier.modify(originalJson, payload);
        } else {
            json = buildJson(payload, original);
        }
        if (json == null) {
            return;
        }
        Files.writeString(file, json.toString());
        KubeJSLab.LOGGER.info("[LabRecipeService] OVERRIDE wrote {} with json={}", file, json);
        ItemStack display = LabRecipeOutput.displayStack(payload.outputs());
        STATE.put(targetId,
                new LabRecipeStateEntry(targetId, LabRecipeStatus.MODIFIED, display, payload.name(), true,
                        payload.machineUid()));
    }

    private static void disable(ResourceLocation targetId, LabRecipePayload payload) throws IOException {
        if (targetId == null) {
            return;
        }
        LabRecipeStateEntry entry = STATE.get(targetId);
        boolean wasModified = entry != null && entry.wasModified();
        ItemStack display = LabRecipeOutput.displayStack(payload.outputs());
        STATE.put(targetId, new LabRecipeStateEntry(targetId, LabRecipeStatus.DISABLED, display, payload.name(),
                wasModified, payload.machineUid()));
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabRecipeStateEntry entry = STATE.get(targetId);
        if (entry != null && entry.wasModified()) {
            STATE.put(targetId, new LabRecipeStateEntry(targetId, LabRecipeStatus.MODIFIED, entry.output(), entry.name(),
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
                .filter(e -> e.status() == LabRecipeStatus.DISABLED)
                .map(e -> e.id().toString())
                .sorted()
                .forEach(id -> sb.append("    event.remove({ id: '").append(id).append("' });\n"));
        sb.append("});\n");
        LabScriptWriter.write("server_scripts", "disabled.js", sb.toString());
    }

    private static JsonObject buildJson(LabRecipePayload payload, Recipe<?> original) {
        if (payload.machineUid() == null) {
            return null;
        }
        LabRecipeMachine machine = LabRecipeMachines.get(payload.machineUid());
        if (machine == null) {
            return null;
        }
        return machine.buildJson(machine.jsonTypeFor(original), payload.inputs(), payload.outputs(), payload.values());
    }

    private static ResourceLocation generateId(ItemStack output) {
        String path = output.getItem().builtInRegistryHolder().key().location().getPath();
        return LabUniqueNames.uniqueId(LabUniqueNames.labId(path),
                taken -> Files.exists(fileFor(taken)) || SESSION_CREATED_IDS.contains(taken));
    }

    private static Path fileFor(ResourceLocation id) {
        return LabPathResolver.dataFile(id, "recipes");
    }

    private static Path backupFor(ResourceLocation id) {
        return LabPathResolver.backupFile(id);
    }

    private static boolean isLabOwned(ResourceLocation id) {
        return LabPathResolver.isLabOwned(id);
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        JsonObject root = LabStateFile.load(LabPathResolver.stateFile());
        if (root == null) {
            return;
        }
        for (String key : root.keySet()) {
            try {
                JsonObject obj = root.getAsJsonObject(key);
                ResourceLocation id = new ResourceLocation(key);
                LabRecipeStatus status = LabRecipeStatus.valueOf(obj.get("status").getAsString());
                ItemStack output = decodeStack(obj);
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                ResourceLocation machineUid = obj.has("machineUid")
                        ? new ResourceLocation(obj.get("machineUid").getAsString())
                        : null;
                STATE.put(id, new LabRecipeStateEntry(id, status, output, name, wasModified, machineUid));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            if (entry.machineUid() != null) {
                obj.addProperty("machineUid", entry.machineUid().toString());
            }
            root.add(entry.id().toString(), obj);
        }
        LabStateFile.save(LabPathResolver.stateFile(), root);
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