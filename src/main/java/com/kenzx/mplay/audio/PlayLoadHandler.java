package com.kenzx.mplay.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import net.minecraft.commands.CommandSourceStack;

public class PlayLoadHandler extends SearchLoadHandler{


    public PlayLoadHandler(CommandSourceStack source, GroupManager group) {
        super(source, group);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        super.trackLoaded(playlist.getTracks().get(0));
    }
}
