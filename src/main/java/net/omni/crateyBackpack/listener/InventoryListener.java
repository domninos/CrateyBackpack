package net.omni.crateyBackpack.listener;

import net.omni.crateyBackpack.CrateyBackpack;
import net.omni.crateyBackpack.inventory.KeysInventory;
import net.omni.crateyBackpack.managers.BackpackManager;
import net.omni.crateyBackpack.hook.CrateyHook;
import net.omni.crateyBackpack.util.SoundUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryListener implements Listener {

    private final CrateyBackpack plugin;

    public InventoryListener(CrateyBackpack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder(false);
        if (!(holder instanceof KeysInventory keysInventory))
            return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        int slot = event.getSlot();

        if (slot == 25) {
            keysInventory.prevPage();
            return;
        }
        if (slot == 26) {
            keysInventory.nextPage();
            return;
        }

        String keyId = keysInventory.getKeyIdAtSlot(slot);
        if (keyId == null)
            return;

        BackpackManager backpackManager = keysInventory.getBackpackManager();

        int amount = backpackManager.getAmount(player.getUniqueId(), keyId);
        if (amount <= 0) {
            String failSound = plugin.getConfigUtil().getFailSound();
            if (failSound != null)
                SoundUtil.playSound(player, failSound);
            return;
        }

        CrateyHook.CrateKeyData keyData = keysInventory.getCrateyHook().getKeyType(keyId).orElse(null);
        if (keyData == null)
            return;

        boolean taken = backpackManager.takeKey(player.getUniqueId(), keyId, 1);
        if (!taken)
            return;

        ItemStack keyItem = keyData.keyItem().clone();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(keyItem);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), leftover.get(0));
        }

        String claimSound = plugin.getConfigUtil().getClaimSound();
        if (claimSound != null)
            SoundUtil.playSound(player, claimSound);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder(false);
        if (holder instanceof KeysInventory)
            event.setCancelled(true);
    }
}
