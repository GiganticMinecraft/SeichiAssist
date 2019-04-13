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
    ItemStack NOTHING = new ItemStack(Material.AIR);

    /**
     * この {@link Slot} が配置されている位置を返します.<br>
     * 形式は {@link Inventory#setItem(int, ItemStack)} 等で指定する {@link Bukkit} の配置数字です.
     *
     * @return {@link Bukkit} の配置番号
     */
    int getPosition();

    /**
     * この {@link Slot} にセットされている {@link ItemStack} を返します.<br>
     * もし,何もセットされていなかったら, {@link #NOTHING} を返します.
     *
     * @param playerData {@link Player} の {@link PlayerData} ({@code null} は許容されません.)
     * @return セットされている場合 {@link ItemStack}. もしセットされていなかったら {@link Slot#NOTHING}
     */
    @Nonnull
    ItemStack getItemStack(@Nonnull PlayerData playerData);

    /**
     * この {@link Slot} を与えられた {@link Inventory} にセットします.<br>
     * 通常, {@link PlayerData} を持つ {@link Player} が {@link Inventory} を開いた際に実行されます.
     *
     * @param inventory  {@link Slot} をセットする {@link Inventory} ({@code null} は許容されません.)
     * @param playerData {@link Player} の {@link PlayerData} ({@code null} は許容されません.)
     */
    void setToInventory(@Nonnull Inventory inventory, @Nonnull PlayerData playerData);

    /**
     * {@link InventoryClickEvent} を与えて {@link Slot} の動作を行わせます.
     *
     * @param event {@link InventoryClickEvent}
     */
    void invoke(@Nonnull InventoryClickEvent event);

    /**
     * {@link Slot} を構成するための {@link Builder} です.
     */
    interface Builder {
        /**
         * {@link Slot} の設置位置を指定します.
         *
         * @param position {@link Slot} の設置位置
         * @return {@link Slot.Builder}
         * @see Slot#getPosition()
         */
        @Nonnull
        Slot.Builder at(int position);

        /**
         * {@link Slot} を生成します.
         *
         * @return 生成された {@link Slot}
         */
        @Nonnull
        Slot build();
    }
}
