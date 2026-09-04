package com.abo47.kubejslab.block.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import com.abo47.kubejslab.block.model.BlockAction;
import com.abo47.kubejslab.block.model.BlockEditAction;
import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.block.model.BlockPayload;
import com.abo47.kubejslab.block.model.BlockState;
import com.abo47.kubejslab.block.model.BlockStatus;
import com.abo47.kubejslab.client.ui.shell.UiKeys;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.workspace.ServerCommands;
import com.abo47.kubejslab.workspace.UniqueIds;
import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class BlockService {

    private static final Map<ResourceLocation, BlockSaveEntry> STATE = new LinkedHashMap<>();

    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();

    private static final Set<ResourceLocation> PENDING = new HashSet<>();

    private static boolean stateLoaded;

    public static final List<String> TYPES = List.of("basic", "detector", "slab", "stairs", "fence", "wall",
            "fence_gate", "pressure_plate", "button", "falling", "crop", "cardinal", "carpet");

    static final int CROP_AGES = 8;

    private static final String[] SOUND_TYPES = {
            "", "wood", "stone", "metal", "gravel", "grass", "sand", "glass", "wool", "snow", "crop",
            "slime", "anvil", "ladder", "honey", "amethyst", "deepslate", "netherrack", "candle", "sculk"
    };

    private static final String[] BLOCK_SET_TYPES = {"", "oak", "stone", "iron", "gold", "diamond", "netherite",
            "polished_blackstone"};

    private static final String[] WOOD_TYPES = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "bamboo", "crimson", "warped"};

    static final String[] CREATIVE_TABS = {
            "minecraft:building_blocks", "minecraft:colored_blocks", "minecraft:natural_blocks",
            "minecraft:functional_blocks", "minecraft:redstone_blocks", "minecraft:hotbar",
            "minecraft:search", "minecraft:tools_and_utilities", "minecraft:combat",
            "minecraft:food_and_drinks", "minecraft:ingredients", "minecraft:spawn_eggs",
            "minecraft:op_blocks", "minecraft:misc"
    };

    private BlockService() {
    }

    public static void handle(ServerPlayer player, BlockEditAction action, ResourceLocation targetId,
            BlockPayload payload) {
        loadStateIfNeeded();
        try {
            switch (action) {
                case SAVE_NEW -> saveNew(payload);
                case MODIFY -> modify(targetId, payload);
                case DUPLICATE -> duplicate(targetId);
                case DISABLE -> disable(targetId);
                case ENABLE -> enable(targetId);
                case RESET -> reset(targetId);
                case DELETE -> delete(targetId);
            }
            BlockStateIo.save(STATE);
            BlockScriptWriter.writeStartupScript(STATE);
            BlockScriptWriter.writeModificationScript(STATE);
            BlockScriptWriter.writeServerScript(STATE);
            BlockScriptWriter.writeCreativeHideScript(STATE);
            BlockScriptWriter.writeClientScript(STATE);
            MinecraftServer server = player.getServer();
            ServerCommands.kubejsStartupReload(server);
            ServerCommands.reload(server);
            if (BlockTextures.copyTextures(STATE)) {
                ServerCommands.kubejsTextureReload(server);
            }
            NetworkRegistry.sendBlockState(player, statePacket());
            if (!PENDING.isEmpty()) {
                player.sendSystemMessage(Component.translatable(UiKeys.CHAT_RESTART_REQUIRED));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to save block: " + e.getMessage()));
        }
    }

    public static S2CBlockStatePacket statePacket() {
        loadStateIfNeeded();
        Map<ResourceLocation, BlockState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : STATE.entrySet()) {
            BlockSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new BlockState(entry.getKey(), e.type(), e.status(),
                    PENDING.contains(entry.getKey()), e.name(), e.wasModified(), e.values(), e.tags(), e.actions()));
        }
        List<ResourceLocation> pendingOnly = new ArrayList<>();
        for (ResourceLocation id : PENDING) {
            if (!states.containsKey(id)) {
                pendingOnly.add(id);
            }
        }
        return new S2CBlockStatePacket(states, pendingOnly);
    }

    private static void saveNew(BlockPayload payload) {
        if (payload.type().isBlank()) {
            throw new IllegalArgumentException("Block type is required");
        }
        String baseName = UniqueIds.slugify(payload.values().displayName());
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Block display name is required");
        }
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new BlockSaveEntry(payload.type(), BlockStatus.CREATED,
                payload.values().displayName(), false, payload.values(), payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
    }

    private static void modify(ResourceLocation targetId, BlockPayload payload) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry existing = STATE.get(targetId);
        String name = payload.values().displayName().isBlank() && existing != null ? existing.name()
                : payload.values().displayName();
        STATE.put(targetId, new BlockSaveEntry(payload.type(), BlockStatus.MODIFIED, name, true,
                payload.values(), payload.tags(), payload.actions()));
        PENDING.add(targetId);
    }

    private static void duplicate(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry source = STATE.get(targetId);
        if (source == null) {
            throw new IllegalArgumentException("Source block not found: " + targetId);
        }
        String base = targetId.getPath().substring("lab/".length()) + "_copy";
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(base),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new BlockSaveEntry(source.type(), BlockStatus.CREATED, source.name(), false,
                source.values(), source.tags(), source.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
    }

    private static void disable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            entry = new BlockSaveEntry("basic", BlockStatus.NORMAL, targetId.getPath(), false,
                    BlockFieldValues.defaults(), List.of(), List.of());
        }
        STATE.put(targetId, new BlockSaveEntry(entry.type(), BlockStatus.DISABLED, entry.name(),
                entry.wasModified(), entry.values(), entry.tags(), withHideActions(entry.actions(), true)));
        PENDING.add(targetId);
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            STATE.put(targetId, new BlockSaveEntry(entry.type(), BlockStatus.MODIFIED, entry.name(),
                    true, entry.values(), entry.tags(), withHideActions(entry.actions(), false)));
        } else {
            STATE.remove(targetId);
        }
        PENDING.add(targetId);
    }

    private static List<BlockAction> withHideActions(List<BlockAction> source, boolean set) {
        List<BlockAction> actions = new ArrayList<>(source);
        for (BlockAction action : List.of(BlockAction.HIDE_CREATIVE_TAB, BlockAction.REMOVE_RECIPES,
                BlockAction.HIDE_VIEWER)) {
            if (set && !actions.contains(action)) {
                actions.add(action);
            } else if (!set) {
                actions.remove(action);
            }
        }
        return actions;
    }

    private static void reset(ResourceLocation targetId) throws IOException {
        if (targetId == null) {
            return;
        }
        STATE.remove(targetId);
        PENDING.add(targetId);
        if (!WorkspacePaths.isLabOwned(targetId)) {
            BlockTextures.deleteCopiedTextures(targetId.getPath());
        }
    }

    private static void delete(ResourceLocation targetId) throws IOException {
        if (targetId == null || !WorkspacePaths.isLabOwned(targetId)) {
            return;
        }
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
        PENDING.remove(targetId);
        BlockTextures.deleteCopiedTextures(targetId.getPath());
    }

    public static String[] soundTypes() {
        return SOUND_TYPES;
    }

    public static String[] blockSetTypes() {
        return BLOCK_SET_TYPES;
    }

    public static String[] woodTypes() {
        return WOOD_TYPES;
    }

    public static String[] creativeTabs() {
        return CREATIVE_TABS;
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        STATE.putAll(BlockStateIo.load());
    }
}
