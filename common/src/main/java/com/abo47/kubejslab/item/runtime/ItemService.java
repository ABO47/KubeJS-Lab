package com.abo47.kubejslab.item.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.shell.UiKeys;
import com.abo47.kubejslab.item.model.CustomTier;
import com.abo47.kubejslab.item.model.ItemAction;
import com.abo47.kubejslab.item.model.ItemEditAction;
import com.abo47.kubejslab.item.model.ItemFieldValues;
import com.abo47.kubejslab.item.model.ItemPayload;
import com.abo47.kubejslab.item.model.ItemState;
import com.abo47.kubejslab.item.model.ItemStatus;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.workspace.ServerCommands;
import com.abo47.kubejslab.workspace.UniqueIds;
import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class ItemService {

    private static final Map<ResourceLocation, ItemSaveEntry> STATE = new LinkedHashMap<>();

    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();

    private static final Set<ResourceLocation> PENDING = new HashSet<>();

    private static boolean stateLoaded;

    static final Set<String> TOOL_TYPES = Set.of("sword", "pickaxe", "axe", "shovel", "hoe", "shears");

    static final Set<String> ARMOR_TYPES = Set.of("helmet", "chestplate", "leggings", "boots");

    static final String[] CREATIVE_TABS = {
            "minecraft:building_blocks", "minecraft:colored_blocks", "minecraft:natural_blocks",
            "minecraft:functional_blocks", "minecraft:redstone_blocks", "minecraft:hotbar",
            "minecraft:search", "minecraft:tools_and_utilities", "minecraft:combat",
            "minecraft:food_and_drinks", "minecraft:ingredients", "minecraft:spawn_eggs",
            "minecraft:op_blocks", "minecraft:misc"
    };

    private ItemService() {
    }

    public static void handle(ServerPlayer player, ItemEditAction action, ResourceLocation targetId,
            ItemPayload payload) {
        KubeJSLab.LOGGER.info(
                "[ItemService] handle: action={}, targetId={}, type={}, displayName={}, values={}, tags={}, actions={}",
                action, targetId, payload.type(), payload.values().displayName(), payload.values(),
                payload.tags().size(), payload.actions().size());
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
            ItemStateIo.save(STATE);
            ItemScriptWriter.writeStartupScript(STATE);
            ItemScriptWriter.writeModelOverrides(STATE);
            ItemScriptWriter.writeServerScript(STATE);
            ItemScriptWriter.writeClientScript(STATE);
            MinecraftServer server = player.getServer();
            ServerCommands.kubejsStartupReload(server);
            ServerCommands.reload(server);
            if (ItemTextures.copyTextures(STATE)) {
                ServerCommands.kubejsTextureReload(server);
            }
            KubeJSLab.LOGGER.info("[ItemService] sent /kubejs reload startup_scripts and /reload after {}", action);
            NetworkRegistry.sendItemState(player, statePacket());
            if (!PENDING.isEmpty()) {
                player.sendSystemMessage(Component.translatable(UiKeys.CHAT_RESTART_REQUIRED));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to save item: " + e.getMessage()));
        }
    }

    public static S2CItemStatePacket statePacket() {
        loadStateIfNeeded();
        Map<ResourceLocation, ItemState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, ItemSaveEntry> entry : STATE.entrySet()) {
            ItemSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new ItemState(entry.getKey(), e.type(), e.status(), PENDING.contains(entry.getKey()),
                    e.name(), e.wasModified(), e.customTier(), e.values(), e.tags(), e.actions()));
        }
        List<ResourceLocation> pendingOnly = new ArrayList<>();
        for (ResourceLocation id : PENDING) {
            if (!states.containsKey(id)) {
                pendingOnly.add(id);
            }
        }
        return new S2CItemStatePacket(states, pendingOnly);
    }

    private static void saveNew(ItemPayload payload) throws IOException {
        if (payload.type().isBlank()) {
            throw new IllegalArgumentException("Item type is required");
        }
        String baseName = UniqueIds.slugify(payload.values().displayName());
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Item display name is required");
        }
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        CustomTier tier = tierFor(payload, id);
        STATE.put(id, new ItemSaveEntry(payload.type(), ItemStatus.CREATED, payload.values().displayName(),
                false, tier, payload.values(), payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
        KubeJSLab.LOGGER.info("[ItemService] SAVE_NEW created {} with displayName={}", id,
                payload.values().displayName());
    }

    private static void modify(ResourceLocation targetId, ItemPayload payload) {
        if (targetId == null) {
            return;
        }
        ItemSaveEntry existing = STATE.get(targetId);
        String name = payload.values().displayName().isBlank() && existing != null ? existing.name()
                : payload.values().displayName();
        CustomTier tier = existing != null && existing.customTier() != null ? existing.customTier()
                : tierFor(payload, targetId);
        STATE.put(targetId, new ItemSaveEntry(payload.type(), ItemStatus.MODIFIED, name, true, tier,
                payload.values(), payload.tags(), payload.actions()));
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[ItemService] MODIFY wrote {}", targetId);
    }

    private static void duplicate(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        ItemSaveEntry source = STATE.get(targetId);
        if (source == null) {
            throw new IllegalArgumentException("Source item not found: " + targetId);
        }
        String base = targetId.getPath().substring("lab/".length()) + "_copy";
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(base),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new ItemSaveEntry(source.type(), ItemStatus.CREATED, source.name(), false,
                source.customTier(), source.values(), source.tags(), source.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
        KubeJSLab.LOGGER.info("[ItemService] DUPLICATE created {}", id);
    }

    private static void disable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        ItemSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            entry = new ItemSaveEntry("basic", ItemStatus.NORMAL, targetId.getPath(), false, null,
                    ItemFieldValues.defaults(), List.of(), List.of());
        }
        STATE.put(targetId, new ItemSaveEntry(entry.type(), ItemStatus.DISABLED, entry.name(),
                entry.wasModified(), entry.customTier(), entry.values(), entry.tags(),
                withHideActions(entry.actions(), true)));
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[ItemService] DISABLE {}", targetId);
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        ItemSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            STATE.put(targetId, new ItemSaveEntry(entry.type(), ItemStatus.MODIFIED, entry.name(),
                    true, entry.customTier(), entry.values(), entry.tags(),
                    withHideActions(entry.actions(), false)));
        } else {
            STATE.remove(targetId);
        }
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[ItemService] ENABLE {}", targetId);
    }

    private static List<ItemAction> withHideActions(List<ItemAction> source, boolean set) {
        List<ItemAction> actions = new ArrayList<>(source);
        for (ItemAction action : List.of(ItemAction.HIDE_CREATIVE_TAB, ItemAction.REMOVE_RECIPES,
                ItemAction.HIDE_VIEWER)) {
            if (set && !actions.contains(action)) {
                actions.add(action);
            } else if (!set) {
                actions.remove(action);
            }
        }
        return actions;
    }

    private static void reset(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        STATE.remove(targetId);
        PENDING.add(targetId);
        if (!WorkspacePaths.isLabOwned(targetId)) {
            try {
                Files.deleteIfExists(ItemTextures.textureFile(targetId));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path modelFile = WorkspacePaths.kubejsDir().resolve("assets").resolve("minecraft").resolve("models")
                    .resolve("item").resolve(targetId.getPath() + ".json");
            try {
                Files.deleteIfExists(modelFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        KubeJSLab.LOGGER.info("[ItemService] RESET {}", targetId);
    }

    private static void delete(ResourceLocation targetId) throws IOException {
        if (targetId == null || !WorkspacePaths.isLabOwned(targetId)) {
            return;
        }
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
        PENDING.remove(targetId);
        try {
            Files.deleteIfExists(ItemTextures.textureFile(targetId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        KubeJSLab.LOGGER.info("[ItemService] DELETE {}", targetId);
    }

    private static CustomTier tierFor(ItemPayload payload, ResourceLocation id) {
        ItemFieldValues v = payload.values();
        boolean armor = ARMOR_TYPES.contains(payload.type());
        if (armor) {
            boolean any = v.tierDurabilityMultiplier() > 0 || v.tierEnchantValue() > 0
                    || !v.tierRepairIngredient().isBlank() || !v.tierEquipSound().isBlank()
                    || v.tierToughness() > 0 || v.tierKnockbackResistance() > 0 || !v.tierProtections().isBlank();
            if (!any) {
                return null;
            }
            return new CustomTier("lab/" + id.getPath().substring("lab/".length()) + "_tier", true, 0, 0, 0, 0,
                    v.tierEnchantValue(), v.tierRepairIngredient(), v.tierDurabilityMultiplier(),
                    parseProtections(v.tierProtections()), v.tierEquipSound(), v.tierToughness(),
                    v.tierKnockbackResistance());
        }
        boolean any = v.tierUses() > 0 || v.tierSpeed() > 0 || v.tierAttackDamageBonus() != 0 || v.tierLevel() > 0
                || v.tierEnchantValue() > 0 || !v.tierRepairIngredient().isBlank();
        if (!any) {
            return null;
        }
        return new CustomTier("lab/" + id.getPath().substring("lab/".length()) + "_tier", false, v.tierUses(),
                v.tierSpeed(), v.tierAttackDamageBonus(), v.tierLevel(), v.tierEnchantValue(),
                v.tierRepairIngredient(), 0, new int[4], "", 0, 0);
    }

    private static int[] parseProtections(String value) {
        int[] result = new int[4];
        if (value.isBlank()) {
            return result;
        }
        String[] parts = value.split(",");
        for (int i = 0; i < Math.min(4, parts.length); i++) {
            try {
                result[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        STATE.putAll(ItemStateIo.load());
    }
}
