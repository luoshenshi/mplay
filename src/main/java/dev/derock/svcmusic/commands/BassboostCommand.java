package dev.derock.svcmusic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
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

public class BassboostCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("music").then(Commands.literal("bassboost").then(Commands.argument("bass_percent", FloatArgumentType.floatArg(0, 200)).executes(BassboostCommand::execute))));
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        float bass = FloatArgumentType.getFloat(context, "bass_percent");
        ModUtils.CheckPlayerGroup result = checkPlayerGroup(context);
        if (result == null) return 1;

        SimpleVoiceChatMusic.SCHEDULED_EXECUTOR.execute(() -> {
            GroupManager gm = MusicManager.getInstance().getGroup(result.group(), result.player().level().getServer());
            gm.broadcast(Component.literal("Bassboost set to " + bass + "% by " + result.source().getTextName()));
            gm.setBassBoost(bass);
        });

        return 0;
    }

}
