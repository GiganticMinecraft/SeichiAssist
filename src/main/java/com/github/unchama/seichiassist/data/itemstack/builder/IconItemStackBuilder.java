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
public class IconItemStackBuilder extends AbstractItemStackBuilder<IconItemStackBuilder> {
    /**
     * {@code null} オブジェクトである {@link Material#AIR} がセットされた {@link IconItemStackBuilder}
     */
    public static final IconItemStackBuilder EMPTY_BUILDER = IconItemStackBuilder.of();

    private Boolean showAttribute = false;

    private IconItemStackBuilder() {
        super(Material.AIR);
    }

    private IconItemStackBuilder(@NotNull Material material) {
        super(material);
    }

    private IconItemStackBuilder(@NotNull Material material, short durability) {
        super(material, durability);
    }

    /**
     * {@link Material#AIR} がセットされたBuilderを生成します.
     */
    @NotNull
    public static IconItemStackBuilder of() {
        return new IconItemStackBuilder();
    }

    /**
     * Iconを生成するBuilderを生成します.
     *
     * @param material ItemStackに設定するMaterial ({@code null} は許容されません)
     */
    @NotNull
    public static IconItemStackBuilder of(@NotNull Material material) {
        return new IconItemStackBuilder(material);
    }

    /**
     * Iconを生成するBuilderを生成します.
     *
     * @param material ItemStackに設定するMaterial ({@code null} は許容されません)
     * @param durability ダメージ値
     */
    @NotNull
    public static IconItemStackBuilder of(@NotNull Material material, short durability) {
        return new IconItemStackBuilder(material, durability);
    }

    /**
     * ItemStack(IconItemStackBuilder)の各種情報を表示させます.(シャベルの採掘速度等)
     *
     * @return このBuilder
     */
    @NotNull
    public IconItemStackBuilder showAttribute() {
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
