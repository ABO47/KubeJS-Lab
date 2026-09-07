package com.abo47.kubejslab.workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public final class ScriptWriter {
	static String labFileName(String fileName) {
		return "kubejslab." + fileName;
	}

	public static void write(String scriptType, String fileName, String content) throws IOException {
		Path dir = WorkspacePaths.kubejsDir().resolve(scriptType);
		Files.createDirectories(dir);
		Files.writeString(dir.resolve(labFileName(fileName)), content);
		Files.deleteIfExists(dir.resolve("lab").resolve(fileName));
	}

	public static void writeOrDelete(String scriptType, String fileName, String content) throws IOException {
		Path dir = WorkspacePaths.kubejsDir().resolve(scriptType);
		Files.createDirectories(dir);
		Files.deleteIfExists(dir.resolve("lab").resolve(fileName));
		Path file = dir.resolve(labFileName(fileName));
		if (content == null || content.isBlank()) {
			Files.deleteIfExists(file);
			return;
		}
		Files.writeString(file, content);
	}

	private ScriptWriter() {
	}
}