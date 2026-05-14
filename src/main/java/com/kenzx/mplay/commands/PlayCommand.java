package com.kenzx.mplay.commands;

import com.kenzx.mplay.MPlayClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.kenzx.mplay.audio.GroupManager;
import com.kenzx.mplay.audio.MusicManager;
import com.kenzx.mplay.audio.PlayLoadHandler;
import com.kenzx.mplay.util.ModUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.kenzx.mplay.util.ModUtils.checkPlayerGroup;


public class PlayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music")
            .then(Commands.literal("play")
                .then(Commands.argument("query", StringArgumentType.string())
                    .executes(PlayCommand::execute))));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final String query = ModUtils.parseTrackId(StringArgumentType.getString(context, "query"));
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        MPlayClient.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());

            result.source().sendSystemMessage(Component.literal("Loading songs..."));
            MusicManager.getInstance().playerManager.loadItemOrdered(
                gm.getPlayer(),
                query,
                new PlayLoadHandler(result.source(), gm)
            );
        });

        return 0;
    }

}
