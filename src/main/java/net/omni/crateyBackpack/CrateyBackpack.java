package net.omni.crateyBackpack;

import net.omni.crateyBackpack.chat.ChatRenderer;
import net.omni.crateyBackpack.chat.PaperChatRenderer;
import net.omni.crateyBackpack.chat.SpigotChatRenderer;
import net.omni.crateyBackpack.config.CBPConfig;
import net.omni.crateyBackpack.config.ConfigUtil;
import net.omni.crateyBackpack.managers.MessagesManager;
import net.omni.crateyBackpack.messages.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrateyBackpack extends JavaPlugin {

    private ChatRenderer chatRenderer;

    private CBPConfig messagesConfig;

    private MessagesManager messagesManager;

    private ConfigUtil configUtil;

    @Override
    public void onDisable() {

        configUtil.flush();
        messagesManager.flush();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initChatRenderer();

        this.messagesConfig = new CBPConfig(this, "messages.yml");
        this.messagesManager = new MessagesManager(this);
        messagesManager.loadMessages();

        this.configUtil = new ConfigUtil(this);
        configUtil.load();

    }

    private void initChatRenderer() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            this.chatRenderer = new PaperChatRenderer();
            sendConsole("<green>PaperMC detected. Using PaperChatRenderer.</green>");
        } catch (ClassNotFoundException e) {
            this.chatRenderer = new SpigotChatRenderer();
            sendConsole("<gray>Spigot detected. Using SpigotChatRenderer.</gray>");
        }

        MessageUtil.init(chatRenderer);
    }

    public void sendConsole(String message) {
        chatRenderer.sendMessage(Bukkit.getConsoleSender(), chatRenderer.color(message));
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("Cratey")) {
        }
        // TODO
    }

    private void registerCommands() {
    }

    private void registerListeners() {
    }

    public void sendMessage(CommandSender sender, String message) {
        chatRenderer.sendMessage(sender, chatRenderer.color(message));
    }

    public CBPConfig getMessagesConfig() {
        return messagesConfig;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }
}
