package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.ContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuActions;
import com.abo47.kubejslab.item.model.ItemEditAction;
import com.abo47.kubejslab.item.model.ItemStatus;


public final class ItemActions {
    private ItemActions() {
    }

    public static List<ContextAction> forEntry(ItemIndex.ItemEntry entry, ItemActionCallbacks callbacks) {
        List<ContextAction> actions = new ArrayList<>();
        ItemStatus status = ItemStates.statusOf(entry.id());
        boolean labMade = entry.kubejs();
        actions.add(ContextMenuActions.action(ItemKeys.ITEM_MODIFY, "editor", ActionTone.PRIMARY,
                () -> callbacks.openModify(entry)));
        switch (status) {
            case NORMAL, CREATED -> actions.add(ContextMenuActions.action(ItemKeys.ITEM_DISABLE, "eye-off",
                    ActionTone.WARNING, () -> callbacks.send(ItemEditAction.DISABLE, entry.id())));
            case MODIFIED -> {
                actions.add(ContextMenuActions.action(ItemKeys.ITEM_RESET, "repeat", ActionTone.SUCCESS,
                        () -> callbacks.send(ItemEditAction.RESET, entry.id())));
                actions.add(ContextMenuActions.action(ItemKeys.ITEM_DISABLE, "eye-off", ActionTone.WARNING,
                        () -> callbacks.send(ItemEditAction.DISABLE, entry.id())));
            }
            case DISABLED -> actions.add(ContextMenuActions.action(ItemKeys.ITEM_ENABLE, "eye", ActionTone.SUCCESS,
                    () -> callbacks.send(ItemEditAction.ENABLE, entry.id())));
        }
        if (labMade) {
            actions.add(ContextMenuActions.action(ItemKeys.ITEM_DUPLICATE, "copy", ActionTone.NEUTRAL,
                    () -> callbacks.send(ItemEditAction.DUPLICATE, entry.id())));
            actions.add(ContextMenuActions.action(ItemKeys.ITEM_DELETE, "delete", ActionTone.DANGER,
                    () -> callbacks.send(ItemEditAction.DELETE, entry.id())));
        }
        return actions;
    }

    public interface ItemActionCallbacks {
        void openModify(ItemIndex.ItemEntry entry);

        void send(ItemEditAction action, @Nullable ResourceLocation targetId);
    }
}