package com.abo47.kubejslab.forge;

import java.util.List;
import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.platform.PlatformService;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;


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

    @Override
    public ItemStack fluidOutputDisplay(Recipe<?> recipe) {
        if (recipe instanceof IMultiblockRecipe multiblock) {
            List<?> outputs = multiblock.getFluidOutputs();
            if (!outputs.isEmpty()) {
                net.minecraftforge.fluids.FluidStack fluid = (net.minecraftforge.fluids.FluidStack) outputs.get(0);
                Item bucket = fluid.getFluid().getBucket();
                if (bucket != null) {
                    return new ItemStack(bucket);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public String fluidOutputDisplayName(Recipe<?> recipe) {
        if (recipe instanceof IMultiblockRecipe multiblock) {
            List<?> outputs = multiblock.getFluidOutputs();
            if (!outputs.isEmpty()) {
                net.minecraftforge.fluids.FluidStack fluid = (net.minecraftforge.fluids.FluidStack) outputs.get(0);
                return fluid.getFluid().getFluidType().getDescription().getString();
            }
        }
        return "";
    }
}
