package com.abo47.kubejslab.workspace;

import java.nio.file.Path;

import net.minecraft.resources.ResourceLocation;

import dev.architectury.platform.Platform;


public final class WorkspacePaths {
	public static Path kubejsDir() {
		return Platform.getGameFolder().resolve("kubejs");
	}

	public static Path configDir() {
		return Platform.getGameFolder().resolve("config").resolve("kubejslab");
	}

	public static Path itemStateFile() {
		return configDir().resolve("items.json");
	}

	public static Path blockStateFile() {
		return configDir().resolve("blocks.json");
	}

	public static Path colorPaletteFile() {
		return configDir().resolve("palette.json");
	}

	public static Path recipeStateFile() {
		return configDir().resolve("recipes.json");
	}

	public static Path lootStateFile() {
		return configDir().resolve("loot.json");
	}

	public static Path legacyStateFile() {
		return configDir().resolve("state.json");
	}

	public static Path legacyLabStateFile(String fileName) {
		return kubejsDir().resolve("lab").resolve(fileName);
	}

	public static Path legacyLabBackupFile(ResourceLocation id) {
		return kubejsDir().resolve("lab").resolve("backups").resolve(id.getPath() + ".json");
	}

	public static Path dataFile(ResourceLocation id, String kind) {
		return kubejsDir().resolve("data").resolve(id.getNamespace()).resolve(kind).resolve(id.getPath() + ".json");
	}

	public static Path backupFile(ResourceLocation id) {
		return configDir().resolve("backups").resolve(id.getPath() + ".json");
	}

	public static boolean isLabOwned(ResourceLocation id) {
		return "kubejs".equals(id.getNamespace()) && id.getPath().startsWith("lab/");
	}

	private WorkspacePaths() {
	}
}