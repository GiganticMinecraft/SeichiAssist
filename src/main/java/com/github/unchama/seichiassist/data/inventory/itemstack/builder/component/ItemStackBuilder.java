package com.github.unchama.seichiassist.data.inventory.itemstack.builder.component;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.text.Text;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * プレイヤーデータを用いてItemStackをBuildするBuilderを表すインターフェース.
 */
public interface ItemStackBuilder {
    /**
     * ItemStack(ItemStackBuilder)の表示名を設定します.
     *
     * @param title PlayerDataを受け取り,表示名を返すFunction
     * @return このBuilder
     */
    @Nonnull
    ItemStackBuilder title(@Nonnull Function<PlayerData, Text> title);

    /**
     * ItemStack(ItemStackBuilder)のloreを設定します.
     *
     * @param lore PlayerDataを受け取り,loreを返すFunction
     *             {@link List} に {@code null} が含まれていた場合,その行は無視されます.
     * @return このBuilder
     */
    @Nonnull
    ItemStackBuilder lore(@Nonnull Function<PlayerData, List<Text>> lore);

    /**
     * ItemStack(ItemStackBuilder)にエンチャントを付与します.
     *
     * @return このBuilder
     */
    @Nonnull
    ItemStackBuilder enchanted();

    /**
     * ItemStackの個数を指定します.
     *
     * @param number ItemStackの個数
     * @return このBuilder
     */
    @Nonnull
    ItemStackBuilder number(int number);

    /**
     * Builderによって指定された各引数を用いてインスタンスを生成します
     *
     * @return 生成されたインスタンス. ({@code null} は許容されません)
     */
    @Nonnull
    ItemStack build(@Nonnull PlayerData playerData);
}
