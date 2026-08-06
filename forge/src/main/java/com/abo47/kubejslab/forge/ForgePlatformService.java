package com.abo47.kubejslab.forge;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.platform.PlatformService;

public final class ForgePlatformService implements PlatformService {
    @Override
    public void registerNetwork() {
        ForgeNetwork.register();
    }

    @Override
    public void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId) {
        int length = serializedHolder.readableBytes();
        byte[] payload = new byte[length];
        serializedHolder.readBytes(payload);
        ForgeNetwork.sendToClient(new ForgeNetwork.OpenScreenPacket(windowId, payload), player);
    }

    @Override
    public void sendOpenRequest() {
        ForgeNetwork.sendToServer(new ForgeNetwork.RequestOpenPacket());
    }

    @Override
    public void sendRecipeEdit(C2SRecipeEditPacket packet) {
        ForgeNetwork.sendToServer(packet);
    }

    @Override
    public void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet) {
        ForgeNetwork.sendToClient(packet, player);
    }

    @Override
    public Optional<FluidStack> readFluidIngredient(IRecipeSlotView view) {
        return view.getIngredients(ForgeTypes.FLUID_STACK)
                .findFirst()
                .map(fs -> fs.getTag() == null
                        ? FluidStack.create(fs.getFluid(), fs.getAmount())
                        : FluidStack.create(fs.getFluid(), fs.getAmount(), fs.getTag()));
    }
}
