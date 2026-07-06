package net.omni.crateyBackpack.config;

import net.omni.crateyBackpack.CrateyBackpack;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class ConfigUtil {
    private final CrateyBackpack plugin;

    private String databaseFile;
    private int autoSaveInterval;
    private List<String> visibleKeys;
    private Material fillerMaterial;

    private String guiTitle;
    private String keyNameFormat;
    private String infoItemName;
    private String fillerName;
    private List<String> hasKeyLore;
    private List<String> noKeyLore;
    private String openSound;
    private String claimSound;
    private String failSound;

    public ConfigUtil(CrateyBackpack plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        flush();

        plugin.reloadConfig();
        load();
    }

    public void flush() {
        if (visibleKeys != null) {
            visibleKeys.clear();
            visibleKeys = null;
        }

        if (hasKeyLore != null) {
            hasKeyLore.clear();
            hasKeyLore = null;
        }

        if (noKeyLore != null) {
            noKeyLore.clear();
            noKeyLore = null;
        }
    }

    public void load() {
        AtomicInteger savedDefaults = new AtomicInteger();

        this.databaseFile = getAndDefaultString("database.file", "data/backpack.db", savedDefaults::getAndAdd);
        this.autoSaveInterval = getAndDefaultInt("auto-save-interval", 300, savedDefaults::getAndAdd);
        this.visibleKeys = plugin.getConfig().getStringList("visible-keys");

        String fillerName = getAndDefaultString("gui.filler-material", "LIGHT_BLUE_STAINED_GLASS_PANE", savedDefaults::getAndAdd);
        this.fillerMaterial = Material.matchMaterial(fillerName);
        if (this.fillerMaterial == null)
            this.fillerMaterial = Material.LIGHT_BLUE_STAINED_GLASS_PANE;

        this.guiTitle = getAndDefaultString("gui.title", "<gradient:#00AAFF:#55FFFF>Your Backpack</gradient>", savedDefaults::getAndAdd);
        this.keyNameFormat = getAndDefaultString("gui.key-name", "<gold>{amount}x {key_name}</gold>", savedDefaults::getAndAdd);
        this.infoItemName = getAndDefaultString("gui.info-item", "<gold>{player_name}'s Key Backpack</gold>", savedDefaults::getAndAdd);
        this.fillerName = getAndDefaultString("gui.filler-name", " ", savedDefaults::getAndAdd);
        this.hasKeyLore = plugin.getConfig().getStringList("gui.has-key-lore");
        this.noKeyLore = plugin.getConfig().getStringList("gui.no-key-lore");
        this.openSound = getAndDefaultString("gui.open-sound", "BLOCK_CHEST_OPEN", savedDefaults::getAndAdd);
        this.claimSound = getAndDefaultString("gui.claim-sound", "ENTITY_ITEM_PICKUP", savedDefaults::getAndAdd);
        this.failSound = getAndDefaultString("gui.fail-sound", "BLOCK_NOTE_BLOCK_BASS", savedDefaults::getAndAdd);

        if (savedDefaults.get() > 0) {
            plugin.saveConfig();
            plugin.sendConsole("<green>Successfully loaded " + savedDefaults.get() + " default configuration(s)</green>");
        }

        plugin.sendConsole("<green>Successfully loaded config.yml</green>");
    }

    private String getAndDefaultString(String path, String defaultVal, IntConsumer consumer) {
        String temp = plugin.getConfig().getString(path);

        if (temp == null) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }

        return temp;
    }

    private int getAndDefaultInt(String path, int defaultVal, IntConsumer consumer) {
        int temp = plugin.getConfig().getInt(path);

        if (!plugin.getConfig().contains(path) || temp == 0) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }

        return temp;
    }

    public void addVisibleKey(String keyId) {
        if (!visibleKeys.contains(keyId))
            visibleKeys.add(keyId);
        plugin.getConfig().set("visible-keys", visibleKeys);
        plugin.saveConfig();
    }

    public void removeVisibleKey(String keyId) {
        visibleKeys.remove(keyId);
        plugin.getConfig().set("visible-keys", visibleKeys);
        plugin.saveConfig();
    }

    public String getDatabaseFile() {
        return databaseFile;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public List<String> getVisibleKeys() {
        return visibleKeys;
    }

    public Material getFillerMaterial() {
        return fillerMaterial;
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public String getKeyNameFormat() {
        return keyNameFormat;
    }

    public String getInfoItemName() {
        return infoItemName;
    }

    public String getFillerName() {
        return fillerName;
    }

    public List<String> getHasKeyLore() {
        return hasKeyLore;
    }

    public List<String> getNoKeyLore() {
        return noKeyLore;
    }

    public String getOpenSound() {
        return openSound;
    }

    public String getClaimSound() {
        return claimSound;
    }

    public String getFailSound() {
        return failSound;
    }
}
