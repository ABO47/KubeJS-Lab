package com.abo47.kubejslab.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.network.ModNetwork;

public final class LabCommand {
    private LabCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(root());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal(KubeJSLab.MOD_ID)
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ModNetwork.sendOpenScreen(player);
                    return 1;
                });
    }
}
