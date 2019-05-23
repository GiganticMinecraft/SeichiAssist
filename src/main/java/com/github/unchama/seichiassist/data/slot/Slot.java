package com.github.unchama.seichiassist.data.slot;

import com.github.unchama.seichiassist.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
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
     * @param playerData {@link Player} の {@link PlayerData} ({@code null} は許容されません.)
     * @return セットされている場合 {@link ItemStack}.
     */
    @Nonnull
    ItemStack getItemStack(@Nonnull PlayerData playerData);

    /**
     * {@link InventoryClickEvent} を与えて {@link Slot} の動作を行わせます.
     *
     * @param event {@link InventoryClickEvent}
     */
    void invoke(@Nonnull InventoryClickEvent event);
}
