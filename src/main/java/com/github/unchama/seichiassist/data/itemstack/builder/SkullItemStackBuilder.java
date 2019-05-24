package com.github.unchama.seichiassist.data.itemstack.builder;

import com.github.unchama.seichiassist.data.itemstack.builder.component.AbstractItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Created by karayuu on 2019/04/09
 */
public class SkullItemStackBuilder extends AbstractItemStackBuilder<SkullItemStackBuilder> {
    @Nullable
    private UUID ownerUUID;

    private SkullItemStackBuilder() {
        super(Material.SKULL_ITEM, (short) 3);
    }

    @NotNull
    public static SkullItemStackBuilder of() {
        return new SkullItemStackBuilder();
    }

    /**
     * {@link Material#SKULL_ITEM} に表示するskullのownerを設定します.
     *
     * @param ownerUUID ownerの {@link UUID} ({@code null} は許容されません.)
     * @return {@link SkullItemStackBuilder}
     */
    @NotNull
    public SkullItemStackBuilder owner(@NotNull UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        return this;
    }

    @NotNull
    @Override
    public ItemStack build() {
        ItemStack skull = component.getItemStack();
        SkullMeta meta = (SkullMeta) component.getItemMeta();

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
        meta.setOwningPlayer(offlinePlayer);
        skull.setItemMeta(meta);

        return skull;
    }
}
