package indi.goldenwater.joinmessage.listeners;

import indi.goldenwater.joinmessage.JoinMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoinEvent implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        final JoinMessage plugin = JoinMessage.getInstance();
        final Configuration config = plugin.getConfig();
        final Server server = plugin.getServer();

        final Player player = event.getPlayer();
        final boolean useJsonVersion = config.getBoolean("useJsonVersion");
        final boolean papiEnabled = server.getPluginManager().getPlugin("PlaceholderAPI") != null &&
                server.getPluginManager().isPluginEnabled("PlaceholderAPI");

        BaseComponent[] message;
        String originMessage = config.getString("joinMessage" + (useJsonVersion ? "Json" : ""))
                .replace("'", "\"");

        if (papiEnabled) {
            originMessage = PlaceholderAPI.setPlaceholders(player, originMessage);
        } else {
            originMessage = originMessage.replace("{{player}}", player.getDisplayName());
        }

        message = useJsonVersion ?
                ComponentSerializer.parse(originMessage) :
                TextComponent.fromLegacyText(originMessage);


        event.setJoinMessage("");
        for (Player onlinePlayer : server.getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(message);
        }
    }
}
