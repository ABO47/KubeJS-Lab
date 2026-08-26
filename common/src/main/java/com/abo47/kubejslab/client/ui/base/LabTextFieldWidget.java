package com.abo47.kubejslab.client.ui.base;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;


public class LabTextFieldWidget extends TextFieldWidget {
    private boolean handlingTextChange;

    public LabTextFieldWidget(int xPosition, int yPosition, int width, int height,
            Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(xPosition, yPosition, width, height, textSupplier, textResponder);
    }

    @Override
    protected void onTextChanged(String newTextString) {
        if (handlingTextChange) {
            return;
        }
        handlingTextChange = true;
        try {
            super.onTextChanged(newTextString);
        } finally {
            handlingTextChange = false;
        }
    }
}
