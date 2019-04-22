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
public class SlotItemStackBuilder extends AbstractItemStackBuilder<SlotItemStackBuilder> {
    private Boolean showAttribute = false;

    private SlotItemStackBuilder(@Nonnull Material material) {
        super(material);
    }

    /**
     * Iconを生成するBuilderを生成します.
     *
     * @param material ItemStackに設定するMaterial ({@code null} は許容されません)
     */
    @Nonnull
    public static SlotItemStackBuilder of(@Nonnull Material material) {
        requireNonNull(material);
        return new SlotItemStackBuilder(material);
    }

    /**
     * ItemStack(ItemStackBuilder)の各種情報を表示させます.(シャベルの採掘速度等)
     *
     * @return このBuilder
     */
    @Nonnull
    public SlotItemStackBuilder showAttribute() {
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
