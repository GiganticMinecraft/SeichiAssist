package com.github.unchama.seichiassist.data.itemstack.builder.component;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@link ItemStack} をBuildするBuilderを表すインターフェース.
 */
public interface ItemStackBuilder {
    /**
     * {@link ItemStack} の表示名を設定します.
     *
     * @param title {@link ItemStack} の表示名
     * @return このBuilder
     */
    @NotNull
    ItemStackBuilder title(@NotNull String title);

    /**
     * {@link ItemStack} のLoreを設定します.
     *
     * @param lore {@link ItemStack} のLoreとして設定する {@link String} の {@link List}
     *             {@link List} に {@code null} が含まれていた場合,その行は無視されます.
     * @return このBuilder
     */
    @NotNull
    ItemStackBuilder lore(@NotNull List<String> lore);

    /**
     * {@link ItemStack} にエンチャントを付与します.
     *
     * @return このBuilder
     */
    @NotNull
    ItemStackBuilder enchanted();

    /**
     * {@link ItemStack} の個数を指定します.
     *
     * @param number {@link ItemStack} の個数
     * @return このBuilder
     */
    @NotNull
    ItemStackBuilder number(int number);

    /**
     * Builderによって指定された各引数を用いて {@link ItemStack} を生成します
     *
     * @return 生成された {@link ItemStack}
     */
    @NotNull
    ItemStack build();
}
