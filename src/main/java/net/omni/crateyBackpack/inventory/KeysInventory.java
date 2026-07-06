package net.omni.crateyBackpack.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.omni.crateyBackpack.CrateyBackpack;
import net.omni.crateyBackpack.hook.CrateyHook;
import net.omni.crateyBackpack.managers.BackpackManager;
import net.omni.crateyBackpack.messages.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KeysInventory implements InventoryHolder {

    private static final int INVENTORY_SIZE = 36;
    private static final int ITEMS_PER_PAGE = 14;
    private static final int PREV_SLOT = 34;
    private static final int NEXT_SLOT = 35;

    private final CrateyBackpack plugin;
    private final Player player;
    private final BackpackManager backpackManager;
    private final CrateyHook crateyHook;
    private final Inventory inventory;

    private final List<Integer> keySlots = new ArrayList<>();
    private final List<CrateyHook.CrateKeyData> displayKeys = new ArrayList<>();
    private int currentPage = 0;
    private ItemStack cachedFiller;
    private ItemStack cachedPrevArrow;
    private ItemStack cachedNextArrow;

    public KeysInventory(CrateyBackpack plugin, Player player, BackpackManager backpackManager, CrateyHook crateyHook) {
        this.plugin = plugin;
        this.player = player;
        this.backpackManager = backpackManager;
        this.crateyHook = crateyHook;

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

        String title = plugin.getConfigUtil().getGuiTitle();
        this.inventory = plugin.getChatRenderer().createInventory(this, INVENTORY_SIZE, MessageUtil.parse(title));

        buildPage(0);
    }

    public void buildPage(int page) {
        this.currentPage = page;
        keySlots.clear();

        ItemStack filler = createFiller();
        for (int i = 0; i < INVENTORY_SIZE; i++)
            inventory.setItem(i, filler);

        Map<String, Integer> amounts = backpackManager.getKeys(player.getUniqueId());
        int totalPages = getTotalPages();
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, displayKeys.size());
        int remaining = end - start;

        // Row 1 (slots 9-17): keys centered, max 7, filler borders at 9 and 17
        int count1 = Math.min(7, remaining);
        int row1Start = 10 + (7 - count1) / 2;
        for (int i = 0; i < count1; i++) {
            CrateyHook.CrateKeyData keyData = displayKeys.get(start + i);
            int amount = amounts.getOrDefault(keyData.id(), 0);
            int slot = row1Start + i;
            inventory.setItem(slot, createKeyItem(keyData, amount));
            keySlots.add(slot);
        }

        // Row 2 (slots 18-26): keys centered in 19-25
        int count2 = Math.min(7, remaining - count1);
        int row2Start = 19 + (7 - count2) / 2;
        for (int i = 0; i < count2; i++) {
            CrateyHook.CrateKeyData keyData = displayKeys.get(start + count1 + i);
            int amount = amounts.getOrDefault(keyData.id(), 0);
            int slot = row2Start + i;
            inventory.setItem(slot, createKeyItem(keyData, amount));
            keySlots.add(slot);
        }

        if (page > 0)
            inventory.setItem(PREV_SLOT, createNavItem("<gold>← Previous</gold>"));

        if (page < totalPages - 1)
            inventory.setItem(NEXT_SLOT, createNavItem("<gold>Next →</gold>"));
    }

    private ItemStack createFiller() {
        if (cachedFiller != null)
            return cachedFiller;

        cachedFiller = new ItemStack(plugin.getConfigUtil().getFillerMaterial());
        ItemMeta meta = cachedFiller.getItemMeta();
        String name = plugin.getConfigUtil().getFillerName();
        plugin.getChatRenderer().setDisplayName(meta, MessageUtil.parse(name));
        cachedFiller.setItemMeta(meta);
        return cachedFiller;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) displayKeys.size() / ITEMS_PER_PAGE);
    }

    private ItemStack createKeyItem(CrateyHook.CrateKeyData keyData, int amount) {
        ItemStack item = keyData.keyItem().clone();
        ItemMeta meta = item.getItemMeta();

        String nameFormat = plugin.getConfigUtil().getKeyNameFormat();
        Component originalName = meta.customName();
        if (originalName == null)
            originalName = Component.text(keyData.name());

        if (nameFormat != null) {
            int idx = nameFormat.indexOf("{key_name}");

            if (idx >= 0) {
                String prefixStr = nameFormat.substring(0, idx)
                        .replace("{amount}", String.valueOf(amount));
                String suffixStr = nameFormat.substring(idx + "{key_name}".length())
                        .replace("{amount}", String.valueOf(amount));

                Component prefix = prefixStr.isEmpty() ? Component.empty()
                        : MiniMessage.miniMessage().deserialize(prefixStr);
                Component suffix = suffixStr.isEmpty() ? Component.empty()
                        : MiniMessage.miniMessage().deserialize(suffixStr);

                meta.customName(prefix.append(originalName).append(suffix));
            } else {
                String name = nameFormat
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{key_name}", keyData.name());
                plugin.getChatRenderer().setDisplayName(meta, MessageUtil.parse(name));
            }
        }

        List<String> allLore = new ArrayList<>();

        List<Component> keyLore = item.lore();
        if (keyLore != null) {
            for (Component comp : keyLore)
                allLore.add(LegacyComponentSerializer.legacySection().serialize(comp));
        }

        List<String> configLore = amount > 0
                ? plugin.getConfigUtil().getHasKeyLore()
                : plugin.getConfigUtil().getNoKeyLore();

        if (configLore != null) {
            for (String line : configLore) {
                allLore.add(MessageUtil.parse(line
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{key_name}", keyData.name())));
            }
        }

        plugin.getChatRenderer().setLore(meta, allLore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(String name) {
        if (name.contains("Previous")) {
            if (cachedPrevArrow != null)
                return cachedPrevArrow;

            cachedPrevArrow = buildArrow(name);
            return cachedPrevArrow;
        }

        if (cachedNextArrow != null)
            return cachedNextArrow;

        cachedNextArrow = buildArrow(name);
        return cachedNextArrow;
    }

    private ItemStack buildArrow(String name) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        plugin.getChatRenderer().setDisplayName(meta, MessageUtil.parse(name));
        item.setItemMeta(meta);
        return item;
    }

    public void refresh() {
        buildPage(currentPage);
    }

    public void nextPage() {
        if (currentPage < getTotalPages() - 1)
            buildPage(currentPage + 1);
    }

    public void prevPage() {
        if (currentPage > 0)
            buildPage(currentPage - 1);
    }

    public String getKeyIdAtSlot(int slot) {
        int idx = keySlots.indexOf(slot);
        if (idx < 0) return null;
        return displayKeys.get(currentPage * ITEMS_PER_PAGE + idx).id();
    }

    public int getCurrentPage() {
        return currentPage;
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
