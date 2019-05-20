package com.github.unchama.seichiassist.data.slot.base;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Slot} の基礎クラスです.
 * 無機能な {@link Slot} を生成可能です.
 *
 * @author karayuu
 */
public class BaseSlot implements Slot {
    private int position;

    @NotNull
    private ItemStackBuilder builder;

    /**
     * 基本的な無機能の {@link Slot} を生成します.
     *
     * @param position {@link Inventory} への設置位置
     * @param builder {@link Inventory} へセットする {@link ItemStack} のBuilderである {@link ItemStackBuilder}
     *
     * @see Slot#getPosition()
     */
    public BaseSlot(int position, @NotNull ItemStackBuilder builder) {
        this.position = position;
        this.builder = builder;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @NotNull
    @Override
    public ItemStack getItemStack(@NotNull PlayerData playerData) {
        return this.builder.build(playerData);
    }

    @Override
    public void invoke(@NotNull InventoryClickEvent event) {
        //何もしない.
    }
}
