package com.abo47.kubejslab.platform;

import java.util.Objects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;

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
        public void registerNetwork() {
        }

        @Override
        public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        }

        @Override
        public void sendOpenRequest() {
        }

        @Override
        public void sendRecipeEdit(C2SRecipeEditPacket packet) {
        }

        @Override
        public void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet) {
        }
    }
}
