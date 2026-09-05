package com.abo47.kubejslab.workspace;

import net.minecraft.server.MinecraftServer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.reload.ReloadDispatcher;
import com.abo47.kubejslab.reload.ReloadKind;


public final class ServerCommands {
	public static void reload(MinecraftServer server) {
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "reload");
	}

	public static void reloadKind(MinecraftServer server, ReloadKind kind) {
		if (ModConfig.useVanillaReload()) {
			reload(server);
			return;
		}
		try {
			ReloadDispatcher.reload(server, kind);
		} catch (Exception e) {
			KubeJSLab.LOGGER.warn("[{}] selective {} reload failed, falling back to full reload", KubeJSLab.MOD_ID,
					kind, e);
			reload(server);
		}
	}

	public static void kubejsStartupReload(MinecraftServer server) {
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "kubejs reload startup_scripts");
	}

	public static void kubejsTextureReload(MinecraftServer server) {
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "kubejs reload textures");
	}

	private ServerCommands() {
	}
}
