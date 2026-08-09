package com.abo47.kubejslab.client.ui.base;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;


public class LabNumberFieldWidget extends TextFieldWidget {

    private boolean handlingTextChange;

    public LabNumberFieldWidget(int xPosition, int yPosition, int width, int height,
            Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(xPosition, yPosition, width, height, textSupplier, textResponder);
    }

    public static LabNumberFieldWidget create(int xPosition, int yPosition, int width, int height,
            Supplier<String> textSupplier, Consumer<String> textResponder) {
        LabNumberFieldWidget field = new LabNumberFieldWidget(xPosition, yPosition, width, height, textSupplier,
                textResponder);
        field.setValidator(field::sanitize);
        return field;
    }

    private String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[^0-9.,]", "");
    }

    @Override
    protected void onTextChanged(String newTextString) {
        if (handlingTextChange) {
            return;
        }
        handlingTextChange = true;
        try {
            super.onTextChanged(sanitize(newTextString));
        } finally {
            handlingTextChange = false;
        }
        setTextColor(LabColors.TEXT_PRIMARY);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        String raw = getRawCurrentString() == null ? "" : getRawCurrentString().trim();
        if (raw.isEmpty()) {
            raw = "0";
        }
        double delta = Math.signum(wheelDelta);
        boolean fractional = raw.contains(".") || raw.contains(",");
        double value = 0;
        try {
            value = Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return true;
        }
        double step = fractional ? 0.25 : 1;
        double next = value + delta * step;
        if (next < 0) {
            next = 0;
        }
        String formatted = fractional ? String.valueOf(next) : Integer.toString((int) Math.round(next));
        setCurrentString(sanitize(formatted));
        return true;
    }
}
