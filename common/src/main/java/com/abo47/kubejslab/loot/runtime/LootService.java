package com.abo47.kubejslab.loot.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.loot.model.LootAction;
import com.abo47.kubejslab.loot.model.LootEditAction;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPayload;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.model.LootState;
import com.abo47.kubejslab.loot.model.LootStatus;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.loot.S2CLootStatePacket;
import com.abo47.kubejslab.reload.ReloadKind;
import com.abo47.kubejslab.workspace.ServerCommands;
import com.abo47.kubejslab.workspace.UniqueIds;
import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class LootService {

    private static final Map<ResourceLocation, LootSaveEntry> STATE = new LinkedHashMap<>();

    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();

    private static boolean stateLoaded;

    public static final String LOOT_TYPE_BLOCK = "block";

    public static final String LOOT_TYPE_ENTITY = "entity";

    public static final String LOOT_TYPE_CHEST = "chest";

    public static final String LOOT_TYPE_FISHING = "fishing";

    public static final String LOOT_TYPE_GIFT = "gift";

    public static final String LOOT_TYPE_GENERIC = "generic";

    private LootService() {
    }

    public static void handle(ServerPlayer player, LootEditAction action, ResourceLocation targetId,
            LootPayload payload) {
        KubeJSLab.LOGGER.info(
                "[LootService] handle: action={}, targetId={}, lootType={}, target={}, tags={}, actions={}",
                action, targetId, payload.lootType(), payload.values().targetId(), payload.tags().size(),
                payload.actions().size());
        loadStateIfNeeded();
        try {
            switch (action) {
                case SAVE_NEW -> saveNew(payload);
                case MODIFY -> modify(targetId, payload);
                case DUPLICATE -> duplicate(targetId);
                case DISABLE -> disable(player.getServer(), targetId, payload);
                case ENABLE -> enable(targetId);
                case RESET -> reset(targetId);
                case DELETE -> delete(targetId);
            }
            LootStateIo.save(STATE);
            LootScriptWriter.writeServerScript(STATE);
            MinecraftServer server = player.getServer();
            ServerCommands.kubejsStartupReload(server);
            ServerCommands.reloadKind(server, ReloadKind.LOOT);
            KubeJSLab.LOGGER.info("[LootService] sent /kubejs reload startup_scripts and selective loot reload after {}", action);
            NetworkRegistry.sendLootState(player, statePacket());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to save loot: " + e.getMessage()));
        }
    }

    public static S2CLootStatePacket statePacket() {
        loadStateIfNeeded();
        Map<ResourceLocation, LootState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, LootSaveEntry> entry : STATE.entrySet()) {
            LootSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new LootState(entry.getKey(), e.lootType(), e.status(),
                    e.name(), e.wasModified(), e.values(), e.tags(), e.actions()));
        }
        return new S2CLootStatePacket(states);
    }

    private static void saveNew(LootPayload payload) throws IOException {
        String lootType = payload.lootType();
        if (lootType == null || lootType.isBlank()) {
            throw new IllegalArgumentException("Loot type is required");
        }
        String targetIdStr = payload.values().targetId();
        if (targetIdStr == null || targetIdStr.isBlank()) {
            throw new IllegalArgumentException("Target ID is required");
        }
        requireDroppableEntry(payload);
        String baseName = UniqueIds.slugify(targetIdStr);
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Target ID is required");
        }
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LootSaveEntry(lootType, LootStatus.CREATED, targetIdStr, false, payload.values(),
                payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        KubeJSLab.LOGGER.info("[LootService] SAVE_NEW created {} for {}", id, targetIdStr);
    }

    private static void modify(ResourceLocation targetId, LootPayload payload) {
        if (targetId == null) {
            throw new IllegalArgumentException("Target loot table is required");
        }
        String target = payload.values().targetId();
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("Target ID is required");
        }
        requireDroppableEntry(payload);
        LootSaveEntry existing = STATE.get(targetId);
        String name = existing != null && !existing.name().isBlank() ? existing.name() : target;
        STATE.put(targetId, new LootSaveEntry(payload.lootType(), LootStatus.MODIFIED, name, true, payload.values(),
                payload.tags(), payload.actions()));
        KubeJSLab.LOGGER.info("[LootService] MODIFY wrote {}", targetId);
    }

    private static void requireDroppableEntry(LootPayload payload) {
        for (LootPoolValues pool : payload.values().pools()) {
            if (pool != null && LootScriptWriter.writesPool(pool)) {
                return;
            }
        }
        throw new IllegalArgumentException("At least one loot drop is required");
    }

    private static void duplicate(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LootSaveEntry source = STATE.get(targetId);
        if (source == null) {
            throw new IllegalArgumentException("Source loot not found: " + targetId);
        }
        String baseName = UniqueIds.slugify(source.name());
        if (baseName.isBlank()) {
            baseName = UniqueIds.slugify(source.values().targetId());
        }
        if (baseName.isBlank()) {
            baseName = "loot";
        }
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(baseName + "_copy"),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LootSaveEntry(source.lootType(), LootStatus.CREATED, source.name(), false,
                source.values(), source.tags(), source.actions()));
        SESSION_CREATED_IDS.add(id);
        KubeJSLab.LOGGER.info("[LootService] DUPLICATE created {}", id);
    }

    private static void disable(MinecraftServer server, ResourceLocation targetId, LootPayload payload) {
        if (targetId == null) {
            return;
        }
        LootSaveEntry entry = STATE.get(targetId);
        String lootType = entry != null ? entry.lootType() : null;
        if (lootType == null || lootType.isBlank()) {
            lootType = inferLootType(server, targetId);
        }
        if (lootType == null || lootType.isBlank()) {
            lootType = knownLootType(payload.lootType()) ? payload.lootType() : LOOT_TYPE_BLOCK;
        }
        LootFieldValues values = entry != null && entry.values().targetId() != null
                && !entry.values().targetId().isBlank()
                        ? entry.values()
                        : new LootFieldValues(targetId.toString(), "", List.of(), 0, 0);
        String name = entry != null && !entry.name().isBlank() ? entry.name() : targetId.getPath();
        boolean wasModified = entry != null && entry.wasModified();
        List<LootAction> actions = new ArrayList<>(entry != null ? entry.actions() : List.of());
        if (!actions.contains(LootAction.NO_EXPLOSION_DROP)) {
            actions.add(LootAction.NO_EXPLOSION_DROP);
        }
        STATE.put(targetId, new LootSaveEntry(lootType, LootStatus.DISABLED, name, wasModified, values,
                entry != null ? entry.tags() : List.of(), actions));
        KubeJSLab.LOGGER.info("[LootService] DISABLE {}", targetId);
    }

    private static boolean knownLootType(String lootType) {
        return LOOT_TYPE_BLOCK.equals(lootType) || LOOT_TYPE_ENTITY.equals(lootType)
                || LOOT_TYPE_CHEST.equals(lootType) || LOOT_TYPE_FISHING.equals(lootType)
                || LOOT_TYPE_GIFT.equals(lootType) || LOOT_TYPE_GENERIC.equals(lootType);
    }

    private static String inferLootType(MinecraftServer server, ResourceLocation targetId) {
        if (server == null) {
            return null;
        }
        if (server.registryAccess().registryOrThrow(Registries.ENTITY_TYPE).containsKey(targetId)) {
            return LOOT_TYPE_ENTITY;
        }
        if (server.registryAccess().registryOrThrow(Registries.BLOCK).containsKey(targetId)) {
            return LOOT_TYPE_BLOCK;
        }
        return null;
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LootSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            List<LootAction> actions = new ArrayList<>(entry.actions());
            actions.remove(LootAction.NO_EXPLOSION_DROP);
            STATE.put(targetId, new LootSaveEntry(entry.lootType(), LootStatus.MODIFIED, entry.name(), true,
                    entry.values(), entry.tags(), actions));
        } else {
            STATE.remove(targetId);
        }
        KubeJSLab.LOGGER.info("[LootService] ENABLE {}", targetId);
    }

    private static void reset(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        STATE.remove(targetId);
        KubeJSLab.LOGGER.info("[LootService] RESET {}", targetId);
    }

    private static void delete(ResourceLocation targetId) {
        if (targetId == null || !WorkspacePaths.isLabOwned(targetId)) {
            return;
        }
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
        KubeJSLab.LOGGER.info("[LootService] DELETE {}", targetId);
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        STATE.putAll(LootStateIo.load());
    }
}
