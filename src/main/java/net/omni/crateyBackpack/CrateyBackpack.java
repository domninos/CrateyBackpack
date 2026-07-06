package net.omni.crateyBackpack;

import net.omni.crateyBackpack.chat.ChatRenderer;
import net.omni.crateyBackpack.chat.PaperChatRenderer;
import net.omni.crateyBackpack.chat.SpigotChatRenderer;
import net.omni.crateyBackpack.command.CrateyBackpackCommand;
import net.omni.crateyBackpack.command.KeysCommand;
import net.omni.crateyBackpack.config.CBPConfig;
import net.omni.crateyBackpack.config.ConfigUtil;
import net.omni.crateyBackpack.listener.InventoryListener;
import net.omni.crateyBackpack.listener.PlayerListener;
import net.omni.crateyBackpack.managers.BackpackManager;
import net.omni.crateyBackpack.hook.CrateyHook;
import net.omni.crateyBackpack.managers.DatabaseManager;
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
    private DatabaseManager databaseManager;
    private BackpackManager backpackManager;
    private CrateyHook crateyHook;

    @Override
    public void onDisable() {
        if (backpackManager != null)
            backpackManager.onDisable();

        configUtil.flush();
        messagesManager.flush();
        sendConsole("<red>Successfully disabled.</red>");
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

        this.databaseManager = new DatabaseManager(this);
        databaseManager.init();

        this.backpackManager = new BackpackManager(this, databaseManager);

        this.crateyHook = new CrateyHook(this);

        registerHooks();
        registerCommands();
        registerListeners();

        sendConsole("<green>Successfully started " + getDescription().getName() + "-v" + getDescription().getVersion() + " </green>");
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

    private void registerHooks() {
        if (crateyHook.isHooked())
            sendConsole("<green>Hooked into Cratey. Loaded " + crateyHook.getKeyTypes().size() + " key types.</green>");
        else
            sendConsole("<yellow>Cratey not available. Key integrations disabled.</yellow>");
    }

    private void registerCommands() {
        getCommand("keys").setExecutor(new KeysCommand(this));
        getCommand("crateybackpack").setExecutor(new CrateyBackpackCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public void sendConsole(String message) {
        chatRenderer.sendMessage(Bukkit.getConsoleSender(), chatRenderer.color(message));
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

    public ChatRenderer getChatRenderer() {
        return chatRenderer;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BackpackManager getBackpackManager() {
        return backpackManager;
    }

    public CrateyHook getCrateyHook() {
        return crateyHook;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
}
