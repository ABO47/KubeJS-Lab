package com.abo47.kubejslab.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.abo47.kubejslab.client.ui.LabUIFactory;


public final class LabTabletItem extends Item {
    public LabTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            LabUIFactory.open(player.blockPosition(), (net.minecraft.server.level.ServerPlayer) player);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide) {
            LabUIFactory.open(player.blockPosition(), (net.minecraft.server.level.ServerPlayer) player);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
