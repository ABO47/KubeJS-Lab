package com.abo47.kubejslab.client.ui.contextmenu;
import com.abo47.kubejslab.client.ui.theme.UiColors;


public enum ActionTone {
    NEUTRAL,
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER;

    public int accentColor() {
        return switch (this) {
            case NEUTRAL -> UiColors.TEXT_MUTED;
            case PRIMARY -> UiColors.INTERACTIVE;
            case SUCCESS -> UiColors.SUCCESS;
            case WARNING -> UiColors.WARNING;
            case DANGER -> UiColors.ERROR;
        };
    }
}
