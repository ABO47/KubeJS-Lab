package com.abo47.kubejslab.lab;

import java.nio.file.Path;

import net.minecraft.resources.ResourceLocation;

import dev.architectury.platform.Platform;

public final class LabPathResolver {
	public static Path kubejsDir() {
		return Platform.getGameFolder().resolve("kubejs");
	}

	public static Path itemStateFile() {
		return kubejsDir().resolve("lab").resolve("items.json");
	}

	public static Path recipeStateFile() {
		return kubejsDir().resolve("lab").resolve("recipes.json");
	}

	public static Path legacyStateFile() {
		return kubejsDir().resolve("lab").resolve("state.json");
	}

	public static Path dataFile(ResourceLocation id, String kind) {
		return kubejsDir().resolve("data").resolve(id.getNamespace()).resolve(kind).resolve(id.getPath() + ".json");
	}

	public static Path backupFile(ResourceLocation id) {
		return kubejsDir().resolve("lab").resolve("backups").resolve(id.getPath() + ".json");
	}

	public static boolean isLabOwned(ResourceLocation id) {
		return "kubejs".equals(id.getNamespace()) && id.getPath().startsWith("lab/");
	}

	private LabPathResolver() {
	}
}