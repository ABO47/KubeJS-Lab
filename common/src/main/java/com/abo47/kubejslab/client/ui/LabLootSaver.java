package com.abo47.kubejslab.client.ui;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.loot.LabLootSettingsWidget;
import com.abo47.kubejslab.loot.model.LabLootEditAction;
import com.abo47.kubejslab.loot.model.LabLootPayload;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.loot.C2SLootEditPacket;


final class LabLootSaver {
    private final LabScreen.LabPanelWidget panel;

    LabLootSaver(LabScreen.LabPanelWidget panel) {
        this.panel = panel;
    }

    void saveLoot() {
        boolean overriding = panel.lootMode == LabScreen.LabPanelWidget.EditMode.MODIFY
                && panel.lootModifyTarget != null;
        send(overriding ? LabLootEditAction.MODIFY : LabLootEditAction.SAVE_NEW,
                overriding ? panel.lootModifyTarget : null);
    }

    void send(LabLootEditAction action, @Nullable ResourceLocation targetId) {
        LabLootSettingsWidget settings = panel.lootSettings;
        LabLootPayload payload = new LabLootPayload(targetId, settings.getLootType(), settings.getValues(),
                settings.getTags(), settings.getActions());
        ModNetwork.sendLootEdit(new C2SLootEditPacket(action, targetId, payload));
    }
}
