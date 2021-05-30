package indi.goldenwater.joinmessage;

import indi.goldenwater.joinmessage.listeners.OnPlayerJoinEvent;
import indi.goldenwater.joinmessage.listeners.OnPlayerQuitEvent;
import indi.goldenwater.joinmessage.utils.ConfigWatchService;
import org.bukkit.plugin.java.JavaPlugin;

public final class JoinMessage extends JavaPlugin {
    private static JoinMessage instance;
    private ConfigWatchService watchService;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        if (getConfig().getBoolean("fileWatchService")) {
            watchService = new ConfigWatchService(this);
            watchService.register("fileWatchService",
                    name -> name.endsWith(".yml"),
                    new ConfigWatchService.DoSomeThing() {
                        @Override
                        public void reload() {
                            reloadConfig();
                        }

                        @Override
                        public void release() {
                            saveDefaultConfig();
                        }
                    });
        }

        getServer().getPluginManager().registerEvents(new OnPlayerJoinEvent(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerQuitEvent(), this);

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (watchService != null) {
            watchService.unregister();
        }

        getLogger().info("Disabled.");
    }

    public static JoinMessage getInstance() {
        return instance;
    }
}
