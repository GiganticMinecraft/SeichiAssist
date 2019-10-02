package com.github.unchama.buildassist.util;

import com.github.unchama.buildassist.BuildAssist$;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class AsyncInventorySetter {
    public static void setItemAsync(Inventory inventory, int slot, ItemStack item) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(BuildAssist$.MODULE$.plugin(), () -> inventory.setItem(slot, item));
    }
}
