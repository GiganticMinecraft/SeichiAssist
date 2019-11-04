package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class AsyncInventorySetter {
    public static void setItemAsync(Inventory inventory, int slot, ItemStack item) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(SeichiAssist.instance(), () -> inventory.setItem(slot, item));
    }
}
