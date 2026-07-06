package net.omni.crateyBackpack.inventory;

import net.omni.crateyBackpack.CrateyBackpack;
import net.omni.crateyBackpack.managers.BackpackManager;
import net.omni.crateyBackpack.managers.CrateyHook;
import net.omni.crateyBackpack.messages.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KeysInventory implements InventoryHolder {

    private final CrateyBackpack plugin;
    private final Player player;
    private final BackpackManager backpackManager;
    private final CrateyHook crateyHook;
    private final Inventory inventory;

    private final List<Integer> keySlots = new ArrayList<>();
    private final List<CrateyHook.CrateKeyData> displayKeys = new ArrayList<>();

    public KeysInventory(CrateyBackpack plugin, Player player, BackpackManager backpackManager, CrateyHook crateyHook) {
        this.plugin = plugin;
        this.player = player;
        this.backpackManager = backpackManager;
        this.crateyHook = crateyHook;
        this.inventory = createInventory();
    }

    private Inventory createInventory() {
        Map<String, CrateyHook.CrateKeyData> allTypes = crateyHook.getKeyTypes();
        List<String> visibleKeys = plugin.getConfigUtil().getVisibleKeys();

        if (visibleKeys.isEmpty()) {
            displayKeys.addAll(allTypes.values());
        } else {
            for (String id : visibleKeys) {
                CrateyHook.CrateKeyData type = allTypes.get(id);
                if (type != null)
                    displayKeys.add(type);
            }
        }

        if (displayKeys.isEmpty())
            displayKeys.addAll(allTypes.values());

        Map<String, Integer> playerKeyAmounts = backpackManager.getKeys(player.getUniqueId());

        int keyRows = Math.max(1, (int) Math.ceil(displayKeys.size() / 7.0));
        int totalRows = Math.clamp(keyRows + 1, 1, 6);
        int size = totalRows * 9;

        String title = plugin.getConfigUtil().getGuiTitle();

        Inventory inv = plugin.getChatRenderer().createInventory(this, size, MessageUtil.parse(title));

        ItemStack filler = createFiller();
        for (int i = 0; i < size; i++)
            inv.setItem(i, filler);

        ItemStack head = createHead();
        inv.setItem(0, head);

        keySlots.clear();
        int keyIndex = 0;
        for (int row = 1; row < totalRows; row++) {
            int remaining = displayKeys.size() - keyIndex;
            if (remaining <= 0) break;

            int itemsInRow = Math.min(7, remaining);
            int startSlot = row * 9 + 1 + (7 - itemsInRow) / 2;

            for (int i = 0; i < itemsInRow; i++) {
                CrateyHook.CrateKeyData keyData = displayKeys.get(keyIndex);
                int amount = playerKeyAmounts.getOrDefault(keyData.id(), 0);
                int slot = startSlot + i;
                inv.setItem(slot, createKeyItem(keyData, amount));
                keySlots.add(slot);
                keyIndex++;
            }
        }

        return inv;
    }

    public void refresh() {
        Map<String, Integer> amounts = backpackManager.getKeys(player.getUniqueId());
        for (int i = 0; i < keySlots.size(); i++) {
            CrateyHook.CrateKeyData keyData = displayKeys.get(i);
            int amount = amounts.getOrDefault(keyData.id(), 0);
            inventory.setItem(keySlots.get(i), createKeyItem(keyData, amount));
        }
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(plugin.getConfigUtil().getFillerMaterial());
        ItemMeta meta = item.getItemMeta();
        String name = plugin.getConfigUtil().getFillerName();
        plugin.getChatRenderer().setDisplayName(meta, MessageUtil.parse(name));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setPlayerProfile(player.getPlayerProfile());

        String infoName = plugin.getConfigUtil().getInfoItemName();
        if (infoName != null) {
            infoName = infoName.replace("{player_name}", player.getName());
            plugin.getChatRenderer().setDisplayName(meta, MessageUtil.parse(infoName));
        }

        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createKeyItem(CrateyHook.CrateKeyData keyData, int amount) {
        ItemStack item = keyData.keyItem().clone();
        ItemMeta meta = item.getItemMeta();

        String nameFormat = plugin.getConfigUtil().getKeyNameFormat();
        if (nameFormat != null) {
            String name = nameFormat
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{key_name}", keyData.name());
            plugin.getChatRenderer().setDisplayName(meta, MessageUtil.parse(name));
        }

        List<String> lore;
        if (amount > 0)
            lore = plugin.getConfigUtil().getHasKeyLore();
        else
            lore = plugin.getConfigUtil().getNoKeyLore();

        if (lore != null) {
            List<String> parsedLore = new ArrayList<>();
            for (String line : lore) {
                parsedLore.add(MessageUtil.parse(line
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{key_name}", keyData.name())));
            }
            plugin.getChatRenderer().setLore(meta, parsedLore);
        }

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public UUID getPlayerUuid() {
        return player.getUniqueId();
    }

    public CrateyHook getCrateyHook() {
        return crateyHook;
    }

    public BackpackManager getBackpackManager() {
        return backpackManager;
    }
}
