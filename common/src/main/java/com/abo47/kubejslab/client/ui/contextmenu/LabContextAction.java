package com.abo47.kubejslab.client.ui.contextmenu;

public record LabContextAction(String label, LabActionTone tone, Runnable action) {

    public LabContextAction {
        if (label == null) {
            label = "";
        }
        if (tone == null) {
            tone = LabActionTone.NEUTRAL;
        }
        if (action == null) {
            action = () -> {
            };
        }
    }
}
