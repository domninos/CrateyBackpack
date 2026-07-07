package net.omni.crateyBackpack.managers;

import net.omni.crateyBackpack.CrateyBackpack;
import net.omni.crateyBackpack.data.PlayerKeys;
import net.omni.crateyBackpack.inventory.KeysInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackpackManager {

    private final CrateyBackpack plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerKeys> cache = new ConcurrentHashMap<>();
    private final Map<UUID, KeysInventory> inventoryCache = new ConcurrentHashMap<>();

    public BackpackManager(CrateyBackpack plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        startAutoSave();
    }

    private void startAutoSave() {
        int interval = plugin.getConfigUtil().getAutoSaveInterval();
        if (interval <= 0) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllAsync();
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L);
    }

    public void saveAllAsync() {
        for (Map.Entry<UUID, PlayerKeys> entry : cache.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Integer> keys = new HashMap<>(entry.getValue().getKeys());
            databaseManager.savePlayerKeysAsync(uuid, keys);
        }
    }

    public void preloadPlayer(UUID uuid) {
        databaseManager.loadPlayerKeysAsync(uuid).thenAccept(keys ->
                Bukkit.getScheduler().runTask(plugin, () ->
                        cache.putIfAbsent(uuid, new PlayerKeys(uuid, keys))));
    }

    public Map<String, Integer> getKeys(UUID uuid) {
        return getOrLoad(uuid).getKeys();
    }

    public PlayerKeys getOrLoad(UUID uuid) {
        return cache.computeIfAbsent(uuid, u -> {
            Map<String, Integer> keys = databaseManager.loadPlayerKeys(u);
            return new PlayerKeys(u, keys);
        });
    }

    public int getAmount(UUID uuid, String keyId) {
        return getOrLoad(uuid).getAmount(keyId);
    }

    public void addKey(UUID uuid, String keyId, int amount) {
        if (amount <= 0) return;
        PlayerKeys data = getOrLoad(uuid);
        data.addAmount(keyId, amount);
        databaseManager.setKeyAsync(uuid, keyId, data.getAmount(keyId));
        refreshInventory(uuid);
    }

    public void invalidateInventories() {
        for (KeysInventory inv : inventoryCache.values())
            inv.rebuildDisplayKeys();
    }

    public void refreshInventory(UUID uuid) {
        KeysInventory inv = inventoryCache.get(uuid);
        if (inv != null)
            inv.refresh();
    }

    public boolean takeKey(UUID uuid, String keyId, int amount) {
        if (amount <= 0) return true;
        PlayerKeys data = getOrLoad(uuid);
        boolean success = data.takeAmount(keyId, amount);
        if (success) {
            int remaining = data.getAmount(keyId);
            if (remaining <= 0)
                databaseManager.removeKeyAsync(uuid, keyId);
            else
                databaseManager.setKeyAsync(uuid, keyId, remaining);
            refreshInventory(uuid);
        }
        return success;
    }

    public void setKey(UUID uuid, String keyId, int amount) {
        PlayerKeys data = getOrLoad(uuid);
        data.setAmount(keyId, amount);
        if (amount <= 0)
            databaseManager.removeKeyAsync(uuid, keyId);
        else
            databaseManager.setKeyAsync(uuid, keyId, amount);
        refreshInventory(uuid);
    }

    public void unloadPlayer(Player player) {
        unloadPlayer(player.getUniqueId());
    }

    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        cache.remove(uuid);
        inventoryCache.remove(uuid);
    }

    public void savePlayer(UUID uuid) {
        PlayerKeys data = cache.get(uuid);
        if (data != null)
            databaseManager.savePlayerKeys(uuid, data.getKeys());
    }

    public KeysInventory getCachedInventory(Player player) {
        return inventoryCache.computeIfAbsent(player.getUniqueId(),
                uuid -> new KeysInventory(plugin, player, this, plugin.getCrateyHook()));
    }

    public void onDisable() {
        saveAll();
        cache.clear();
        inventoryCache.clear();
        databaseManager.close();
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerKeys> entry : cache.entrySet())
            databaseManager.savePlayerKeys(entry.getKey(), entry.getValue().getKeys());
    }
}
