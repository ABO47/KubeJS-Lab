package com.abo47.kubejslab.client.ui.contextmenu;

import net.minecraft.client.resources.language.I18n;


public final class LabActions {
    private LabActions() {
    }

    public static LabContextAction action(String key, String iconKey, LabActionTone tone, Runnable action) {
        return new LabContextAction(I18n.get(key), iconKey, tone, action);
    }
}