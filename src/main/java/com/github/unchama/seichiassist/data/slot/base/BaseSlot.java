package com.github.unchama.seichiassist.data.slot.base;

import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    private ItemStack itemStack;

    protected BaseSlot(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * 基本的な無機能の {@link Slot} を生成します.
     */
    public static BaseSlot from(@NotNull ItemStack itemStack) {
        return new BaseSlot(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public void invoke(@NotNull InventoryClickEvent event) {
        //何もしない.
    }
}
