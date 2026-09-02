package com.abo47.kubejslab.platform;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
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

import mezz.jei.api.gui.ingredient.IRecipeSlotView;


public interface PlatformService {
    void registerNetwork();

    void sendOpenScreen(ServerPlayer player, FriendlyByteBuf serializedHolder, int windowId);

    void sendOpenRequest();

    void sendRecipeEdit(C2SRecipeEditPacket packet);

    void sendRecipeState(ServerPlayer player, S2CRecipeStatePacket packet);

    void sendItemEdit(C2SItemEditPacket packet);

    void sendItemState(ServerPlayer player, S2CItemStatePacket packet);

    void sendBlockEdit(C2SBlockEditPacket packet);

    void sendBlockState(ServerPlayer player, S2CBlockStatePacket packet);

    void sendLootEdit(C2SLootEditPacket packet);

    void sendLootState(ServerPlayer player, S2CLootStatePacket packet);

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
