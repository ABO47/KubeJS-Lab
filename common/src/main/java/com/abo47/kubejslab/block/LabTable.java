package com.abo47.kubejslab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.abo47.kubejslab.network.ModNetwork;

public final class LabTable extends Block {
    public LabTable(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ModNetwork.sendOpenScreen((net.minecraft.server.level.ServerPlayer) player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
