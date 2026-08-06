package com.abo47.kubejslab.client.ui.base;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

public class LabNumberFieldWidget extends TextFieldWidget {

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
        String stripped = text.replaceAll("[^0-9.,]", "");
        if (stripped.equals(text)) {
            return text;
        }
        if (stripped.equals(getCurrentString())) {
            return text;
        }
        return stripped;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint >= '0' && codePoint <= '9') {
            return super.charTyped(codePoint, modifiers);
        }
        if ((codePoint == '.' || codePoint == ',')
                && !getCurrentString().contains(".") && !getCurrentString().contains(",")) {
            return super.charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    protected void onTextChanged(String newTextString) {
        super.onTextChanged(newTextString);
        setTextColor(LabColors.TEXT_PRIMARY);
    }
}
