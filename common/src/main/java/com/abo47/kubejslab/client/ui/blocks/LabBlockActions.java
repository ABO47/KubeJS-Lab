package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.block.model.LabBlockEditAction;
import com.abo47.kubejslab.block.model.LabBlockStatus;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.contextmenu.LabActions;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;


public final class LabBlockActions {
    private LabBlockActions() {
    }

    public static List<LabContextAction> forEntry(LabBlockIndex.LabBlockEntry entry, BlockActionCallbacks callbacks) {
        List<LabContextAction> actions = new ArrayList<>();
        LabBlockStatus status = LabBlockStates.statusOf(entry.id());
        boolean labMade = entry.kubejs();
        actions.add(LabActions.action(LabGuiKeys.LAB_BLOCK_MODIFY, "editor", LabActionTone.PRIMARY,
                () -> callbacks.openModify(entry)));
        switch (status) {
            case NORMAL, CREATED -> actions.add(LabActions.action(LabGuiKeys.LAB_BLOCK_DISABLE, "eye-off",
                    LabActionTone.WARNING, () -> callbacks.send(LabBlockEditAction.DISABLE, entry.id())));
            case MODIFIED -> {
                actions.add(LabActions.action(LabGuiKeys.LAB_BLOCK_RESET, "repeat", LabActionTone.SUCCESS,
                        () -> callbacks.send(LabBlockEditAction.RESET, entry.id())));
                actions.add(LabActions.action(LabGuiKeys.LAB_BLOCK_DISABLE, "eye-off", LabActionTone.WARNING,
                        () -> callbacks.send(LabBlockEditAction.DISABLE, entry.id())));
            }
            case DISABLED -> actions.add(LabActions.action(LabGuiKeys.LAB_BLOCK_ENABLE, "eye", LabActionTone.SUCCESS,
                    () -> callbacks.send(LabBlockEditAction.ENABLE, entry.id())));
        }
        if (labMade) {
            actions.add(LabActions.action(LabGuiKeys.LAB_BLOCK_DUPLICATE, "copy", LabActionTone.NEUTRAL,
                    () -> callbacks.send(LabBlockEditAction.DUPLICATE, entry.id())));
            actions.add(LabActions.action(LabGuiKeys.LAB_BLOCK_DELETE, "delete", LabActionTone.DANGER,
                    () -> callbacks.send(LabBlockEditAction.DELETE, entry.id())));
        }
        return actions;
    }

    public interface BlockActionCallbacks {
        void openModify(LabBlockIndex.LabBlockEntry entry);

        void send(LabBlockEditAction action, @Nullable ResourceLocation targetId);
    }
}
