package net.omni.crateyBackpack.listener;

import net.omni.crateyBackpack.CrateyBackpack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final CrateyBackpack plugin;

    public PlayerListener(CrateyBackpack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getBackpackManager().getOrLoad(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getBackpackManager().unloadPlayer(event.getPlayer());
    }
}
