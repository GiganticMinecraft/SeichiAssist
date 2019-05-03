package com.github.unchama.seichiassist.data.subhome;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * サブホームオブジェクトのクラス
 */
public class SubHome {
    private final @NotNull Location location;
    public final @Nullable String name;

    public SubHome(@NotNull Location location, @Nullable String name) {
        this.location = location;
        this.name = name;
    }

    public Location getLocation() {
        // BukkitのLocationはミュータブルなのでコピーして返す必要がある
        return location.clone();
    }
}
