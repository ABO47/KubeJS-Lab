package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.contextmenu.LabActions;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;
import com.abo47.kubejslab.loot.model.LabLootEditAction;
import com.abo47.kubejslab.loot.model.LabLootStatus;


public final class LabLootActions {
    private LabLootActions() {
    }

    public static List<LabContextAction> forEntry(LabLootIndex.LabLootEntry entry, LootActionCallbacks callbacks) {
        List<LabContextAction> actions = new ArrayList<>();
        LabLootStatus status = LabLootStates.statusOf(entry.id());
        boolean labMade = entry.kubejs();
        actions.add(LabActions.action(LabGuiKeys.LAB_LOOT_MODIFY, "editor", LabActionTone.PRIMARY,
                () -> callbacks.openModify(entry)));
        switch (status) {
            case NORMAL, CREATED -> actions.add(LabActions.action(LabGuiKeys.LAB_LOOT_DISABLE, "eye-off",
                    LabActionTone.WARNING, () -> callbacks.send(LabLootEditAction.DISABLE, entry.id())));
            case MODIFIED -> {
                actions.add(LabActions.action(LabGuiKeys.LAB_LOOT_RESET, "repeat", LabActionTone.SUCCESS,
                        () -> callbacks.send(LabLootEditAction.RESET, entry.id())));
                actions.add(LabActions.action(LabGuiKeys.LAB_LOOT_DISABLE, "eye-off", LabActionTone.WARNING,
                        () -> callbacks.send(LabLootEditAction.DISABLE, entry.id())));
            }
            case DISABLED -> actions.add(LabActions.action(LabGuiKeys.LAB_LOOT_ENABLE, "eye", LabActionTone.SUCCESS,
                    () -> callbacks.send(LabLootEditAction.ENABLE, entry.id())));
        }
        if (labMade) {
            actions.add(LabActions.action(LabGuiKeys.LAB_LOOT_DELETE, "delete", LabActionTone.DANGER,
                    () -> callbacks.send(LabLootEditAction.DELETE, entry.id())));
        }
        return actions;
    }

    public interface LootActionCallbacks {
        void openModify(LabLootIndex.LabLootEntry entry);

        void send(LabLootEditAction action, @Nullable ResourceLocation targetId);
    }
}
