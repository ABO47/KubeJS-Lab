package com.abo47.kubejslab.client.ui.contextmenu;

import java.util.function.LongSupplier;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class LabContextMenuAnimation {
    private LabContextMenuAnimation() {
    }

    public static WidgetGroup wrap(WidgetGroup content, LongSupplier startMsSupplier) {
        return LabContextMenuPopWidget.menu(content, startMsSupplier);
    }
}
