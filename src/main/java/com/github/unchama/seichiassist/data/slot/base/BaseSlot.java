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
    @NotNull
    private ItemStackBuilder builder;

    protected BaseSlot(@NotNull ItemStackBuilder builder) {
        this.builder = builder;
    }

    /**
     * 基本的な無機能の {@link Slot} を生成します.
     *
     * @param builder {@link Inventory} へセットする {@link ItemStack} のBuilderである {@link ItemStackBuilder}
     */
    public static BaseSlot from(@NotNull ItemStackBuilder builder) {
        return new BaseSlot(builder);
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
