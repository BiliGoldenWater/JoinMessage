package indi.goldenwater.joinmessage;

import indi.goldenwater.joinmessage.listeners.OnPlayerJoinEvent;
import indi.goldenwater.joinmessage.listeners.OnPlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.*;

public final class JoinMessage extends JavaPlugin {
    private static JoinMessage instance;
    private WatchService watchService;
    private BukkitRunnable watchServiceRunnable;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        if (getConfig().getBoolean("fileWatchService")) {
            registerWatchService();
        }

        getServer().getPluginManager().registerEvents(new OnPlayerJoinEvent(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerQuitEvent(), this);

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (watchServiceRunnable.isCancelled()) {
            watchServiceRunnable.cancel();
        }

        getLogger().info("Disabled.");
    }

    private void registerWatchService() {
        try {
            Path dataFolder = getDataFolder().toPath();
            watchService = FileSystems.getDefault().newWatchService();
            dataFolder.register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            watchServiceRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        WatchKey key;
                        while ((key = watchService.take()) != null) {
                            for (WatchEvent<?> event : key.pollEvents()) {
                                if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                                    if (event.context().toString().equals("config.yml")) {
                                        saveDefaultConfig();
                                    }
                                } else if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                    if (event.context().toString().equals("config.yml")) {
                                        reloadConfig();
                                        if (!getConfig().getBoolean("fileWatchService")) {
                                            this.cancel();
                                        }
                                    }
                                }
                            }
                            key.reset();
//                            if (!watchServerRunning) break;
                        }
                        try {
                            watchService.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            watchServiceRunnable.runTaskAsynchronously(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JoinMessage getInstance() {
        return instance;
    }
}
