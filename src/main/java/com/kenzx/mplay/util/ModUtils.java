package com.kenzx.mplay.util;

import com.kenzx.mplay.SVC;
import com.mojang.brigadier.context.CommandContext;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ModUtils {

    public static MutableComponent hyperlink(String string, String url) {
        return Component.literal(string)
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(url))));
    }

    public static MutableComponent trackInfo(AudioTrackInfo track) {
        return trackInfo(track, false);
    }

    public static MutableComponent trackInfo(AudioTrackInfo track, boolean longFormat) {
        MutableComponent text = Component.literal(track.title)
                .setStyle(
                        Style.EMPTY.withColor(ChatFormatting.AQUA)
                                .withClickEvent(new ClickEvent.OpenUrl(URI.create(track.uri)))
                )
                .append(Component.literal(" by ").setStyle(Style.EMPTY))
                .append(Component.literal(track.author).setStyle(
                        Style.EMPTY.withColor(ChatFormatting.AQUA))
                );

        // if long format, add more track data
        if (longFormat) {
            text.append(Component.literal(" [" + formatMMSS(track.length) + "]").setStyle(Style.EMPTY));
        }

        return text;
    }

    public static String formatMMSS(long millis) {
        String seconds = Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))).toString();

        if (seconds.length() == 1) seconds = "0" + seconds;

        return String.format("%d:%s",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                seconds
        );
    }

    public static String parseTrackId(String userInput) {
        if (userInput.startsWith("ytsearch:") || userInput.startsWith("ytmsearch:") || userInput.startsWith("scsearch:") || userInput.startsWith("spotify:")) {
            return userInput;
        }

        // if starts with id:, parse ourselves
        if (userInput.startsWith("id:")) {
            return userInput.substring(3);
        }

        // try and parse as URL
        try {
            new URL(userInput);
        } catch (MalformedURLException e) {
            return "ytmsearch:" + userInput;
        }

        return userInput;
    }

    public static @Nullable CheckPlayerGroup checkPlayerGroup(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (SVC.voicechatServerApi == null) {
            source.sendSystemMessage(
                    Component.literal("VoiceChat API connection has not been established yet! Please try again later.")
            );
            return null;
        }

        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendSystemMessage(
                    Component.literal("This command is player only!")
            );
            return null;
        }

        VoicechatConnection connection = SVC.voicechatServerApi.getConnectionOf(player.getUUID());

        if (connection == null) {
            source.sendSystemMessage(
                    Component.literal("You are not connected to voice chat!")
            );
            return null;
        }

        Group group = connection.getGroup();

        if (group == null) {
            source.sendSystemMessage(
                    Component.literal("You're not in a group!")
            );
            return null;
        }
        return new CheckPlayerGroup(source, player, group);
    }

    public record CheckPlayerGroup(CommandSourceStack source, ServerPlayer player, Group group) {
    }
}
