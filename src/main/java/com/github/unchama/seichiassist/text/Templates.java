package com.github.unchama.seichiassist.text;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;

/**
 * Created by karayuu on 2019/05/03
 */
public class Templates {
    public static List<Text> playerInfoDescrpition = Arrays.asList(
        Text.of("※1分毎に更新", ChatColor.DARK_GRAY),
        Text.of("統計データは", ChatColor.GREEN),
        Text.of("各サバイバルサーバー間で", ChatColor.GREEN),
        Text.of("共有されます", ChatColor.GREEN)
    );
}
