package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.contextmenu.LabActions;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;
import com.abo47.kubejslab.item.model.LabItemEditAction;
import com.abo47.kubejslab.item.model.LabItemStatus;


public final class LabItemActions {
    private LabItemActions() {
    }

    public static List<LabContextAction> forEntry(LabItemIndex.LabItemEntry entry, ItemActionCallbacks callbacks) {
        List<LabContextAction> actions = new ArrayList<>();
        LabItemStatus status = LabItemStates.statusOf(entry.id());
        boolean labMade = entry.kubejs();
        actions.add(LabActions.action(LabGuiKeys.LAB_ITEM_MODIFY, "editor", LabActionTone.PRIMARY,
                () -> callbacks.openModify(entry)));
        switch (status) {
            case NORMAL, CREATED -> actions.add(LabActions.action(LabGuiKeys.LAB_ITEM_DISABLE, "eye-off",
                    LabActionTone.WARNING, () -> callbacks.send(LabItemEditAction.DISABLE, entry.id())));
            case MODIFIED -> {
                actions.add(LabActions.action(LabGuiKeys.LAB_ITEM_RESET, "repeat", LabActionTone.SUCCESS,
                        () -> callbacks.send(LabItemEditAction.RESET, entry.id())));
                actions.add(LabActions.action(LabGuiKeys.LAB_ITEM_DISABLE, "eye-off", LabActionTone.WARNING,
                        () -> callbacks.send(LabItemEditAction.DISABLE, entry.id())));
            }
            case DISABLED -> actions.add(LabActions.action(LabGuiKeys.LAB_ITEM_ENABLE, "eye", LabActionTone.SUCCESS,
                    () -> callbacks.send(LabItemEditAction.ENABLE, entry.id())));
        }
        if (labMade) {
            actions.add(LabActions.action(LabGuiKeys.LAB_ITEM_DUPLICATE, "copy", LabActionTone.NEUTRAL,
                    () -> callbacks.send(LabItemEditAction.DUPLICATE, entry.id())));
            actions.add(LabActions.action(LabGuiKeys.LAB_ITEM_DELETE, "delete", LabActionTone.DANGER,
                    () -> callbacks.send(LabItemEditAction.DELETE, entry.id())));
        }
        return actions;
    }

    public interface ItemActionCallbacks {
        void openModify(LabItemIndex.LabItemEntry entry);

        void send(LabItemEditAction action, @Nullable ResourceLocation targetId);
    }
}