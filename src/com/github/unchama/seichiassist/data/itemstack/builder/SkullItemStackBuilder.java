package com.github.unchama.seichiassist.data.itemstack.builder;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.component.AbstractItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * Created by karayuu on 2019/04/09
 */
public class SkullItemStackBuilder extends AbstractItemStackBuilder<SkullItemStackBuilder> {
    @Nonnull
    private String ownerName = "";

    private SkullItemStackBuilder() {
        super(Material.SKULL_ITEM);
    }

    public SkullItemStackBuilder of() {
        return new SkullItemStackBuilder();
    }

    @Nonnull
    public SkullItemStackBuilder owner(@Nonnull String ownerName) {
        requireNonNull(ownerName);
        this.ownerName = ownerName;
        return this;
    }

    @Nonnull
    @Override
    public ItemStack build(@Nonnull PlayerData playerData) {
        requireNonNull(playerData);

        ItemStack skull = component.getItemStack();
        SkullMeta meta = (SkullMeta) component.getItemMeta(playerData);

        meta.setOwner(ownerName);
        skull.setItemMeta(meta);

        return skull;
    }
}
