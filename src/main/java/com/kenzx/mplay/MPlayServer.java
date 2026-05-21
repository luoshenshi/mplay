package com.kenzx.mplay;

import com.kenzx.mplay.audio.MusicManager;
import com.kenzx.mplay.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MPlayServer implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("mplay");

    public static ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "MPlayExecutor");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(
                (t, e) -> MPlayServer.LOGGER.error("Uncaught exception in thread {}", t.getName(), e)
        );

        return thread;
    });

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(SearchCommand::register);
        CommandRegistrationCallback.EVENT.register(NowPlayingCommand::register);
        CommandRegistrationCallback.EVENT.register(SkipCommand::register);
        CommandRegistrationCallback.EVENT.register(PlayCommand::register);
        CommandRegistrationCallback.EVENT.register(QueueCommand::register);
        CommandRegistrationCallback.EVENT.register(PauseCommand::register);
        CommandRegistrationCallback.EVENT.register(ResumeCommand::register);
        CommandRegistrationCallback.EVENT.register(StopCommand::register);
        CommandRegistrationCallback.EVENT.register(KillCommand::register);
        CommandRegistrationCallback.EVENT.register(VolumeCommand::register);
        CommandRegistrationCallback.EVENT.register(BassBoostCommand::register);

        // Staring it when server starts
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SCHEDULED_EXECUTOR.execute(() -> {
                LOGGER.info("Pre-initializing MusicManager...");
                MusicManager.getInstance();
                LOGGER.info("MusicManager ready.");
            });
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((MinecraftServer _) -> {
            LOGGER.info("Cleaning up due to shutdown.");
            MusicManager.getInstance().cleanup();
        });

        LOGGER.info("Loaded MPlayServer");
    }
}
