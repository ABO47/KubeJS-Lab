package com.abo47.kubejslab.client.ui.shell;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.items.ItemSettingsWidget;
import com.abo47.kubejslab.item.model.ItemEditAction;
import com.abo47.kubejslab.item.model.ItemPayload;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;


final class ItemSaver {
    private final WorkspacePanel panel;

    ItemSaver(WorkspacePanel panel) {
        this.panel = panel;
    }

    void saveItem() {
        boolean overriding = panel.itemMode == WorkspacePanel.EditMode.MODIFY
                && panel.itemModifyTarget != null;
        send(overriding ? ItemEditAction.MODIFY : ItemEditAction.SAVE_NEW,
                overriding ? panel.itemModifyTarget.id() : null);
    }

    void send(ItemEditAction action, @Nullable ResourceLocation targetId) {
        ItemSettingsWidget settings = panel.itemSettings;
        ItemPayload payload = new ItemPayload(targetId, settings.getType(), settings.getValues(),
                settings.getTags(), settings.getActions());
        NetworkRegistry.sendItemEdit(new C2SItemEditPacket(action, targetId, payload));
    }
}