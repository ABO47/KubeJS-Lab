package com.abo47.kubejslab.workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ModConfigTest {

    @Test
    void pickerRoundTrip(@TempDir Path dir) {
        Path file = dir.resolve("kubejslab.json");
        ModConfig.savePicker(file, 120, 34, true);
        ModConfig.PickerState state = ModConfig.loadPicker(file);
        assertEquals(120, state.x());
        assertEquals(34, state.y());
        assertTrue(state.minimized());
    }

    @Test
    void reloadFlagDefaultsFalse(@TempDir Path dir) {
        Path file = dir.resolve("kubejslab.json");
        assertFalse(ModConfig.useVanillaReload(file));
        assertNull(ModConfig.loadPicker(file));
    }

    @Test
    void reloadFlagRoundTripPreservesPicker(@TempDir Path dir) {
        Path file = dir.resolve("kubejslab.json");
        ModConfig.savePicker(file, 7, 9, false);
        ModConfig.setVanillaReload(file, true);
        assertTrue(ModConfig.useVanillaReload(file));
        ModConfig.PickerState state = ModConfig.loadPicker(file);
        assertEquals(7, state.x());
        assertEquals(9, state.y());
        assertFalse(state.minimized());
    }

    @Test
    void pickerSavePreservesReloadFlag(@TempDir Path dir) {
        Path file = dir.resolve("kubejslab.json");
        ModConfig.setVanillaReload(file, true);
        ModConfig.savePicker(file, 1, 2, false);
        assertTrue(ModConfig.useVanillaReload(file));
    }

    @Test
    void legacyPickerMigrates(@TempDir Path dir) throws IOException {
        Path legacy = dir.resolve("kubejslab_picker.json");
        Files.writeString(legacy, "{\"x\":50,\"y\":12,\"minimized\":true}");
        Path file = dir.resolve("kubejslab.json");
        ModConfig.PickerState state = ModConfig.loadPicker(file);
        assertEquals(50, state.x());
        assertEquals(12, state.y());
        assertTrue(state.minimized());
        assertFalse(ModConfig.useVanillaReload(file));
        assertTrue(Files.exists(file));
        assertFalse(Files.exists(legacy));
    }

    @Test
    void malformedFileFallsBackToDefaults(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("kubejslab.json");
        Files.writeString(file, "not json");
        assertNull(ModConfig.loadPicker(file));
        assertFalse(ModConfig.useVanillaReload(file));
    }
}
