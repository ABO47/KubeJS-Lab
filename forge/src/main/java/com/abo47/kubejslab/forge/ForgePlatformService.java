package com.abo47.kubejslab.forge;

import java.util.List;
import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.block.C2SBlockEditPacket;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.network.loot.C2SLootEditPacket;
import com.abo47.kubejslab.network.loot.S2CLootStatePacket;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.platform.PlatformService;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;


public final class ForgePlatformService implements PlatformService {
    private static final boolean IE_LOADED = isClassPresent(
            "blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe");
    private static final boolean CREATE_LOADED = isClassPresent(
            "com.simibubi.create.content.processing.recipe.ProcessingRecipe");

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name, false, ForgePlatformService.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

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
    public void sendItemEdit(C2SItemEditPacket packet) {
        ForgeNetwork.sendToServer(packet);
    }

    @Override
    public void sendItemState(ServerPlayer player, S2CItemStatePacket packet) {
        ForgeNetwork.sendToClient(packet, player);
    }

    @Override
    public void sendBlockEdit(C2SBlockEditPacket packet) {
        ForgeNetwork.sendToServer(packet);
    }

    @Override
    public void sendBlockState(ServerPlayer player, S2CBlockStatePacket packet) {
        ForgeNetwork.sendToClient(packet, player);
    }

    @Override
    public void sendLootEdit(C2SLootEditPacket packet) {
        ForgeNetwork.sendToServer(packet);
    }

    @Override
    public void sendLootState(ServerPlayer player, S2CLootStatePacket packet) {
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
        if (IE_LOADED && recipe instanceof IMultiblockRecipe multiblock) {
            List<?> outputs = multiblock.getFluidOutputs();
            if (outputs != null && !outputs.isEmpty()) {
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
    public FluidStack fluidOutputStack(Recipe<?> recipe) {
        if (IE_LOADED && recipe instanceof IMultiblockRecipe multiblock) {
            List<?> outputs = multiblock.getFluidOutputs();
            if (outputs != null && !outputs.isEmpty()) {
                net.minecraftforge.fluids.FluidStack fluid = (net.minecraftforge.fluids.FluidStack) outputs.get(0);
                return fluid.getTag() == null
                        ? FluidStack.create(fluid.getFluid(), fluid.getAmount())
                        : FluidStack.create(fluid.getFluid(), fluid.getAmount(), fluid.getTag());
            }
        }
        if (CREATE_LOADED && dev.architectury.platform.Platform.isModLoaded("create")
                && recipe instanceof com.simibubi.create.content.processing.recipe.ProcessingRecipe<?> processing) {
            List<net.minecraftforge.fluids.FluidStack> fluidResults = processing.getFluidResults();
            if (!fluidResults.isEmpty()) {
                net.minecraftforge.fluids.FluidStack fluid = fluidResults.get(0);
                return fluid.getTag() == null
                        ? FluidStack.create(fluid.getFluid(), fluid.getAmount())
                        : FluidStack.create(fluid.getFluid(), fluid.getAmount(), fluid.getTag());
            }
        }
        return FluidStack.empty();
    }

    @Override
    public String fluidOutputDisplayName(Recipe<?> recipe) {
        if (IE_LOADED && recipe instanceof IMultiblockRecipe multiblock) {
            List<?> outputs = multiblock.getFluidOutputs();
            if (outputs != null && !outputs.isEmpty()) {
                net.minecraftforge.fluids.FluidStack fluid = (net.minecraftforge.fluids.FluidStack) outputs.get(0);
                return fluid.getFluid().getFluidType().getDescription().getString();
            }
        }
        return "";
    }
}
