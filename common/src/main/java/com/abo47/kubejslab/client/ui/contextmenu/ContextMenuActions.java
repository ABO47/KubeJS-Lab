package com.abo47.kubejslab.client.ui.contextmenu;

import net.minecraft.client.resources.language.I18n;


public final class ContextMenuActions {
    private ContextMenuActions() {
    }

    public static ContextAction action(String key, String iconKey, ActionTone tone, Runnable action) {
        return new ContextAction(I18n.get(key), iconKey, tone, action);
    }
}