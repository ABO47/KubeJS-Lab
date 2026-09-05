package com.abo47.kubejslab.client.ui.shell;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import com.abo47.kubejslab.client.ui.loot.LootKeys;
import com.abo47.kubejslab.client.ui.loot.LootSettingsWidget;
import com.abo47.kubejslab.loot.model.LootEditAction;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPayload;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.runtime.LootScriptWriter;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.loot.C2SLootEditPacket;


final class LootSaver {
    private final WorkspacePanel panel;

    LootSaver(WorkspacePanel panel) {
        this.panel = panel;
    }

    void saveLoot() {
        boolean overriding = panel.loot.lootMode == WorkspacePanel.EditMode.MODIFY
                && panel.loot.lootModifyTarget != null;
        LootSettingsWidget settings = panel.lootSettings;
        LootFieldValues values = settings.getValues();
        String target = values.targetId();
        if (target == null || target.isBlank()) {
            tell(LootKeys.LOOT_NEEDS_TARGET);
            return;
        }
        boolean drops = false;
        for (LootPoolValues pool : values.pools()) {
            if (pool != null && LootScriptWriter.writesPool(pool)) {
                drops = true;
                break;
            }
        }
        if (!drops) {
            tell(LootKeys.LOOT_NEEDS_DROP);
            return;
        }
        send(overriding ? LootEditAction.MODIFY : LootEditAction.SAVE_NEW,
                overriding ? panel.loot.lootModifyTarget : null);
    }

    void send(LootEditAction action, @Nullable ResourceLocation targetId) {
        LootSettingsWidget settings = panel.lootSettings;
        LootPayload payload = new LootPayload(targetId, settings.getLootType(), settings.getValues(),
                settings.getTags(), settings.getActions());
        NetworkRegistry.sendLootEdit(new C2SLootEditPacket(action, targetId, payload));
    }

    private static void tell(String key) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.translatable(key), false);
        }
    }
}
