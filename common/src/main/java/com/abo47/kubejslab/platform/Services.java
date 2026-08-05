package com.abo47.kubejslab.platform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class Services {
    private static volatile PlatformService platform = new FallbackPlatformService();

    private Services() {
    }

    public static PlatformService platform() {
        return platform;
    }

    public static void setPlatform(PlatformService service) {
        platform = Objects.requireNonNull(service, "service");
    }

    private static final class FallbackPlatformService implements PlatformService {
        @Override
        public Path configDir() {
            return Paths.get("config");
        }

        @Override
        public String loaderName() {
            return "unknown";
        }

        @Override
        public String loaderVersion() {
            return "unknown";
        }

        @Override
        public String modVersion(String modId) {
            return "unknown";
        }

        @Override
        public void registerNetwork() {
        }

        @Override
        public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        }

        @Override
        public void sendOpenRequest() {
        }
    }
}
