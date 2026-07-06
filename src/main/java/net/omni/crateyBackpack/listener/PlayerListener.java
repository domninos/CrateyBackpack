package net.omni.crateyBackpack.listener;

import me.colingrimes.cratey.config.implementation.Crates;
import me.colingrimes.cratey.crate.events.CrateGiveKeyEvent;
import net.omni.crateyBackpack.CrateyBackpack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    public void onCrateKeyGive(CrateGiveKeyEvent event) {
        if (event.isCancelled())
            return;

        event.setCancelled(true);

        OfflinePlayer player = event.getPlayer();
        Crates.CrateData crateData = event.getCrateData();

        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                "keys give " + player.getName() + " " + crateData.getId() + " 1");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getBackpackManager().unloadPlayer(event.getPlayer());
    }
}
