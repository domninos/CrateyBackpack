package net.omni.crateyBackpack.command;

import net.omni.crateyBackpack.CrateyBackpack;
import net.omni.crateyBackpack.inventory.KeysInventory;
import net.omni.crateyBackpack.managers.BackpackManager;
import net.omni.crateyBackpack.managers.CrateyHook;
import net.omni.crateyBackpack.messages.Messages;
import net.omni.crateyBackpack.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            case "give" -> giveKeys(sender, args);
            case "take" -> takeKeys(sender, args);
            case "set" -> setKeys(sender, args);
            case "show" -> showKey(sender, args);
            case "hide" -> hideKey(sender, args);
            case "list" -> listKeys(sender);
            default ->
                    plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys [give|take|set|show|hide|list]"));
        }
        return true;
    }

    private void openBackpack(Player player) {
        KeysInventory inv = plugin.getBackpackManager().getCachedInventory(player);
        player.openInventory(inv.getInventory());

        String sound = plugin.getMessagesConfig().getString("gui.open-sound");
        if (sound != null)
            SoundUtil.playSound(player, sound);
    }

    private void giveKeys(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys give <player> <keyId> [amount]"));
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
        plugin.sendMessage(sender, Messages.KEY_GIVEN
                .replace("player", target.getName() != null ? target.getName() : args[1])
                .replace("key_name", name)
                .replace("amount", String.valueOf(amount)));
    }

    private void takeKeys(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys take <player> <keyId> [amount]"));
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
                    .replace("player", target.getName() != null ? target.getName() : args[1])
                    .replace("key_id", keyId));
            return;
        }

        String name = plugin.getCrateyHook().getKeyType(keyId).get().name();
        plugin.sendMessage(sender, Messages.KEY_TAKEN
                .replace("player", target.getName() != null ? target.getName() : args[1])
                .replace("key_name", name)
                .replace("amount", String.valueOf(amount)));
    }

    private void setKeys(CommandSender sender, String[] args) {
        if (args.length < 4) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys set <player> <keyId> <amount>"));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, Messages.PLAYER_NOT_FOUND.replace("player", args[1]));
            return;
        }
        String keyId = args[2].toLowerCase();
        int amount = parseInt(args[3], 0);
        if (amount < 0) amount = 0;

        if (amount > 0 && plugin.getCrateyHook().getKeyType(keyId).isEmpty()) {
            plugin.sendMessage(sender, Messages.KEY_NOT_FOUND.replace("key_id", keyId));
            return;
        }

        plugin.getBackpackManager().setKey(target.getUniqueId(), keyId, amount);

        String name = plugin.getCrateyHook().getKeyType(keyId).map(CrateyHook.CrateKeyData::name).orElse(keyId);
        plugin.sendMessage(sender, Messages.KEY_SET
                .replace("player", target.getName() != null ? target.getName() : args[1])
                .replace("key_name", name)
                .replace("amount", String.valueOf(amount)));
    }

    private void showKey(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys show <keyId>"));
            return;
        }
        String keyId = args[1].toLowerCase();
        if (plugin.getCrateyHook().getKeyType(keyId).isEmpty()) {
            plugin.sendMessage(sender, Messages.KEY_NOT_FOUND.replace("key_id", keyId));
            return;
        }

        plugin.getConfigUtil().addVisibleKey(keyId);

        String name = plugin.getCrateyHook().getKeyType(keyId).get().name();
        plugin.sendMessage(sender, Messages.VISIBILITY_CHANGED
                .replace("key_name", name)
                .replace("visibility", "visible"));
    }

    private void hideKey(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/keys hide <keyId>"));
            return;
        }
        String keyId = args[1].toLowerCase();

        plugin.getConfigUtil().removeVisibleKey(keyId);

        String name = plugin.getCrateyHook().getKeyType(keyId).map(CrateyHook.CrateKeyData::name).orElse(keyId);
        plugin.sendMessage(sender, Messages.VISIBILITY_CHANGED
                .replace("key_name", name)
                .replace("visibility", "hidden"));
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
}
