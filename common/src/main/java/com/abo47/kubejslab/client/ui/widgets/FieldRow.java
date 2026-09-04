package com.abo47.kubejslab.client.ui.widgets;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;


public record FieldRow(TextTexture label, Widget control, ItemStackTexture icon, boolean disabled) {
    public FieldRow(TextTexture label, Widget control, ItemStackTexture icon) {
        this(label, control, icon, false);
    }
}
