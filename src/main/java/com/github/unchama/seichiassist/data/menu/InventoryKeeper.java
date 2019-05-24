package com.github.unchama.seichiassist.data.menu;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
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
import java.util.function.Function;

/**
 * Created by karayuu on 2019/05/23
 */
public class InventoryKeeper {
    @NotNull
    private final Inventory inventory;
    @NotNull
    private final Map<@NotNull Integer, @NotNull Function<@NotNull PlayerData, @NotNull ? extends Slot>> slotMap;

    private InventoryKeeper(@NotNull Inventory inventory) {
        this.inventory = inventory;
        this.slotMap = new HashMap<>();
        MenuHandler.getInstance().addInventoryHolder(this);
    }

    public static InventoryKeeper from(@NotNull Inventory inventory) {
        return new InventoryKeeper(inventory);
    }

    @NotNull
    public String getTitle() {
        return inventory.getTitle();
    }

    public void setSlot(int position, Function<PlayerData, ? extends Slot> slotFunction) {
        slotMap.put(position, slotFunction);
    }

    public void openBy(@NotNull Player player) {
        final PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotMap.get(i) != null) {
                setSlot(i, slotMap.get(i).apply(playerData).getItemStack());
            }
        }

        player.openInventory(inventory);
    }

    public void invokeAndReload(int position, @NotNull InventoryClickEvent event) {
        if (event.getWhoClicked().getType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final PlayerData playerData = SeichiAssist.playermap.get(player.getUniqueId());

        final Slot slot = slotMap.get(position).apply(playerData);
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
