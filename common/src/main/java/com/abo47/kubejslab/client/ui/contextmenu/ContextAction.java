package com.abo47.kubejslab.client.ui.contextmenu;

public record ContextAction(String label, String iconKey, ActionTone tone, Runnable action) {

    public ContextAction {
        if (label == null) {
            label = "";
        }
        if (iconKey == null) {
            iconKey = "";
        }
        if (tone == null) {
            tone = ActionTone.NEUTRAL;
        }
        if (action == null) {
            action = () -> {
            };
        }
    }
}
