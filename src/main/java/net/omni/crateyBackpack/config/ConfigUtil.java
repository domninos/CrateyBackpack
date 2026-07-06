package net.omni.crateyBackpack.config;

import net.omni.crateyBackpack.CrateyBackpack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class ConfigUtil {
    private final CrateyBackpack plugin;

    public ConfigUtil(CrateyBackpack plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        flush();

        plugin.reloadConfig();
        load();
    }

    public void flush() {
    }

    public void load() {
        AtomicInteger savedDefaults = new AtomicInteger();

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

    private double getAndDefaultDouble(String path, double defaultVal, IntConsumer consumer) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }

        return plugin.getConfig().getDouble(path);
    }
}