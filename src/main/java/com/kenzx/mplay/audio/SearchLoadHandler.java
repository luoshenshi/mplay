package com.kenzx.mplay.audio;

import com.kenzx.mplay.MPlayClient;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.kenzx.mplay.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent;

import java.util.List;
import java.util.Objects;

public class SearchLoadHandler implements AudioLoadResultHandler {

    protected final CommandSourceStack source;
    protected final GroupManager group;

    public SearchLoadHandler(CommandSourceStack source, GroupManager group) {
        this.source = source;
        this.group = group;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        group.enqueueSong(track);

        if (source != null) {
            this.group.broadcast(
                Component.literal("Enqueued ")
                    .append(ModUtils.trackInfo(track.getInfo(), true))
                    .append(" - ").append(Objects.requireNonNull(source.getPlayer()).getName())
            );
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        // if over 10, trim
        List<AudioTrack> loaded = playlist.getTracks().subList(0, 5);

        if (source != null) {
            // get all titles and create one large string
            MutableComponent text = Component.literal("Found " + loaded.size() + " results: \n");

            for (AudioTrack track : loaded) {
                text.append(Component.literal("  - "))
                    .append(ModUtils.trackInfo(track.getInfo(), true))
                    .append(Component.literal("\n"))
                    .append(Component.literal("    "))
                        .append(
                                Component.literal("[Click to add to queue]")
                                        .setStyle(
                                                Style.EMPTY.withClickEvent(
                                                        new ClickEvent.RunCommand(
                                                                "/music play \"" + track.getIdentifier() + "\""
                                                        )
                                                )
                                        )
                        )
                    .append(Component.literal("\n\n"));
            }

            source.sendSystemMessage(text);
        }
    }

    @Override
    public void noMatches() {
        if (source != null) {
            source.sendSystemMessage(Component.literal("No matches found!"));
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (!exception.severity.equals(FriendlyException.Severity.COMMON)) {
            MPlayClient.LOGGER.warn("Failed to load track from query", exception);
        }

        if (source != null) {
            source.sendSystemMessage(Component.literal(exception.severity == FriendlyException.Severity.COMMON ? "Failed to load track: " + exception.getMessage() : "Track failed to load! Check server logs for more information"));
        }
    }
}
