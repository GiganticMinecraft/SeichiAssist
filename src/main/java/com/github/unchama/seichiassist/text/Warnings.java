package com.github.unchama.seichiassist.text;

import com.github.unchama.seichiassist.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bukkit.ChatColor.RED;

/**
 * Created by karayuu on 2019/04/30
 */
public final class Warnings {
    private Warnings() {
    }

    /**
     * 整地ワールド以外では建築量・ガチャ券が増加しないという警告.
     *
     * @param player 判断したい {@link Player} ({@code null} は許容されません.
     */
    @NotNull
    public static List<String> noRewardsOutsideSeichiWorld(@NotNull Player player) {
        if (Util.INSTANCE.isSeichiWorld(player)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(
                RED + "整地ワールド以外では",
                RED + "整地量とガチャ券は増えません"
            );
        }
    }
}
