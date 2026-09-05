package com.abo47.kubejslab.client.ui.widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;


public final class CommitField extends TextField {
    private final Consumer<String> onCommit;

    public CommitField(int x, int y, int w, int h, Supplier<String> supplier, Consumer<String> onCommit) {
        super(x, y, w, h, supplier, null);
        this.onCommit = onCommit;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String value = getText();
            if (value != null && value.isBlank()) {
                return handled;
            }
            if (isFocus() && onCommit != null) {
                onCommit.accept(value.trim());
            }
            return true;
        }
        return handled;
    }

    private String getText() {
        return getRawCurrentString();
    }
}