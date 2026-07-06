package net.omni.crateyBackpack.hook;

import me.colingrimes.cratey.Cratey;
import me.colingrimes.cratey.config.implementation.Crates;
import me.colingrimes.cratey.crate.manager.CrateManager;
import net.omni.crateyBackpack.CrateyBackpack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CrateyHook {

    private final CrateyBackpack plugin;
    private CrateManager crateManager;
    private Map<String, CrateKeyData> cachedKeyTypes;

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
        mergeVisibleKeys();
        plugin.getLogger().info("Successfully hooked into Cratey.");
    }

    private void mergeVisibleKeys() {
        List<String> visible = plugin.getConfigUtil().getVisibleKeys();
        Set<String> existing = new HashSet<>(visible);
        int added = 0;

        for (String keyId : getKeyTypes().keySet()) {
            if (!existing.contains(keyId)) {
                visible.add(keyId);
                added++;
            }
        }

        if (added > 0) {
            plugin.getConfig().set("visible-keys", visible);
            plugin.saveConfig();
            plugin.sendConsole("<green>Added " + added + " new key type(s) to visible-keys.</green>");
        }
    }

    public void refresh() {
        cachedKeyTypes = null;

        if (Bukkit.getPluginManager().isPluginEnabled("Cratey"))
            this.crateManager = Cratey.getInstance().getCrateManager();

        mergeVisibleKeys();
    }

    public Map<String, CrateKeyData> getKeyTypes() {
        if (cachedKeyTypes != null)
            return cachedKeyTypes;

        cachedKeyTypes = new LinkedHashMap<>();
        if (crateManager == null)
            return cachedKeyTypes;

        for (Map.Entry<String, Crates.CrateData> entry : crateManager.getCrateData().entrySet()) {
            Crates.CrateData data = entry.getValue();
            String keyName = ChatColor.stripColor(data.getName().toText());
            cachedKeyTypes.put(data.getId(), new CrateKeyData(data.getId(), keyName, data.getKey().clone()));
        }

        return cachedKeyTypes;
    }

    public boolean isHooked() {
        return crateManager != null;
    }

    public Optional<ItemStack> getKeyItem(String keyId) {
        return getKeyType(keyId).map(data -> data.keyItem().clone());
    }

    public Optional<CrateKeyData> getKeyType(String keyId) {
        return Optional.ofNullable(getKeyTypes().get(keyId));
    }

    public record CrateKeyData(String id, String name, ItemStack keyItem) {}
}
