package com.github.unchama.seichiassist.data.menu;

import arrow.core.Either;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.util.InventoryCreatorKt;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by karayuu on 2019/05/23
 */
public class InventoryView implements InventoryHolder {
    @NotNull
    private final Either<@NotNull Integer, @NotNull InventoryType> property;

    @NotNull
    private final String title;

    @NotNull
    private final Map<@NotNull Integer, @NotNull Slot> slotMap = new HashMap<>();

    public InventoryView(@NotNull Either<@NotNull Integer, @NotNull InventoryType> property, @NotNull String title) {
        this.property = property;
        this.title = title;
    }

    @NotNull
    public String getTitle() {
        return this.title;
    }

    public void setSlot(int position, @NotNull Slot slot) {
        slotMap.put(position, slot);
    }

    void invoke(int position, @NotNull InventoryClickEvent event) {
        final Slot slot = slotMap.get(position);
        if (slot == null) {
            return;
        }
        slot.invoke(event);
    }

    @NotNull
    public Inventory getInventory() {
        final Inventory inventory = InventoryCreatorKt.createInventory(this, property, title);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotMap.get(i) != null) {
                setItemStackAsynchronously(inventory, i, slotMap.get(i).getItemStack());
            }
        }
        return inventory;
    }

    /**
     * 非同期で {@link Inventory} に {@link ItemStack} をセットします.
     *
     * @param inventory {@link Slot} をセットする {@link Inventory}
     * @param position  セットしたい位置
     * @param itemStack セットしたい {@link ItemStack}
     */
    private void setItemStackAsynchronously(@NotNull Inventory inventory, int position, @NotNull ItemStack itemStack) {
        Bukkit.getScheduler().runTaskAsynchronously(SeichiAssist.instance,
                () -> inventory.setItem(position, itemStack));
    }
}
