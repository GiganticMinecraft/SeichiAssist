package com.github.unchama.seichiassist.commands;

import com.github.unchama.seichiassist.*;
import com.github.unchama.seichiassist.task.*;
import com.github.unchama.seichiassist.util.*;
import org.bukkit.*;
import org.bukkit.command.*;

import java.util.*;

/**
 * Created by karayuu on 2018/07/25
 */
public class GiganticFeverCommand implements TabExecutor {
    private Config config = SeichiAssist.config;
    private static int end = 0;
    private static boolean isInTime = false;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int now = MinuteTaskRunnable.time;
        end = now + config.getGiganticFeverMinutes();
        isInTime = true;

        Util.sendEveryMessage(ChatColor.AQUA + "フィーバー！この時間MOBたちは踊りに出かけてるぞ！今が整地時だ！");
        Util.sendEveryMessage(ChatColor.AQUA + "(" + config.getGiganticFeverDisplayTime() + "間)");

        Util.setDifficulty(SeichiAssist.seichiWorldList, Difficulty.PEACEFUL);

        return true;
    }

    public static void checkTime() {
        if (!isInTime) {
            return;
        }
        if (MinuteTaskRunnable.time == end) {
            Util.setDifficulty(SeichiAssist.seichiWorldList, Difficulty.HARD);
            Util.sendEveryMessage(ChatColor.AQUA + "フィーバー終了！MOBたちは戻ってきたぞ！");
            isInTime = false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
