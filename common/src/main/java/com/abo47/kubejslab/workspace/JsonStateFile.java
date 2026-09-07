package com.abo47.kubejslab.workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public final class JsonStateFile {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static JsonObject load(Path file) {
		if (!Files.exists(file)) {
			return null;
		}
		try {
			return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static JsonObject loadFirstPresent(Path... files) {
		for (Path file : files) {
			JsonObject root = file == null ? null : load(file);
			if (root != null) {
				return root;
			}
		}
		return null;
	}

	public static void save(Path file, JsonObject root) throws IOException {
		Files.createDirectories(file.getParent());
		Files.writeString(file, GSON.toJson(root));
	}

	public static void saveAndClearLegacy(Path file, Path legacyFile, JsonObject root) throws IOException {
		save(file, root);
		if (legacyFile != null) {
			Files.deleteIfExists(legacyFile);
		}
	}

	private JsonStateFile() {
	}
}