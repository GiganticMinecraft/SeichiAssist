package com.github.unchama.seichiassist.text;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;

/**
 * Created by karayuu on 2019/05/03
 */
public class Templates {
    /**
     * プレイヤー情報の一般説明文
     */
    public static List<Text> playerInfoDescrpition = Arrays.asList(
        Text.of("※1分毎に更新", ChatColor.DARK_GRAY),
        Text.of("統計データは", ChatColor.GREEN),
        Text.of("各サバイバルサーバー間で", ChatColor.GREEN),
        Text.of("共有されます", ChatColor.GREEN)
    );

    /**
     * 採掘速度上昇の一般説明文
     */
    public static List<Text> miningSpeedDescription = Arrays.asList(
        Text.of("採掘速度上昇効果とは", ChatColor.GRAY),
        Text.of("接続人数と1分間の採掘速度に応じて", ChatColor.GRAY),
        Text.of("採掘速度が変化するシステムです", ChatColor.GRAY)
    );
}
