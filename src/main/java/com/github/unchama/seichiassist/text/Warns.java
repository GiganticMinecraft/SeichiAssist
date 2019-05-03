package com.github.unchama.seichiassist.text;

import com.github.unchama.seichiassist.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Created by karayuu on 2019/04/30
 */
public class Warns {
    /**
     * 整地ワールドの建築量・ガチャ券警告.
     * @param player 判断したい {@link Player} ({@code null} は許容されません.
     */
    public static List<Text> seichiWorldWarning(@Nonnull Player player) {
        requireNonNull(player);
        if (!Util.isSeichiWorld(player)) {
            return Arrays.asList(
                Text.of("整地ワールド以外では", ChatColor.RESET , ChatColor.RED),
                Text.of("整地量とガチャ券は増えません", ChatColor.RESET, ChatColor.RED)
            );
        } else {
            return null;
        }
    }
}
