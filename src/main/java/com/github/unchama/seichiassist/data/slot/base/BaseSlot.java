package com.github.unchama.seichiassist.data.slot.base;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.component.ItemStackBuilder;
import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * {@link Slot} の基礎クラスです.
 * 無機能な {@link Slot} を生成可能です.
 *
 * @author karayuu
 */
public class BaseSlot implements Slot {
    private int position;

    @Nonnull
    private ItemStackBuilder builder;

    /**
     * 基本的な無機能の {@link Slot} を生成します.
     *
     * @param position {@link Inventory} への設置位置
     * @param builder {@link Inventory} へセットする {@link ItemStack} のBuilderである {@link ItemStackBuilder}
     *
     * @see Slot#getPosition()
     */
    public BaseSlot(int position, ItemStackBuilder builder) {
        this.position = position;
        this.builder = requireNonNull(builder);
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack(@Nonnull PlayerData playerData) {
        requireNonNull(playerData);
        return this.builder.build(playerData);
    }

    @Override
    public void invoke(@Nonnull InventoryClickEvent event) {
        //何もしない.
    }
}
