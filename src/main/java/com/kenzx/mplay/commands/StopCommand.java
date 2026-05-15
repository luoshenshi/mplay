package com.kenzx.mplay.commands;

import com.kenzx.mplay.MPlayClient;
import com.kenzx.mplay.audio.GroupManager;
import com.kenzx.mplay.audio.MusicManager;
import com.kenzx.mplay.util.ModUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.kenzx.mplay.util.ModUtils.checkPlayerGroup;

public class StopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music")
                .then(Commands.literal("stop")
                        .executes(StopCommand::execute)));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        MPlayClient.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());
            gm.broadcast(Component.literal("Playback stopped by " + result.source().getTextName()));
            gm.getPlayer().stopTrack();
        });

        return 0;
    }

}
