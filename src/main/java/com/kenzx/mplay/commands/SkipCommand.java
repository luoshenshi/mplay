package com.kenzx.mplay.commands;

import com.kenzx.mplay.MPlayClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.kenzx.mplay.audio.GroupManager;
import com.kenzx.mplay.audio.MusicManager;
import com.kenzx.mplay.util.ModUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.kenzx.mplay.util.ModUtils.checkPlayerGroup;

public class SkipCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music")
                .then(Commands.literal("skip")
                        .executes(SkipCommand::execute)));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        MPlayClient.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());
            gm.broadcast(Component.literal("Song skipped by " + result.source().getTextName()));
            gm.nextTrack();
        });

        return 0;
    }

}
