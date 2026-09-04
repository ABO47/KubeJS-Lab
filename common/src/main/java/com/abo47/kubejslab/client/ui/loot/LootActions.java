package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.ContextAction;
import com.abo47.kubejslab.client.ui.contextmenu.ContextMenuActions;
import com.abo47.kubejslab.loot.model.LootEditAction;
import com.abo47.kubejslab.loot.model.LootStatus;


public final class LootActions {
    private LootActions() {
    }

    public static List<ContextAction> forEntry(LootIndex.LootEntry entry, LootActionCallbacks callbacks) {
        List<ContextAction> actions = new ArrayList<>();
        LootStatus status = LootStates.statusOf(entry.id());
        boolean labMade = entry.kubejs();
        actions.add(ContextMenuActions.action(LootKeys.LOOT_MODIFY, "editor", ActionTone.PRIMARY,
                () -> callbacks.openModify(entry)));
        switch (status) {
            case NORMAL, CREATED -> actions.add(ContextMenuActions.action(LootKeys.LOOT_DISABLE, "eye-off",
                    ActionTone.WARNING, () -> callbacks.send(LootEditAction.DISABLE, entry.id())));
            case MODIFIED -> {
                actions.add(ContextMenuActions.action(LootKeys.LOOT_RESET, "repeat", ActionTone.SUCCESS,
                        () -> callbacks.send(LootEditAction.RESET, entry.id())));
                actions.add(ContextMenuActions.action(LootKeys.LOOT_DISABLE, "eye-off", ActionTone.WARNING,
                        () -> callbacks.send(LootEditAction.DISABLE, entry.id())));
            }
            case DISABLED -> actions.add(ContextMenuActions.action(LootKeys.LOOT_ENABLE, "eye", ActionTone.SUCCESS,
                    () -> callbacks.send(LootEditAction.ENABLE, entry.id())));
        }
        if (labMade) {
            actions.add(ContextMenuActions.action(LootKeys.LOOT_DUPLICATE, "copy", ActionTone.NEUTRAL,
                    () -> callbacks.send(LootEditAction.DUPLICATE, entry.id())));
            actions.add(ContextMenuActions.action(LootKeys.LOOT_DELETE, "delete", ActionTone.DANGER,
                    () -> callbacks.send(LootEditAction.DELETE, entry.id())));
        }
        return actions;
    }

    public interface LootActionCallbacks {
        void openModify(LootIndex.LootEntry entry);

        void send(LootEditAction action, @Nullable ResourceLocation targetId);
    }
}
