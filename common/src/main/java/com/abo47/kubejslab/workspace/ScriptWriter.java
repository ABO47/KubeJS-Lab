package com.abo47.kubejslab.workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public final class ScriptWriter {
	public static void write(String scriptType, String fileName, String content) throws IOException {
		Path dir = WorkspacePaths.kubejsDir().resolve(scriptType).resolve("lab");
		Files.createDirectories(dir);
		Files.writeString(dir.resolve(fileName), content);
	}

	private ScriptWriter() {
	}
}