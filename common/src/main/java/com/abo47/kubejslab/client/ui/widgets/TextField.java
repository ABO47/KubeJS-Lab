package com.abo47.kubejslab.client.ui.widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;


public class TextField extends TextFieldWidget {
    private boolean handlingTextChange;

    public TextField(int xPosition, int yPosition, int width, int height,
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
