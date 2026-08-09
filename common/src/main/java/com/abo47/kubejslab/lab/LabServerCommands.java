package com.abo47.kubejslab.lab;

import net.minecraft.server.MinecraftServer;

public final class LabServerCommands {
	public static void reload(MinecraftServer server) {
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "reload");
	}

	public static void kubejsStartupReload(MinecraftServer server) {
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "kubejs reload startup_scripts");
	}

	public static void kubejsTextureReload(MinecraftServer server) {
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "kubejs reload textures");
	}

	private LabServerCommands() {
	}
}