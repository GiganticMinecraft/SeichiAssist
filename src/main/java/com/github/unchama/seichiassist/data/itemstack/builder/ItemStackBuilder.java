package com.github.unchama.seichiassist.data.itemstack.builder;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.component.AbstractItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * ItemStack,特にメニューに使用するスロットのIconを生成するBuilderです.
 * <p>
 * Created by karayuu on 2019/03/30
 */
public class ItemStackBuilder extends AbstractItemStackBuilder<ItemStackBuilder> {
    private Boolean showAttribute = false;

    private ItemStackBuilder(@NotNull Material material) {
        super(material);
    }

    private ItemStackBuilder(@NotNull Material material, short durability) {
        super(material, durability);
    }

    /**
     * Iconを生成するBuilderを生成します.
     *
     * @param material ItemStackに設定するMaterial ({@code null} は許容されません)
     */
    @NotNull
    public static ItemStackBuilder of(@NotNull Material material) {
        return new ItemStackBuilder(material);
    }

    /**
     * Iconを生成するBuilderを生成します.
     *
     * @param material ItemStackに設定するMaterial ({@code null} は許容されません)
     * @param durability ダメージ値
     */
    @NotNull
    public static ItemStackBuilder of(@NotNull Material material, short durability) {
        return new ItemStackBuilder(material, durability);
    }

    /**
     * ItemStack(ItemStackBuilder)の各種情報を表示させます.(シャベルの採掘速度等)
     *
     * @return このBuilder
     */
    @NotNull
    public ItemStackBuilder showAttribute() {
        this.showAttribute = true;
        return this;
    }

    @Override
    @NotNull
    public ItemStack build(@NotNull PlayerData playerData) {
        ItemStack itemStack = super.component.getItemStack();
        ItemMeta meta = super.component.getItemMeta(playerData);

        if (!showAttribute) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
