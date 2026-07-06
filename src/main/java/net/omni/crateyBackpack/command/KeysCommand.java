package net.omni.crateyBackpack.command;

import net.omni.crateyBackpack.CrateyBackpack;
import net.omni.crateyBackpack.hook.CrateyHook;
import net.omni.crateyBackpack.inventory.KeysInventory;
import net.omni.crateyBackpack.messages.MessageUtil;
import net.omni.crateyBackpack.messages.Messages;
import net.omni.crateyBackpack.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeysCommand implements CommandExecutor {

    private final CrateyBackpack plugin;

    public KeysCommand(CrateyBackpack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                plugin.sendMessage(sender, Messages.ONLY_PLAYERS.toString());
                return true;
            }

            if (!player.hasPermission("crateybackpack.use")) {
                plugin.sendMessage(sender, Messages.NO_PERMS.toString());
                return true;
            }

            openBackpack(player);
            return true;
        }

        if (!sender.hasPermission("crateybackpack.admin")) {
            plugin.sendMessage(sender, Messages.NO_PERMS.toString());
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help" -> sendHelp(sender);
            case "about" -> sender.sendMessage(MessageUtil.parse(getAboutText()));
            case "give" -> giveKeys(sender, args);
            case "take" -> takeKeys(sender, args);
            case "set" -> setKeys(sender, args);
            case "show" -> showKey(sender, args);
            case "hide" -> hideKey(sender, args);
            case "list" -> listKeys(sender);
            default ->
                    plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys [help|about|give|take|set|show|hide|list]"));
        }
        return true;
    }

    private void openBackpack(Player player) {
        KeysInventory inv = plugin.getBackpackManager().getCachedInventory(player);
        inv.buildPage(0);
        player.openInventory(inv.getInventory());

        String sound = plugin.getConfigUtil().getOpenSound();
        if (sound != null)
            SoundUtil.playSound(player, sound);
    }

    private void sendHelp(CommandSender sender) {
        StringBuilder helpBuilder = new StringBuilder();

        helpBuilder.append("\n<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>\n");
        helpBuilder.append("  <gradient:#00AAFF:#55FFFF><bold>CrateyBackpack</bold></gradient> <gray>\n\n");

        if (sender.hasPermission("crateybackpack.use")) {
            MessageUtil.append("keys", "Opens your key backpack.", helpBuilder, "keybackpack, kb");
            MessageUtil.append("keys <#55FFFF>help</#55FFFF>", "Shows this help menu.", helpBuilder);
            MessageUtil.append("keys <#55FFFF>about</#55FFFF>", "Shows plugin information.", helpBuilder);

            if (sender.hasPermission("crateybackpack.admin")) {
                MessageUtil.append("keys give <#55FFFF>(player) (keyId) [amount]</#55FFFF>", "Gives keys to a player.", helpBuilder);
                MessageUtil.append("keys take <#55FFFF>(player) (keyId) [amount]</#55FFFF>", "Takes keys from a player.", helpBuilder);
                MessageUtil.append("keys set <#55FFFF>(player) (keyId) {amount}</#55FFFF>", "Sets a player's key count.", helpBuilder);
                MessageUtil.append("keys show <#55FFFF>(keyId)</#55FFFF>", "Shows a key type in /keys.", helpBuilder);
                MessageUtil.append("keys hide <#55FFFF>(keyId)</#55FFFF>", "Hides a key type from /keys.", helpBuilder);
                MessageUtil.append("keys <#55FFFF>list</#55FFFF>", "Lists all key types.", helpBuilder);
            }
        }

        helpBuilder.append("<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>");

        sender.sendMessage(MessageUtil.parse(helpBuilder.toString()));
    }

    private String getAboutText() {
        String pluginName = plugin.getDescription().getName();
        String version = plugin.getDescription().getVersion();
        String author = plugin.getDescription().getAuthors().getFirst();
        String githubUrl = "https://github.com/domninos/CrateyBackpack";
        String discordUrl = "https://discord.gg/7CuCtDHmQ3";

        return "<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>\n" +
                "  <gradient:#00AAFF:#55FFFF><bold>" + pluginName + "</bold></gradient>\n\n" +
                "  <yellow>Version:</yellow> <white>" + version + "</white>\n" +
                "  <yellow>Author:</yellow> <aqua>" + author + "</aqua>\n\n" +
                "  <white>Links: </white>" +
                "<click:open_url:'" + githubUrl + "'><hover:show_text:'<gray>View source code on GitHub</gray>'><dark_purple>[GitHub]</dark_purple></hover></click> " +
                "<click:open_url:'" + discordUrl + "'><hover:show_text:'<gray>Join the support Discord</gray>'><blue>[Discord]</blue></hover></click>\n" +
                "<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>";
    }

    private void giveKeys(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys give (player) (keyId) [amount]"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, Messages.PLAYER_NOT_FOUND.replace("player", args[1]));
            return;
        }

        String keyId = args[2].toLowerCase();
        int amount = args.length > 3 ? parseInt(args[3], 1) : 1;
        if (amount < 1) amount = 1;

        if (plugin.getCrateyHook().getKeyType(keyId).isEmpty()) {
            plugin.sendMessage(sender, Messages.KEY_NOT_FOUND.replace("key_id", keyId));
            return;
        }

        plugin.getBackpackManager().addKey(target.getUniqueId(), keyId, amount);

        String name = plugin.getCrateyHook().getKeyType(keyId).get().name();
        plugin.sendMessage(sender,
                Messages.KEY_GIVEN.replace(
                        "player", target.getName() != null ? target.getName() : args[1],
                        "key_name", name,
                        "amount", String.valueOf(amount)
                ));
    }

    private void takeKeys(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys take (player) (keyId) [amount]"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, Messages.PLAYER_NOT_FOUND.replace("player", args[1]));
            return;
        }

        String keyId = args[2].toLowerCase();
        int amount = args.length > 3 ? parseInt(args[3], 1) : 1;
        if (amount < 1) amount = 1;

        if (plugin.getCrateyHook().getKeyType(keyId).isEmpty()) {
            plugin.sendMessage(sender, Messages.KEY_NOT_FOUND.replace("key_id", keyId));
            return;
        }

        boolean success = plugin.getBackpackManager().takeKey(target.getUniqueId(), keyId, amount);
        if (!success) {
            plugin.sendMessage(sender, Messages.KEY_TAKE_FAIL
                    .replace(
                            "player", target.getName() != null ? target.getName() : args[1],
                            "key_id", keyId
                    ));
            return;
        }

        String name = plugin.getCrateyHook().getKeyType(keyId).get().name();
        plugin.sendMessage(sender,
                Messages.KEY_TAKEN.replace(
                        "player", target.getName() != null ? target.getName() : args[1],
                        "key_name", name,
                        "amount", String.valueOf(amount)
                ));
    }

    private void setKeys(CommandSender sender, String[] args) {
        if (args.length < 4) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys set (player) (keyId) {amount}"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, Messages.PLAYER_NOT_FOUND.replace("player", args[1]));
            return;
        }

        String keyId = args[2].toLowerCase();
        int amount = parseInt(args[3], 0);

        if (amount < 0)
            amount = 0;

        if (amount > 0 && plugin.getCrateyHook().getKeyType(keyId).isEmpty()) {
            plugin.sendMessage(sender, Messages.KEY_NOT_FOUND.replace("key_id", keyId));
            return;
        }

        plugin.getBackpackManager().setKey(target.getUniqueId(), keyId, amount);

        String name = plugin.getCrateyHook().getKeyType(keyId).map(CrateyHook.CrateKeyData::name).orElse(keyId);
        plugin.sendMessage(sender,
                Messages.KEY_SET.replace(
                        "player", target.getName() != null ? target.getName() : args[1],
                        "key_name", name,
                        "amount", String.valueOf(amount)
                ));
    }

    private void showKey(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys show (keyId)"));
            return;
        }

        String keyId = args[1].toLowerCase();
        if (plugin.getCrateyHook().getKeyType(keyId).isEmpty()) {
            plugin.sendMessage(sender, Messages.KEY_NOT_FOUND.replace("key_id", keyId));
            return;
        }

        plugin.getConfigUtil().addVisibleKey(keyId);

        String name = plugin.getCrateyHook().getKeyType(keyId).get().name();
        plugin.sendMessage(sender,
                Messages.VISIBILITY_CHANGED.replace(
                        "key_name", name,
                        "visibility", "visible"
                ));
    }

    private void hideKey(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys hide (keyId)"));
            return;
        }

        String keyId = args[1].toLowerCase();

        plugin.getConfigUtil().removeVisibleKey(keyId);

        String name = plugin.getCrateyHook().getKeyType(keyId).map(CrateyHook.CrateKeyData::name).orElse(keyId);
        plugin.sendMessage(sender,
                Messages.VISIBILITY_CHANGED.replace(
                        "key_name", name,
                        "visibility", "visible"
                ));
    }

    private void listKeys(CommandSender sender) {
        Map<String, CrateyHook.CrateKeyData> types = plugin.getCrateyHook().getKeyTypes();
        if (types.isEmpty()) {
            plugin.sendMessage(sender, "<red>No key types found.</red>");
            return;
        }

        List<String> visible = plugin.getConfigUtil().getVisibleKeys();
        boolean allVisible = visible.isEmpty();

        StringBuilder msg = new StringBuilder("<gold>=== Key Types ===</gold>\n");
        for (Map.Entry<String, CrateyHook.CrateKeyData> entry : types.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue().name();
            boolean isVisible = allVisible || visible.contains(id);
            msg.append(" <gray>-</gray> <white>")
                    .append(name)
                    .append("</white> <dark_gray>(</dark_gray><gray>")
                    .append(id)
                    .append("</gray><dark_gray>)</dark_gray> ")
                    .append(isVisible ? "<green>[visible]</green>" : "<red>[hidden]</red>")
                    .append("\n");
        }

        plugin.sendMessage(sender, msg.toString());
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public void register() {
        PluginCommand cmd = plugin.getCommand("keys");

        if (cmd == null) {
            plugin.sendConsole("<red>/keys is not a command.</red>");
            return;
        }

        cmd.setTabCompleter((sender, command, label, args) -> {
            List<String> subcommands = new ArrayList<>();
            List<String> completions = new ArrayList<>();

            if (args.length == 1) {
                subcommands.add("help");
                subcommands.add("about");

                if (sender.hasPermission("crateybackpack.admin")) {
                    subcommands.add("give");
                    subcommands.add("take");
                    subcommands.add("set");
                    subcommands.add("show");
                    subcommands.add("hide");
                    subcommands.add("list");
                }

                StringUtil.copyPartialMatches(args[0], subcommands, completions);

                return completions;
            } else if (args.length >= 2) {
                if ((args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set"))
                        && sender.hasPermission("crateybackpack.admin")) {
                    if (args.length == 2)
                        return null;
                    else if (args.length == 3)
                        completions.addAll(plugin.getCrateyHook().getKeyTypes().keySet());
                    else if (args.length == 4)
                        completions.add("[amount]");

                    return completions;
                } else if ((args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("hide"))
                        && sender.hasPermission("crateybackpack.admin")) {
                    if (args.length == 2)
                        completions.addAll(plugin.getCrateyHook().getKeyTypes().keySet());

                    return completions;
                }
            }

            return Collections.emptyList();
        });

        cmd.setExecutor(this);
    }
}
