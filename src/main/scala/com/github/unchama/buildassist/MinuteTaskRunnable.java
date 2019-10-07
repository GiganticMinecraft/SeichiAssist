/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.github.unchama.buildassist;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.exp.IExperienceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import scala.collection.mutable.HashMap;
import scala.runtime.BoxedUnit;

import java.math.BigDecimal;
import java.util.UUID;

public class MinuteTaskRunnable extends BukkitRunnable {
    private final HashMap<UUID, PlayerData> playermap = BuildAssist.playermap();

    @Override
    public void run() {
        this.playermap.values().foreach(playerdata -> {
            if (!playerdata.isOffline()) {
                final Player player = Bukkit.getServer().getPlayer(
                        playerdata.uuid);
                //SeichiAssistのデータを取得
                final UUID uuid = player.getUniqueId();
                final com.github.unchama.seichiassist.data.player.PlayerData playerdata_s = SeichiAssist.playermap().get(uuid).get();
                //経験値変更用のクラスを設定
                final IExperienceManager expman = new ExperienceManager(player);

                final int minus = -BuildAssist.config().getFlyExp();

                //1分間の建築量を加算する

                if (playerdata.build_num_1min.doubleValue() > BuildAssist.config().getBuildNum1minLimit()) {
                    playerdata.totalbuildnum = playerdata.totalbuildnum.add(new BigDecimal(BuildAssist.config().getBuildNum1minLimit()));
                } else {
                    playerdata.totalbuildnum = playerdata.totalbuildnum.add(playerdata.build_num_1min);
                }
                playerdata.build_num_1min = BigDecimal.ZERO;
                playerdata.updateLevel(player);
                playerdata.buildsave(player);

                if (playerdata.endlessfly) {
                    if (playerdata_s.idleMinute() >= 10) {
                        player.setAllowFlight(true);
                        player.sendMessage(ChatColor.GRAY + "放置時間中のFLYは無期限で継続中です(経験値は消費しません)");
                    } else if (!expman.hasExp(BuildAssist.config().getFlyExp())) {
                        player.sendMessage(ChatColor.RED
                                + "Fly効果の発動に必要な経験値が不足しているため、");
                        player.sendMessage(ChatColor.RED + "Fly効果を終了しました");
                        playerdata.flytime = 0;
                        playerdata.flyflag = false;
                        playerdata.endlessfly = false;
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    } else {
                        player.setAllowFlight(true);
                        player.sendMessage(ChatColor.GREEN + "Fly効果は無期限で継続中です");
                        expman.changeExp(minus);
                    }
                } else if (playerdata.flyflag) {
                    final int flytime = playerdata.flytime;
                    if (playerdata_s.idleMinute() >= 10) {
                        player.setAllowFlight(true);
                        player.sendMessage(ChatColor.GRAY + "放置時間中のFLYは無期限で継続中です(経験値は消費しません)");
                    } else if (flytime <= 0) {
                        player.sendMessage(ChatColor.GREEN + "Fly効果が終了しました");
                        playerdata.flyflag = false;
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    } else if (!expman.hasExp(BuildAssist.config().getFlyExp())) {
                        player.sendMessage(ChatColor.RED
                                + "Fly効果の発動に必要な経験値が不足しているため、");
                        player.sendMessage(ChatColor.RED + "Fly効果を終了しました");
                        playerdata.flytime = 0;
                        playerdata.flyflag = false;
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    } else {
                        player.setAllowFlight(true);
                        player.sendMessage(ChatColor.GREEN + "Fly効果はあと"
                                + flytime + "分です");
                        playerdata.flytime -= 1;
                        expman.changeExp(minus);
                    }
                }
            }

            return BoxedUnit.UNIT;
        });
    }
}
