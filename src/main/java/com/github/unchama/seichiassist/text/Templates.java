package com.github.unchama.seichiassist.text;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.GREEN;

/**
 * Created by karayuu on 2019/05/03
 */
public final class Templates {
    private Templates() {

    }

    public static List<String> playerInfoDescrpition = Arrays.asList(
        DARK_GRAY + "※1分毎に更新",
        GREEN + "統計データは",
        GREEN + "各サバイバルサーバー間で",
        GREEN + "共有されます"
    );
}
