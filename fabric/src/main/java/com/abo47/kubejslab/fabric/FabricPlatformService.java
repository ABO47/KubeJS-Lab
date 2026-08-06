package com.abo47.kubejslab.fabric;

import java.util.Optional;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.platform.PlatformService;

import io.netty.buffer.Unpooled;

public final class FabricPlatformService implements PlatformService {
    @Override
    public void registerNetwork() {
        FabricNetwork.register();
    }

    @Override
    public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        FabricNetwork.sendToClient(player, serializedHolder, windowId);
    }

    @Override
    public void sendOpenRequest() {
        ClientPlayNetworking.send(FabricNetwork.OPEN_REQUEST, new FriendlyByteBuf(Unpooled.buffer()));
    }

    @Override
    public void sendRecipeEdit(C2SRecipeEditPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ClientPlayNetworking.send(FabricNetwork.RECIPE_EDIT, buf);
    }

    @Override
    public void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, FabricNetwork.STATE_SYNC, buf);
    }

    @Override
    public Optional<FluidStack> readFluidIngredient(IRecipeSlotView view) {
        return view.getIngredients(FabricTypes.FLUID_STACK)
                .findFirst()
                .map(ingredient -> FluidStack.create(ingredient.getFluid(), ingredient.getAmount(),
                        ingredient.getTag().orElse(null)));
    }
}
