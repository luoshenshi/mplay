package dev.derock.svcmusic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.audio.GroupManager;
import dev.derock.svcmusic.audio.MusicManager;
import dev.derock.svcmusic.util.ModUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;


import static dev.derock.svcmusic.util.ModUtils.checkPlayerGroup;

public class ResumeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music")
            .then(Commands.literal("resume")
                .executes(ResumeCommand::execute)));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());
            gm.broadcast(Component.literal("Playback resumed by " + result.source().getTextName()));
            gm.getPlayer().setPaused(false);
        });

        return 0;
    }

}
