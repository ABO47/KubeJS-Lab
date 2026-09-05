package com.abo47.kubejslab.workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.architectury.platform.Platform;


public final class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String CONFIG_FILE_NAME = "kubejslab.json";
	private static final String LEGACY_FILE_NAME = "kubejslab_picker.json";
	private static final String PICKER_KEY = "picker";
	private static final String VANILLA_RELOAD_KEY = "vanilla_reload";

	public record PickerState(int x, int y, boolean minimized) {
	}

	public static Path configFile() {
		return Platform.getConfigFolder().resolve(CONFIG_FILE_NAME);
	}

	public static PickerState loadPicker() {
		return pickerFrom(loadRoot(configFile()));
	}

	public static PickerState loadPicker(final Path file) {
		return pickerFrom(loadRoot(file));
	}

	public static void savePicker(final int x, final int y, final boolean minimized) {
		savePicker(configFile(), x, y, minimized);
	}

	public static void savePicker(final Path file, final int x, final int y, final boolean minimized) {
		final JsonObject root = loadRoot(file);
		final JsonObject picker = new JsonObject();
		picker.addProperty("x", x);
		picker.addProperty("y", y);
		picker.addProperty("minimized", minimized);
		root.add(PICKER_KEY, picker);
		ensureReloadKey(root);
		saveRoot(file, root);
	}

	public static boolean useVanillaReload() {
		return useVanillaReload(configFile());
	}

	public static boolean useVanillaReload(final Path file) {
		final JsonObject root = loadRoot(file);
		return root.has(VANILLA_RELOAD_KEY) && root.get(VANILLA_RELOAD_KEY).isJsonPrimitive()
				&& root.get(VANILLA_RELOAD_KEY).getAsJsonPrimitive().isBoolean()
				&& root.getAsJsonPrimitive(VANILLA_RELOAD_KEY).getAsBoolean();
	}

	public static void setVanillaReload(final boolean vanillaReload) {
		setVanillaReload(configFile(), vanillaReload);
	}

	public static void setVanillaReload(final Path file, final boolean vanillaReload) {
		final JsonObject root = loadRoot(file);
		root.addProperty(VANILLA_RELOAD_KEY, vanillaReload);
		saveRoot(file, root);
	}

	static JsonObject loadRoot(final Path file) {
		if (Files.exists(file)) {
			final JsonObject parsed = parseFile(file);
			if (parsed != null) {
				return parsed;
			}
		}
		final JsonObject migrated = migrateLegacy(file);
		if (migrated != null) {
			return migrated;
		}
		return new JsonObject();
	}

	static void saveRoot(final Path file, final JsonObject root) {
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(root));
		} catch (IOException ignored) {
		}
	}

	private static PickerState pickerFrom(final JsonObject root) {
		try {
			if (!root.has(PICKER_KEY) || !root.get(PICKER_KEY).isJsonObject()) {
				return null;
			}
			final JsonObject picker = root.getAsJsonObject(PICKER_KEY);
			return new PickerState(picker.get("x").getAsInt(), picker.get("y").getAsInt(),
					picker.get("minimized").getAsBoolean());
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	private static void ensureReloadKey(final JsonObject root) {
		if (!root.has(VANILLA_RELOAD_KEY)) {
			root.addProperty(VANILLA_RELOAD_KEY, false);
		}
	}

	private static JsonObject parseFile(final Path file) {
		try {
			return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
		} catch (IOException | RuntimeException e) {
			return null;
		}
	}

	private static JsonObject migrateLegacy(final Path file) {
		if (!CONFIG_FILE_NAME.equals(file.getFileName().toString())) {
			return null;
		}
		final Path legacy = file.resolveSibling(LEGACY_FILE_NAME);
		if (!Files.exists(legacy)) {
			return null;
		}
		final JsonObject root = new JsonObject();
		final JsonObject legacyRoot = parseFile(legacy);
		if (legacyRoot != null) {
			try {
				final JsonObject picker = new JsonObject();
				picker.addProperty("x", legacyRoot.get("x").getAsInt());
				picker.addProperty("y", legacyRoot.get("y").getAsInt());
				picker.addProperty("minimized", legacyRoot.get("minimized").getAsBoolean());
				root.add(PICKER_KEY, picker);
			} catch (RuntimeException ignored) {
			}
		}
		ensureReloadKey(root);
		saveRoot(file, root);
		try {
			Files.deleteIfExists(legacy);
		} catch (IOException ignored) {
		}
		return root;
	}

	private ModConfig() {
	}
}
