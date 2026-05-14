package com.kenzx.mplay.commands;

import com.kenzx.mplay.MPlayClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.kenzx.mplay.audio.GroupManager;
import com.kenzx.mplay.audio.GroupSettingsManager;
import com.kenzx.mplay.audio.MusicManager;
import com.kenzx.mplay.util.ModUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.kenzx.mplay.util.ModUtils.checkPlayerGroup;

public class NowPlayingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music")
            .then(Commands.literal("now-playing")
                .executes(NowPlayingCommand::execute)));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        MPlayClient.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());
            AudioTrack track = gm.getPlayer().getPlayingTrack();
            GroupSettingsManager settings = gm.getSettingsStore();

            if (track == null) {
                result.source().sendSystemMessage(Component.literal("Nothing is playing."));
                return;
            }

            result.source().sendSystemMessage(
                Component.literal("Currently Playing ")
                    .append(ModUtils.trackInfo(track.getInfo()))
                    .append(Component.literal("\n" + ModUtils.formatMMSS(track.getPosition()) + "/" + ModUtils.formatMMSS(track.getDuration())) )
                    .append(Component.literal(" • " + settings.volume + "% volume"))
                    .append(Component.literal(" • " + settings.bassboost + "% bassboost"))
            );
        });

        return 0;
    }

}
