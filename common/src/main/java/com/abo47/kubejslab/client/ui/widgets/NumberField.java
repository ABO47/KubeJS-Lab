package com.abo47.kubejslab.client.ui.widgets;

import com.abo47.kubejslab.client.ui.theme.UiColors;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class NumberField extends TextField {

    public NumberField(int xPosition, int yPosition, int width, int height,
            Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(xPosition, yPosition, width, height, textSupplier, textResponder);
    }

    public static NumberField create(int xPosition, int yPosition, int width, int height,
            Supplier<String> textSupplier, Consumer<String> textResponder) {
        NumberField field = new NumberField(xPosition, yPosition, width, height, textSupplier,
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
        super.onTextChanged(sanitize(newTextString));
        setTextColor(UiColors.TEXT_PRIMARY);
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
            value = 0;
            fractional = false;
        }
        double step = fractional ? 0.25 : 1;
        double next = value + delta * step;
        if (next < 0) {
            next = 0;
        }
        String formatted;
        if (fractional) {
            formatted = String.valueOf(next);
            if (formatted.contains(".")) {
                formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
                if (formatted.isEmpty()) formatted = "0";
            }
        } else {
            formatted = Integer.toString((int) Math.round(next));
        }
        setCurrentString(sanitize(formatted));
        setFocus(true);
        return true;
    }
}
