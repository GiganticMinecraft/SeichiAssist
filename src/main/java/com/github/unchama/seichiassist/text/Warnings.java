package com.github.unchama.seichiassist.text;

import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by karayuu on 2019/04/30
 */
public final class Warnings {
    private Warnings() {}
    /**
     * 整地ワールドの建築量・ガチャ券の警告.
     * @param player 判断したい {@link Player} ({@code null} は許容されません.
     */
    @NotNull
    public static List<Text> seichiWorldWarning(@NotNull Player player) {
        if (Util.isSeichiWorld(player)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(
                Text.of("整地ワールド以外では", ChatColor.RED),
                Text.of("整地量とガチャ券は増えません", ChatColor.RED)
            );
        }
    }
}
