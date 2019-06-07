package com.github.unchama.seichiassist.data.menu;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.slot.Slot;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by karayuu on 2019/05/23
 */
public class InventoryView implements InventoryHolder {
    @Nullable
    private final InventoryType type;

    @Nullable
    private final Integer size;

    @NotNull
    private final Text title;

    @NotNull
    private final Map<@NotNull Integer, @NotNull Slot> slotMap = new HashMap<>();

    /**
     * {@link InventoryView} を作成します.
     *
     * @param type  作成したい {@link Inventory} の {@link InventoryType}
     * @param title 作成したい {@link Inventory} の表示名
     */
    public InventoryView(@NotNull InventoryType type, @NotNull Text title) {
        this.type = type;
        this.size = null;
        this.title = title;
    }

    public InventoryView(@NotNull Integer size, @NotNull Text title) {
        this.type = null;
        this.size = size;
        this.title = title;
    }

    @NotNull
    public String getTitle() {
        return this.title.stringValue();
    }

    public void setSlot(int position, @NotNull Slot slot) {
        slotMap.put(position, slot);
    }

    void invokeAndReload(int position, @NotNull InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
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
    public Inventory getInventory() {
        final Inventory inventory = createInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotMap.get(i) != null) {
                setSlotAsynchronously(inventory, i, slotMap.get(i).getItemStack());
            }
        }
        return inventory;
    }

    private Inventory createInventory() {
        if (type == null) {
            assert size != null;
            return Bukkit.createInventory(this, size, title.stringValue());
        } else if (size == null) {
            return Bukkit.createInventory(this, type, title.stringValue());
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * 非同期で {@link Inventory} に {@link ItemStack} をセットします.
     *
     * @param inventory {@link Slot} をセットする {@link Inventory}
     * @param position  セットしたい位置
     * @param itemStack セットしたい {@link ItemStack}
     */
    private void setSlotAsynchronously(@NotNull Inventory inventory, int position, @NotNull ItemStack itemStack) {
        Bukkit.getScheduler().runTaskAsynchronously(SeichiAssist.instance,
                () -> inventory.setItem(position, itemStack));
    }
}
