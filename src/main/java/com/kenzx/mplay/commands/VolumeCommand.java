package com.kenzx.mplay.commands;

import com.kenzx.mplay.MPlayClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

public class VolumeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music")
            .then(Commands.literal("volume")
                .then(Commands.argument("volume_percent", IntegerArgumentType.integer(0, 100))
                    .executes(VolumeCommand::execute))));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int volume = IntegerArgumentType.getInteger(context, "volume_percent");
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        MPlayClient.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());
            gm.broadcast(Component.literal("Volume set to " + volume + "% by " + result.source().getTextName()));
            gm.setVolume(volume);
        });

        return 0;
    }

}
