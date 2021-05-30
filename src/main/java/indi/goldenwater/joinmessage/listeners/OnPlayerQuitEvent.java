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
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuitEvent implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        final JoinMessage plugin = JoinMessage.getInstance();
        final Configuration config = plugin.getConfig();
        final Server server = plugin.getServer();

        final Player player = event.getPlayer();
        final boolean useJsonVersion = config.getBoolean("useJsonVersion");

        BaseComponent[] message;
        String originMessage = config.getString("quitMessage" + (useJsonVersion ? "Json" : ""))
                .replace("'", "\"");

        if (originMessage.equals("")) {
            event.setQuitMessage("");
            return;
        }

        final boolean papiEnabled = server.getPluginManager().getPlugin("PlaceholderAPI") != null &&
                server.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiEnabled) {
            originMessage = PlaceholderAPI.setPlaceholders(player, originMessage);
        }

        originMessage = originMessage.replace("{{player}}", player.getDisplayName());

        message = useJsonVersion ?
                ComponentSerializer.parse(originMessage) :
                TextComponent.fromLegacyText(originMessage);

        event.setQuitMessage("");
        server.getLogger().info(message[0].toPlainText());
        for (Player onlinePlayer : server.getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(message);
        }
    }
}
