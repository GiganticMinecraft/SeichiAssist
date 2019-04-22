package com.github.unchama.seichiassist.data.itemstack.builder;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.component.AbstractItemStackBuilder;
import com.github.unchama.seichiassist.data.menu.Menu;
import com.github.unchama.seichiassist.data.slot.Slot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

    @Nonnull
    private boolean isPlayerSkull = false;

    private SkullItemStackBuilder() {
        super(Material.SKULL_ITEM);
    }

    public static SkullItemStackBuilder of() {
        return new SkullItemStackBuilder();
    }

    /**
     * {@link Material#SKULL_ITEM} に表示するskullのownerを設定します.
     * {@link #setPlayerSkull()} と同時に指定された場合は, {@link #setPlayerSkull()} が優先されます.
     *
     * @param ownerName ownerの名前 ({@code null} は許容されません.)
     * @return {@link SkullItemStackBuilder}
     * @see #setPlayerSkull()
     */
    @Nonnull
    public SkullItemStackBuilder owner(@Nonnull String ownerName) {
        requireNonNull(ownerName);
        this.ownerName = ownerName;
        return this;
    }

    /**
     * {@link Menu} を開いた {@link Player} の頭にします.
     * {@link #owner(String)} と同時に指定された場合は,こちらが優先されます.
     *
     * @return {@link SkullItemStackBuilder}
     * @see #owner(String)
     */
    @Nonnull
    public SkullItemStackBuilder setPlayerSkull() {
        this.isPlayerSkull = true;
        return this;
    }

    @Nonnull
    @Override
    public ItemStack build(@Nonnull PlayerData playerData) {
        requireNonNull(playerData);

        ItemStack skull = component.getItemStack();
        SkullMeta meta = (SkullMeta) component.getItemMeta(playerData);

        if (isPlayerSkull) {
            meta.setOwner(playerData.name);
        } else {
            meta.setOwner(ownerName);
        }
        skull.setItemMeta(meta);

        return skull;
    }
}
