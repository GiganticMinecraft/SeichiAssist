package com.github.unchama.seichiassist.data.subhome;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * サブホームオブジェクトのクラス
 */
// TODO Scalaize and make this case class
public class SubHome {
    public final @Nullable String name;
    private final @NotNull Location location;

    public SubHome(@NotNull Location location, @Nullable String name) {
        this.location = location;
        this.name = name;
    }

    public Location getLocation() {
        // BukkitのLocationはミュータブルなのでコピーして返す必要がある
        return location.clone();
    }
}
