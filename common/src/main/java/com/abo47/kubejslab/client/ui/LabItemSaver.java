package com.abo47.kubejslab.client.ui;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.items.LabItemSettingsWidget;
import com.abo47.kubejslab.item.model.LabItemEditAction;
import com.abo47.kubejslab.item.model.LabItemPayload;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;


final class LabItemSaver {
    private final LabScreen.LabPanelWidget panel;

    LabItemSaver(LabScreen.LabPanelWidget panel) {
        this.panel = panel;
    }

    void saveItem() {
        boolean overriding = panel.itemMode == LabScreen.LabPanelWidget.EditMode.MODIFY
                && panel.itemModifyTarget != null;
        send(overriding ? LabItemEditAction.MODIFY : LabItemEditAction.SAVE_NEW,
                overriding ? panel.itemModifyTarget.id() : null);
    }

    void send(LabItemEditAction action, @Nullable ResourceLocation targetId) {
        LabItemSettingsWidget settings = panel.itemSettings;
        LabItemPayload payload = new LabItemPayload(targetId, settings.getType(), settings.getValues(),
                settings.getTags(), settings.getActions());
        ModNetwork.sendItemEdit(new C2SItemEditPacket(action, targetId, payload));
    }
}