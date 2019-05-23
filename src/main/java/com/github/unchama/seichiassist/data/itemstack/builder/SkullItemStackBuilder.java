package com.github.unchama.seichiassist.data.itemstack.builder;

import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.data.itemstack.builder.component.AbstractItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Created by karayuu on 2019/04/09
 */
public class SkullItemStackBuilder extends AbstractItemStackBuilder<SkullItemStackBuilder> {
    @NotNull
    private String ownerName = "";

    private boolean isPlayerSkull = false;

    private SkullItemStackBuilder() {
        super(Material.SKULL_ITEM, (short) 3);
    }

    @NotNull
    public static SkullItemStackBuilder of() {
        return new SkullItemStackBuilder();
    }

    /**
     * {@link Material#SKULL_ITEM} に表示するskullのownerを設定します.
     * {@link #playerSkull()} と同時に指定された場合は, {@link #playerSkull()} が優先されます.
     *
     * @param ownerName ownerの名前 ({@code null} は許容されません.)
     * @return {@link SkullItemStackBuilder}
     * @see #playerSkull()
     */
    @NotNull
    public SkullItemStackBuilder owner(@NotNull String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    /**
     * Menu を開いた {@link Player} の頭にします.
     * {@link #owner(String)} と同時に指定された場合は,こちらが優先されます.
     *
     * @return {@link SkullItemStackBuilder}
     * @see #owner(String)
     */
    @NotNull
    public SkullItemStackBuilder playerSkull() {
        this.isPlayerSkull = true;
        return this;
    }

    @NotNull
    @Override
    public ItemStack build(@NotNull PlayerData playerData) {
        ItemStack skull = component.getItemStack();
        SkullMeta meta = (SkullMeta) component.getItemMeta(playerData);

        if (isPlayerSkull) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerData.uuid);
            meta.setOwningPlayer(offlinePlayer);
        } else {
            meta.setOwner(ownerName);
        }
        skull.setItemMeta(meta);

        return skull;
    }
}
