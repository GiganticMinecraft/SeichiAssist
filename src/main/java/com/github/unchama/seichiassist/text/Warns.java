package com.github.unchama.seichiassist.text;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;

/**
 * Created by karayuu on 2019/04/30
 */
public class Warns {
    /**
     * 整地ワールドの建築量・ガチャ券警告.
     */
    public static List<Text> seichiWorldWarning = Arrays.asList(
        Text.of("整地ワールド以外では", ChatColor.RESET , ChatColor.RED),
        Text.of("整地量とガチャ券は増えません", ChatColor.RESET, ChatColor.RED)
    );
}
