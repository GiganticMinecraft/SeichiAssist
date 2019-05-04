package com.github.unchama.seichiassist.data.itemstack.builder;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.component.AbstractItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * ItemStack,特にメニューに使用するスロットのIconを生成するBuilderです.
 * <p>
 * Created by karayuu on 2019/03/30
 */
public class ItemStackBuilder extends AbstractItemStackBuilder<ItemStackBuilder> {
    private Boolean showAttribute = false;

    private ItemStackBuilder(@Nonnull Material material) {
        super(material);
    }

    private ItemStackBuilder(@Nonnull Material material, short durability) {
        super(material, durability);
    }

    /**
     * Iconを生成するBuilderを生成します.
     *
     * @param material ItemStackに設定するMaterial ({@code null} は許容されません)
     */
    @Nonnull
    public static ItemStackBuilder of(@Nonnull Material material) {
        requireNonNull(material);
        return new ItemStackBuilder(material);
    }

    /**
     * Iconを生成するBuilderを生成します.
     *
     * @param material ItemStackに設定するMaterial ({@code null} は許容されません)
     * @param durability ダメージ値
     */
    @Nonnull
    public static ItemStackBuilder of(@Nonnull Material material, short durability) {
        requireNonNull(material);
        return new ItemStackBuilder(material, durability);
    }

    /**
     * ItemStack(ItemStackBuilder)の各種情報を表示させます.(シャベルの採掘速度等)
     *
     * @return このBuilder
     */
    @Nonnull
    public ItemStackBuilder showAttribute() {
        this.showAttribute = true;
        return this;
    }

    @Override
    @Nonnull
    public ItemStack build(@Nonnull PlayerData playerData) {
        requireNonNull(playerData);

        ItemStack itemStack = super.component.getItemStack();
        ItemMeta meta = super.component.getItemMeta(playerData);

        if (!showAttribute) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
