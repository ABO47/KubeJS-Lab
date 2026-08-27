package com.abo47.kubejslab.platform;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.block.C2SBlockEditPacket;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;


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

        @Override
        public void sendItemEdit(C2SItemEditPacket packet) {
        }

        @Override
        public void sendItemState(ServerPlayer player, S2CItemStatePacket packet) {
        }

        @Override
        public void sendBlockEdit(C2SBlockEditPacket packet) {
        }

        @Override
        public void sendBlockState(ServerPlayer player, S2CBlockStatePacket packet) {
        }

        @Override
        public Optional<FluidStack> readFluidIngredient(IRecipeSlotView view) {
            return Optional.empty();
        }
    }
}
