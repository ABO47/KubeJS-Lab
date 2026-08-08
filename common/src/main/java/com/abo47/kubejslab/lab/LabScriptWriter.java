package com.abo47.kubejslab.lab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LabScriptWriter {
	public static void write(String scriptType, String fileName, String content) throws IOException {
		Path dir = LabPathResolver.kubejsDir().resolve(scriptType).resolve("lab");
		Files.createDirectories(dir);
		Files.writeString(dir.resolve(fileName), content);
	}

	private LabScriptWriter() {
	}
}