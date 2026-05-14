package com.kenzx.mplay.commands;

import com.kenzx.mplay.MPlayClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.kenzx.mplay.audio.GroupManager;
import com.kenzx.mplay.audio.MusicManager;
import com.kenzx.mplay.util.ModUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.concurrent.BlockingQueue;

import static com.kenzx.mplay.util.ModUtils.checkPlayerGroup;

public class QueueCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music").then(Commands.literal("queue").executes(QueueCommand::execute)));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        MPlayClient.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());

            MutableComponent text = Component.empty();
            AudioTrack currentTrack = gm.getPlayer().getPlayingTrack();
            BlockingQueue<AudioTrack> tracks = gm.getQueue();

            if (currentTrack != null) {
                text.append(Component.literal("Current: ").append(ModUtils.trackInfo(currentTrack.getInfo())).append("\n"));
            }

            AudioTrack[] tracksArr = tracks.toArray(AudioTrack[]::new);
            for (int i = 0; i < tracksArr.length; i++) {
                AudioTrack track = tracksArr[i];
                text.append(Component.literal(i + ". ").append(ModUtils.trackInfo(track.getInfo())).append(Component.literal("\n")));
            }

            if (text.getString().isBlank()) {
                text.append(Component.literal("No songs in the queue."));
            }

            result.source().sendSystemMessage(text);
        });

        return 0;
    }

}
