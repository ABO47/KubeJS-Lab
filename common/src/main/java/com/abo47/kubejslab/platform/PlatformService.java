package com.abo47.kubejslab.platform;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;


public interface PlatformService {
    void registerNetwork();

    void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId);

    void sendOpenRequest();

    void sendRecipeEdit(C2SRecipeEditPacket packet);

    void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet);

    Optional<FluidStack> readFluidIngredient(IRecipeSlotView view);

    default ItemStack fluidOutputDisplay(Recipe<?> recipe) {
        return ItemStack.EMPTY;
    }

    default FluidStack fluidOutputStack(Recipe<?> recipe) {
        return FluidStack.empty();
    }

    default String fluidOutputDisplayName(Recipe<?> recipe) {
        return "";
    }
}
