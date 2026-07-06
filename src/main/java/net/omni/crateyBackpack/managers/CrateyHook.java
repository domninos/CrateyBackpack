package net.omni.crateyBackpack.managers;

import me.colingrimes.cratey.Cratey;
import me.colingrimes.cratey.config.implementation.Crates;
import me.colingrimes.cratey.crate.manager.CrateManager;
import net.omni.crateyBackpack.CrateyBackpack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CrateyHook {

    private final CrateyBackpack plugin;
    private CrateManager crateManager;

    public CrateyHook(CrateyBackpack plugin) {
        this.plugin = plugin;
        hook();
    }

    private void hook() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Cratey")) {
            plugin.getLogger().warning("Cratey plugin not found. Key definitions unavailable.");
            return;
        }

        this.crateManager = Cratey.getInstance().getCrateManager();
        plugin.getLogger().info("Successfully hooked into Cratey.");
    }

    public boolean isHooked() {
        return crateManager != null;
    }

    public Map<String, CrateKeyData> getKeyTypes() {
        Map<String, CrateKeyData> result = new LinkedHashMap<>();
        if (crateManager == null) return result;

        for (Map.Entry<String, Crates.CrateData> entry : crateManager.getCrateData().entrySet()) {
            Crates.CrateData data = entry.getValue();
            result.put(data.getId(), new CrateKeyData(data.getId(), data.getName().toText(), data.getKey().clone()));
        }

        return result;
    }

    public Optional<CrateKeyData> getKeyType(String keyId) {
        return Optional.ofNullable(getKeyTypes().get(keyId));
    }

    public Optional<ItemStack> getKeyItem(String keyId) {
        return getKeyType(keyId).map(data -> data.keyItem().clone());
    }

    public record CrateKeyData(String id, String name, ItemStack keyItem) {}
}
