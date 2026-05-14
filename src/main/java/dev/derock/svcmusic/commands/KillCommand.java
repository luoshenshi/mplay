package dev.derock.svcmusic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.derock.svcmusic.SimpleVoiceChatMusic;
import dev.derock.svcmusic.audio.GroupManager;
import dev.derock.svcmusic.audio.MusicManager;
import dev.derock.svcmusic.util.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.HashSet;
import java.util.UUID;

import static dev.derock.svcmusic.util.ModUtils.checkPlayerGroup;

public class KillCommand {
    private static HashSet<UUID> warned = new HashSet<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music")
                .then(Commands.literal("kill")
                        .executes(KillCommand::execute)));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        if (warned.add(result.player().getUUID())) {
            result.source().sendSystemMessage(
                    Component.literal("Are you sure you want to do this? This command should be used when everything is broken and you need to alt-f4 the plugin. Group members may hear a bit of earrape as the opus packets abruptly end.")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)).append(Component.literal("\n\nIf you understand this, run the command again.")));
            return 0;
        }

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());
            gm.broadcast(Component.literal("Playback forcibly killed by " + result.source().getTextName() + "."));
            gm.cleanup();
        });

        return 0;
    }

}
