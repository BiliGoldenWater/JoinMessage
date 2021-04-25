package indi.goldenwater.joinmessage;

import indi.goldenwater.joinmessage.listeners.OnPlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.*;

public final class JoinMessage extends JavaPlugin {
    private static JoinMessage instance;
    private WatchService watchService;
    private boolean watchServerRunning = false;
    private boolean watchServiceRegistered = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        if (getConfig().getBoolean("fileWatchService")) {
            registerWatchService();
            watchServiceRegistered = true;
        }

        getServer().getPluginManager().registerEvents(new OnPlayerJoinEvent(), this);

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        closeWatchService();

        getLogger().info("Disabled.");
    }

    private void registerWatchService() {
        try {
            Path dataFolder = getDataFolder().toPath();
            watchService = FileSystems.getDefault().newWatchService();
            dataFolder.register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            BukkitRunnable watchServiceRunnable = new BukkitRunnable() {

                @Override
                public void run() {
                    try {
                        WatchKey key;
                        while (watchServerRunning && (key = watchService.take()) != null) {
                            for (WatchEvent<?> event : key.pollEvents()) {
                                if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                                    if (event.context().toString().equals("config.yml")) {
                                        saveDefaultConfig();
                                    }
                                } else if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                    if (event.context().toString().equals("config.yml")) {
                                        reloadConfig();
                                        if (!getConfig().getBoolean("fileWatchService")) {
                                            closeWatchService();
                                        }
                                    }
                                }
                            }
                            key.reset();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            watchServerRunning = true;
            watchServiceRunnable.runTaskAsynchronously(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWatchService() {
        if (watchServiceRegistered) {
            try {
                watchServerRunning = false;
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        watchServiceRegistered = false;
    }

    public static JoinMessage getInstance() {
        return instance;
    }
}
