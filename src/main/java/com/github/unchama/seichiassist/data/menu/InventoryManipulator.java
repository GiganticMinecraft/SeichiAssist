package com.github.unchama.seichiassist.data.menu;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by karayuu on 2019/05/23
 */
public class InventoryManipulator {
    @NotNull
    final Inventory inventory;
    @NotNull
    private final Map<@NotNull Integer, @NotNull Slot> slotMap;

    private InventoryManipulator(@NotNull Inventory inventory) {
        this.inventory = inventory;
        this.slotMap = new HashMap<>();
        MenuHandler.getInstance().addInventoryHolder(this);
    }

    public static InventoryManipulator from(@NotNull Inventory inventory) {
        return new InventoryManipulator(inventory);
    }

    @NotNull
    public String getTitle() {
        return inventory.getTitle();
    }

    public void setSlot(int position, @NotNull Slot slot) {
        slotMap.put(position, slot);
    }

    public void openBy(@NotNull Player player) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotMap.get(i) != null) {
                setSlot(i, slotMap.get(i).getItemStack());
            }
        }

        player.openInventory(inventory);
    }

    void invokeAndReload(int position, @NotNull InventoryClickEvent event) {
        if (event.getWhoClicked().getType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        final Slot slot = slotMap.get(position);
        slot.invoke(event);
        inventory.setItem(position, slot.getItemStack());
    }

    @NotNull
    public Inventory rawValue() {
        return this.inventory;
    }

    /**
     * 非同期で {@link Inventory} に {@link ItemStack} をセットします.
     *
     * @param position セットしたい位置
     * @param itemStack セットしたい {@link ItemStack}
     */
    private void setSlot(int position, @NotNull ItemStack itemStack) {
        Bukkit.getScheduler().runTaskAsynchronously(SeichiAssist.instance,
            () -> inventory.setItem(position, itemStack));
    }
}
