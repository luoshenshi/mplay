package com.kenzx.mplay.audio;

import com.kenzx.mplay.MPlayClient;
import com.kenzx.mplay.MPlayServer;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.*;

import static com.kenzx.mplay.util.Constants.BASS_BOOST;

public class GroupManager {
    private final Group group;
    private final AudioPlayer lavaplayer;
    private final MinecraftServer server;
    private final BlockingQueue<AudioTrack> queue;
    private final GroupSettingsManager settingsStore;

    private final ConcurrentHashMap<UUID, StaticAudioChannel> connections = new ConcurrentHashMap<>();
    private final MutableAudioFrame currentFrame;
    private final EqualizerFactory equalizer = new EqualizerFactory();

    private @Nullable ScheduledFuture<?> audioFrameSendingTask = null;
    private @Nullable ScheduledFuture<?> playerTrackingTask = null;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "SVCGroupMusicExecutor");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(
            (t, e) -> MPlayClient.LOGGER.error("Uncaught exception in thread {}", t.getName(), e)
        );

        return thread;
    });

    public GroupManager(Group group, AudioPlayer player, MinecraftServer server) {
        this.group = group;
        this.server = server;
        this.lavaplayer = player;
        this.currentFrame = new MutableAudioFrame();
        this.settingsStore = GroupSettingsManager.getGroup(group);

        // apply EQ
        this.lavaplayer.setFilterFactory(this.equalizer);
        this.lavaplayer.setFrameBufferDuration(500);

        // buffer for storing current opus frame
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        currentFrame.setBuffer(buffer);

        // todo: max queue size
        this.queue = new LinkedBlockingQueue<>();

        // register events
        player.addListener(new TrackScheduler(this));

        // schedule task
        startGroupTracking();
        startAudioFrameSending();

        // restore settings
        this.setVolume(this.settingsStore.volume);
        this.setBassBoost(this.settingsStore.bassboost);
    }

    private void startAudioFrameSending() {
        if (this.audioFrameSendingTask != null && !this.audioFrameSendingTask.isDone()) {
            // already started, so leave it.
            MPlayClient.LOGGER.info("Not starting new audio frame sending task.");
            return;
        }

        if (this.audioFrameSendingTask != null && this.audioFrameSendingTask.isDone()) {
            // stop and restart
            MPlayClient.LOGGER.info("Frame task in stuck state, attempting to revive");
            this.audioFrameSendingTask.cancel(true);
        }

        MPlayClient.LOGGER.info("Starting new audio frame sending task.");
        this.audioFrameSendingTask = this.executorService.scheduleAtFixedRate(() -> {
            if (MPlayServer.voicechatServerApi == null) {
                return;
            }

            // check if playback is paused
            if (this.lavaplayer == null || this.lavaplayer.isPaused() || this.lavaplayer.getPlayingTrack() == null) {
                return;
            }

            if (lavaplayer.provide(this.currentFrame)) {
                for (StaticAudioChannel channel : connections.values()) {
                    channel.send(this.currentFrame.getData());
                }
            }
        }, 1000L, 20L, TimeUnit.MILLISECONDS);
    }

    private void startGroupTracking() {
        this.playerTrackingTask = executorService.scheduleAtFixedRate(() -> {
            if (MPlayServer.voicechatServerApi == null) return;

            HashSet<UUID> uuids = new HashSet<>();

            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                VoicechatConnection playerConnection = MPlayServer.voicechatServerApi.getConnectionOf(serverPlayer.getUUID());

                if (playerConnection == null || !playerConnection.isConnected()) continue;
                Group playerGroup = playerConnection.getGroup();
                if (playerGroup == null || playerGroup.getId() != this.group.getId()) continue;

                uuids.add(serverPlayer.getUUID());

                connections.computeIfAbsent(
                    serverPlayer.getUUID(),
                    (uuid) -> {
                        StaticAudioChannel channel = MPlayServer.voicechatServerApi.createStaticAudioChannel(
                            UUID.randomUUID(),
                            MPlayServer.voicechatServerApi.fromServerLevel(serverPlayer.level()),
                            playerConnection
                        );

                        if (channel == null) return null;
                        channel.setCategory(MPlayServer.MUSIC_CATEGORY);

                        return channel;
                    }
                );
            }

            // now remove all that aren't here anymore
            for (UUID uuid : connections.keySet()) {
                if (uuids.contains(uuid)) continue;
                connections.remove(uuid);
            }

            // clean up if no players
            if (this.connections.isEmpty()) {
                MPlayClient.LOGGER.info("Group {} is now empty. Cleaning up...", this.group.getName());
                this.cleanup();
            }

            // stop if no songs queued
            // if (this.lavaplayer.getPlayingTrack() == null && this.queue.isEmpty() && this.audioFrameSendingTask != null) {
            //     MPlayClient.LOGGER.info("Pausing playback in {} due to empty queue", this.group.getName());
            //     this.audioFrameSendingTask.cancel(false);
            //     this.audioFrameSendingTask = null;
            // }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    public boolean enqueueSong(AudioTrack track) {
        // noInterrupt true => false return if smth already playing
        //                     true return if nothing playing
        if (!lavaplayer.startTrack(track, true)) {
            return this.queue.offer(track);
        }

        return true;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void nextTrack() {
        // ensure this happens in the correct thread
        this.executorService.execute(() -> {
            // poll returns track or null
            // if null, lavaplayer stops
            AudioTrack track = queue.poll();
            lavaplayer.startTrack(track, false);

            // revive task if needed
            if (track != null) {
                this.startAudioFrameSending();
            } else {
                // no more songs to play, so quit
                this.cleanup();
            }
        });
    }

    public AudioPlayer getPlayer() {
        return this.lavaplayer;
    }

    public void broadcast(MutableComponent text) {
        // execute on main thread
        server.execute(() -> {
            ServerPlayer[] players = server.getPlayerList().getPlayers().stream().filter(
                (player) -> this.connections.containsKey(player.getUUID())
            ).toArray(ServerPlayer[]::new);

            for (ServerPlayer player : players) {
                player.sendSystemMessage(text);
            }
        });
    }

    public void cleanup() {
        this.broadcast(Component.literal("No more songs to play."));
        if (this.audioFrameSendingTask != null) this.audioFrameSendingTask.cancel(true);
        this.lavaplayer.destroy();
        MusicManager.getInstance().deleteGroup(this.group);
        if (this.playerTrackingTask != null) this.playerTrackingTask.cancel(false);
        this.executorService.shutdown();
    }

    public void setBassBoost(float percentage) {
        this.settingsStore.bassboost = percentage;
        final float multiplier = percentage / 100.00f;

        for (int i = 0; i < BASS_BOOST.length; i++) {
            this.equalizer.setGain(i, BASS_BOOST[i] * multiplier);
        }
    }

    public void setVolume(int volume) {
        this.settingsStore.volume = volume;
        this.getPlayer().setVolume(volume);
    }

    public final GroupSettingsManager getSettingsStore() {
        return this.settingsStore;
    }
}
