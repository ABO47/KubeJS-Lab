package com.abo47.kubejslab.client.ui.base;


public interface LabPopupProvider {
    boolean isOpen();

    boolean isPopupOver(double mouseX, double mouseY);

    void closePopup();

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta);
}