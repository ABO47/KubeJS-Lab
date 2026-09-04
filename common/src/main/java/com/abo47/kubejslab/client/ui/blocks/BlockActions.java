package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.block.model.BlockEditAction;
import com.abo47.kubejslab.block.model.BlockStatus;
import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.ContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuActions;


public final class BlockActions {
    private BlockActions() {
    }

    public static List<ContextAction> forEntry(BlockIndex.BlockEntry entry, BlockActionCallbacks callbacks) {
        List<ContextAction> actions = new ArrayList<>();
        BlockStatus status = BlockStates.statusOf(entry.id());
        boolean labMade = entry.kubejs();
        actions.add(ContextMenuActions.action(BlockKeys.BLOCK_MODIFY, "editor", ActionTone.PRIMARY,
                () -> callbacks.openModify(entry)));
        switch (status) {
            case NORMAL, CREATED -> actions.add(ContextMenuActions.action(BlockKeys.BLOCK_DISABLE, "eye-off",
                    ActionTone.WARNING, () -> callbacks.send(BlockEditAction.DISABLE, entry.id())));
            case MODIFIED -> {
                actions.add(ContextMenuActions.action(BlockKeys.BLOCK_RESET, "repeat", ActionTone.SUCCESS,
                        () -> callbacks.send(BlockEditAction.RESET, entry.id())));
                actions.add(ContextMenuActions.action(BlockKeys.BLOCK_DISABLE, "eye-off", ActionTone.WARNING,
                        () -> callbacks.send(BlockEditAction.DISABLE, entry.id())));
            }
            case DISABLED -> actions.add(ContextMenuActions.action(BlockKeys.BLOCK_ENABLE, "eye", ActionTone.SUCCESS,
                    () -> callbacks.send(BlockEditAction.ENABLE, entry.id())));
        }
        if (labMade) {
            actions.add(ContextMenuActions.action(BlockKeys.BLOCK_DUPLICATE, "copy", ActionTone.NEUTRAL,
                    () -> callbacks.send(BlockEditAction.DUPLICATE, entry.id())));
            actions.add(ContextMenuActions.action(BlockKeys.BLOCK_DELETE, "delete", ActionTone.DANGER,
                    () -> callbacks.send(BlockEditAction.DELETE, entry.id())));
        }
        return actions;
    }

    public interface BlockActionCallbacks {
        void openModify(BlockIndex.BlockEntry entry);

        void send(BlockEditAction action, @Nullable ResourceLocation targetId);
    }
}
