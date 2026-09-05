package com.abo47.kubejslab.platform;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.block.C2SBlockEditPacket;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.network.loot.C2SLootEditPacket;
import com.abo47.kubejslab.network.loot.C2SLootPrefillPacket;
import com.abo47.kubejslab.network.loot.S2CLootPrefillPacket;
import com.abo47.kubejslab.network.loot.S2CLootStatePacket;
import com.abo47.kubejslab.network.loot.S2CLootTableListPacket;
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
        public void sendLootEdit(C2SLootEditPacket packet) {
        }

        @Override
        public void sendLootState(ServerPlayer player, S2CLootStatePacket packet) {
        }

        @Override
        public void sendLootPrefill(C2SLootPrefillPacket packet) {
        }

        @Override
        public void sendLootPrefill(ServerPlayer player, S2CLootPrefillPacket packet) {
        }

        @Override
        public void sendLootTableList(ServerPlayer player, S2CLootTableListPacket packet) {
        }

        @Override
        public Optional<FluidStack> readFluidIngredient(IRecipeSlotView view) {
            return Optional.empty();
        }

        @Override
        public void applyRecipeData(RecipeManager manager, Map<ResourceLocation, JsonElement> recipes,
                ResourceManager resources) {
        }
    }
}
