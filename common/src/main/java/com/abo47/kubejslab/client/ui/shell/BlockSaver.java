package com.abo47.kubejslab.client.ui.shell;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.block.model.BlockEditAction;
import com.abo47.kubejslab.block.model.BlockPayload;
import com.abo47.kubejslab.client.ui.blocks.BlockSettingsWidget;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.block.C2SBlockEditPacket;


final class BlockSaver {
    private final WorkspacePanel panel;

    BlockSaver(WorkspacePanel panel) {
        this.panel = panel;
    }

    void saveBlock() {
        boolean overriding = panel.blockMode == WorkspacePanel.EditMode.MODIFY
                && panel.blockModifyTarget != null;
        send(overriding ? BlockEditAction.MODIFY : BlockEditAction.SAVE_NEW,
                overriding ? panel.blockModifyTarget.id() : null);
    }

    void send(BlockEditAction action, @Nullable ResourceLocation targetId) {
        BlockSettingsWidget settings = panel.blockSettings;
        BlockPayload payload = new BlockPayload(targetId, settings.getType(), settings.getValues(),
                settings.getTags(), settings.getActions());
        NetworkRegistry.sendBlockEdit(new C2SBlockEditPacket(action, targetId, payload));
    }
}
