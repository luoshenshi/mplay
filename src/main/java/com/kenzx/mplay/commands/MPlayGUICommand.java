package com.kenzx.mplay.commands;

import com.kenzx.mplay.gui.MPlayGUI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MPlayGUICommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music").then(Commands.literal("gui").executes(MPlayGUICommand::execute)));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        Minecraft client = Minecraft.getInstance();

        client.execute(() -> {
            client.setScreen(new MPlayGUI());
        });

        return 0;
    }
}