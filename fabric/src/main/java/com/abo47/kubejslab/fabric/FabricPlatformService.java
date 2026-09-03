package com.abo47.kubejslab.fabric;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.abo47.kubejslab.network.block.C2SBlockEditPacket;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.network.item.C2SItemEditPacket;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;
import com.abo47.kubejslab.network.loot.C2SLootEditPacket;
import com.abo47.kubejslab.network.loot.C2SLootPrefillPacket;
import com.abo47.kubejslab.network.loot.S2CLootPrefillPacket;
import com.abo47.kubejslab.network.loot.S2CLootStatePacket;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.network.recipe.S2CRecipeStatePacket;
import com.abo47.kubejslab.platform.PlatformService;

import io.netty.buffer.Unpooled;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;


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
    public void sendItemEdit(C2SItemEditPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ClientPlayNetworking.send(FabricNetwork.ITEM_EDIT, buf);
    }

    @Override
    public void sendItemState(ServerPlayer player, S2CItemStatePacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, FabricNetwork.ITEM_STATE_SYNC, buf);
    }

    @Override
    public void sendBlockEdit(C2SBlockEditPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ClientPlayNetworking.send(FabricNetwork.BLOCK_EDIT, buf);
    }

    @Override
    public void sendBlockState(ServerPlayer player, S2CBlockStatePacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, FabricNetwork.BLOCK_STATE_SYNC, buf);
    }

    @Override
    public void sendLootEdit(C2SLootEditPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ClientPlayNetworking.send(FabricNetwork.LOOT_EDIT, buf);
    }

    @Override
    public void sendLootState(ServerPlayer player, S2CLootStatePacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, FabricNetwork.LOOT_STATE_SYNC, buf);
    }

    @Override
    public void sendLootPrefill(C2SLootPrefillPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ClientPlayNetworking.send(FabricNetwork.LOOT_PREFILL, buf);
    }

    @Override
    public void sendLootPrefill(ServerPlayer player, S2CLootPrefillPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, FabricNetwork.LOOT_PREFILL_SYNC, buf);
    }

    @Override
    public Optional<FluidStack> readFluidIngredient(IRecipeSlotView view) {
        return view.getIngredients(FabricTypes.FLUID_STACK)
                .findFirst()
                .map(ingredient -> FluidStack.create(ingredient.getFluid(), ingredient.getAmount(),
                        ingredient.getTag().orElse(null)));
    }
}
