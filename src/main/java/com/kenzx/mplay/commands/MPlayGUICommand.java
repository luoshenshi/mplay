package com.kenzx.mplay.commands;

import com.kenzx.mplay.gui.MPlayGUI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;

public class MPlayGUICommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandBuildContext commandBuildContext) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommands.literal("music-gui").executes(MPlayGUICommand::execute));
    }

    private static int execute(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) {
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> client.setScreen(new MPlayGUI()));

        return 0;
    }
}