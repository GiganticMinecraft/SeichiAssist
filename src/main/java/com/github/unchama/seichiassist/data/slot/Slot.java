package com.github.unchama.seichiassist.data.slot;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

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
    @Nonnull
    ItemStack getItemStack();

    /**
     * {@link InventoryClickEvent} を与えて {@link Slot} の動作を行わせます.
     *
     * @param event {@link InventoryClickEvent}
     */
    void invoke(@Nonnull InventoryClickEvent event);
}
