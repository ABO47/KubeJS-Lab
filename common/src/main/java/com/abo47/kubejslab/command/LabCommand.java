package com.abo47.kubejslab.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.client.ui.LabUIFactory;
import com.abo47.kubejslab.KubeJSLab;


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
                    LabUIFactory.open(player.blockPosition(), player);
                    return 1;
                });
    }
}
