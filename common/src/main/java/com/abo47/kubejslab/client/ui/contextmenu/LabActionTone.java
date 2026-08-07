package com.abo47.kubejslab.client.ui.contextmenu;
import com.abo47.kubejslab.client.ui.base.LabColors;


public enum LabActionTone {
    NEUTRAL,
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER;

    public int accentColor() {
        return switch (this) {
            case NEUTRAL -> LabColors.TEXT_MUTED;
            case PRIMARY -> LabColors.INTERACTIVE;
            case SUCCESS -> LabColors.SUCCESS;
            case WARNING -> LabColors.WARNING;
            case DANGER -> LabColors.ERROR;
        };
    }
}
