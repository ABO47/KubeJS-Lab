package com.abo47.kubejslab.client.ui;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.block.model.LabBlockEditAction;
import com.abo47.kubejslab.block.model.LabBlockPayload;
import com.abo47.kubejslab.client.ui.blocks.LabBlockSettingsWidget;
import com.abo47.kubejslab.network.block.C2SBlockEditPacket;
import com.abo47.kubejslab.network.ModNetwork;


final class LabBlockSaver {
    private final LabScreen.LabPanelWidget panel;

    LabBlockSaver(LabScreen.LabPanelWidget panel) {
        this.panel = panel;
    }

    void saveBlock() {
        boolean overriding = panel.blockMode == LabScreen.LabPanelWidget.EditMode.MODIFY
                && panel.blockModifyTarget != null;
        send(overriding ? LabBlockEditAction.MODIFY : LabBlockEditAction.SAVE_NEW,
                overriding ? panel.blockModifyTarget.id() : null);
    }

    void send(LabBlockEditAction action, @Nullable ResourceLocation targetId) {
        LabBlockSettingsWidget settings = panel.blockSettings;
        LabBlockPayload payload = new LabBlockPayload(targetId, settings.getType(), settings.getValues(),
                settings.getTags(), settings.getActions());
        ModNetwork.sendBlockEdit(new C2SBlockEditPacket(action, targetId, payload));
    }
}
