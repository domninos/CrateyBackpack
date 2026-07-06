package net.omni.crateyBackpack.command;

import net.omni.crateyBackpack.CrateyBackpack;
import net.omni.crateyBackpack.messages.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CrateyBackpackCommand implements CommandExecutor {

    private final CrateyBackpack plugin;

    public CrateyBackpackCommand(CrateyBackpack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("crateybackpack.admin")) {
            plugin.sendMessage(sender, Messages.NO_PERMS.toString());
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/crateybackpack reload"));
            return true;
        }

        plugin.getConfigUtil().reloadConfig();
        plugin.getMessagesManager().loadMessages();
        plugin.sendMessage(sender, Messages.RELOADED.toString());
        return true;
    }
}
