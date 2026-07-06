package net.omni.crateyBackpack.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerKeys {
    private final UUID uuid;
    private final Map<String, Integer> keys;

    public PlayerKeys(UUID uuid) {
        this.uuid = uuid;
        this.keys = new ConcurrentHashMap<>();
    }

    public PlayerKeys(UUID uuid, Map<String, Integer> keys) {
        this.uuid = uuid;
        this.keys = new ConcurrentHashMap<>(keys);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, Integer> getKeys() {
        return keys;
    }

    public int getAmount(String keyId) {
        return keys.getOrDefault(keyId, 0);
    }

    public void setAmount(String keyId, int amount) {
        if (amount <= 0)
            keys.remove(keyId);
        else
            keys.put(keyId, amount);
    }

    public void addAmount(String keyId, int amount) {
        setAmount(keyId, getAmount(keyId) + amount);
    }

    public boolean takeAmount(String keyId, int amount) {
        int current = getAmount(keyId);
        if (current < amount)
            return false;
        setAmount(keyId, current - amount);
        return true;
    }
}
