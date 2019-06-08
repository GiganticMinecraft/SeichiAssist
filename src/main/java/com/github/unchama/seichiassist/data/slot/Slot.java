package com.github.unchama.seichiassist.data.slot;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Slot全般を表すinterfaceです.
 *
 * @author karayuu
 */
public interface Slot {
    /**
     * この {@link Slot} にセットされている {@link ItemStack} を返します.<br>
     *
     * @return セットされている場合 {@link ItemStack}.
     */
    @NotNull
    ItemStack getItemStack();

    /**
     * {@link InventoryClickEvent} を与えて {@link Slot} の動作を行わせます.
     *
     * @param event {@link InventoryClickEvent}
     */
    void invoke(@NotNull InventoryClickEvent event);
}
