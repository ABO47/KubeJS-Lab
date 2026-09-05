package com.abo47.kubejslab.client.ui.machines;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;

import com.abo47.kubejslab.client.ui.shell.UiKeys;
import com.abo47.kubejslab.client.ui.theme.UiColors;


final class DropdownRowTextures {
    private DropdownRowTextures() {
    }

    static List<Textures> forMachines(List<MachineView> machines, int width) {
        List<Textures> result = new ArrayList<>(machines.size());
        for (MachineView machine : machines) {
            result.add(new Textures(
                    new ItemStackTexture(machine.icon()),
                    new TextTexture(displayName(machine), UiColors.TEXT_PRIMARY)
                            .setWidth(width - 22)
                            .setType(TextTexture.TextType.LEFT_HIDE),
                    new TextTexture(displayName(machine), UiColors.TEXT_MUTED)
                            .setWidth(width - 22)
                            .setType(TextTexture.TextType.LEFT_HIDE)));
        }
        return result;
    }

    static String displayName(MachineView machine) {
        if (machine.supported()) {
            return machine.name();
        }
        return machine.name() + " (" + I18n.get(UiKeys.NOT_SUPPORTED) + ")";
    }

    static final class Textures {
        final ItemStackTexture icon;
        final TextTexture nameSelected;
        final TextTexture nameNormal;

        Textures(ItemStackTexture icon, TextTexture nameSelected, TextTexture nameNormal) {
            this.icon = icon;
            this.nameSelected = nameSelected;
            this.nameNormal = nameNormal;
        }
    }
}