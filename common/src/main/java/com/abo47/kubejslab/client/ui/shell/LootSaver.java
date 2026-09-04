package com.abo47.kubejslab.client.ui.shell;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.loot.LootSettingsWidget;
import com.abo47.kubejslab.loot.model.LootEditAction;
import com.abo47.kubejslab.loot.model.LootPayload;
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
        send(overriding ? LootEditAction.MODIFY : LootEditAction.SAVE_NEW,
                overriding ? panel.loot.lootModifyTarget : null);
    }

    void send(LootEditAction action, @Nullable ResourceLocation targetId) {
        LootSettingsWidget settings = panel.lootSettings;
        LootPayload payload = new LootPayload(targetId, settings.getLootType(), settings.getValues(),
                settings.getTags(), settings.getActions());
        NetworkRegistry.sendLootEdit(new C2SLootEditPacket(action, targetId, payload));
    }
}
