package com.github.unchama.seichiassist.util;

import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * デバッグ出力用クラス
 * すべてのメソッドがデバッグモードONの際に有効化します。
 * 2017/8/29
 *
 * @author karayuu
 */
public class DebugOutput {

    public static void outputAsConsoleMsg(String msg) {
        if (SeichiAssist.DEBUG) {
            Bukkit.getServer().getLogger().info(ChatColor.BLUE + "[DEBUG]" + ChatColor.RESET + msg);
        }
    }

    public static void outputAsPlayerMsg(Player player, String msg) {
        if (SeichiAssist.DEBUG) {
            player.sendMessage(ChatColor.BLUE + "[DEBUG]" + ChatColor.RESET + msg);
        }
    }
}
