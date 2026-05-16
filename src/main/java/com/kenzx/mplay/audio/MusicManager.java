package com.kenzx.mplay.audio;

import com.kenzx.mplay.MPlayClient;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import de.maxhenkel.voicechat.api.Group;

import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.UUID;

public class MusicManager {
    private static final MusicManager instance = new MusicManager();
    private final HashMap<UUID, GroupManager> groups = new HashMap<>();
    public AudioPlayerManager playerManager;

    public MusicManager() {
        MPlayClient.LOGGER.info("Loading sources...");
        this.playerManager = new DefaultAudioPlayerManager();
        this.playerManager.getConfiguration().setFilterHotSwapEnabled(true);

        // 1. Modern v2 YouTube manager
        dev.lavalink.youtube.YoutubeAudioSourceManager ytSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager();
        this.playerManager.registerSourceManager(ytSourceManager);

        // 2. Legacy YouTube Manager
        AudioSourceManagers.registerRemoteSources(
                this.playerManager,
                com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class
        );

        MPlayClient.LOGGER.info("Loaded all sources!");
    }

    public static MusicManager getInstance() {
        return instance;
    }

    public GroupManager getGroup(Group group, MinecraftServer server) {
        if (groups.containsKey(group.getId())) {
            return groups.get(group.getId());
        } else {
            GroupManager gm = new GroupManager(group, playerManager.createPlayer(), server);
            groups.put(group.getId(), gm);
            return gm;
        }
    }

    public GroupManager deleteGroup(Group group) {
        return groups.remove(group.getId());
    }

    /**
     * Destroys all groups
     */
    public void cleanup() {
        for (GroupManager gm : groups.values()) {
            gm.cleanup();
        }

        groups.clear();
    }
}
