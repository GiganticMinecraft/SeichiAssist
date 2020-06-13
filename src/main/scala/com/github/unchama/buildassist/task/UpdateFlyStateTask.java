package com.github.unchama.buildassist.task;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.util.ExperienceManager;
import com.github.unchama.buildassist.data.PlayerData;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.util.exp.IExperienceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import scala.collection.mutable.HashMap;
import scala.runtime.BoxedUnit;

import java.util.UUID;

public class UpdateFlyStateTask extends BukkitRunnable {
    private final HashMap<UUID, PlayerData> playermap = BuildAssist.playermap();

    @Override
    public void run() {
        this.playermap.values()
                .filterNot(PlayerData::isOffline)
                .foreach((PlayerData playerdata) -> {
                    final Player player = Bukkit.getServer().getPlayer(
                            playerdata.uuid);
                    final UUID uuid = player.getUniqueId();

                    //経験値変更用のクラスを設定
                    final IExperienceManager expman = new ExperienceManager(player);
                    final int needs = BuildAssist.config().getFlyExp();
                    final int decrease = -needs;
                    final com.github.unchama.seichiassist.data.player.PlayerData playerdata_s = SeichiAssist.playermap().get(uuid).get();
                    final boolean isAFK = playerdata_s.idleMinute() >= 10;
                    final boolean has = expman.hasExp(needs);
                    if (isAFK) {
                        player.setAllowFlight(true);
                        player.sendMessage(ChatColor.GRAY + "放置時間中のflyは無期限で継続中です(経験値は消費しません)");
                    } else if (!has) {
                        player.sendMessage(ChatColor.RED
                                + "fly効果の発動に必要な経験値が不足しているため、");
                        player.sendMessage(ChatColor.RED + "fly効果を終了しました");
                        playerdata.flyMinute = 0;
                        playerdata.isFlying = false;
                        playerdata.doesEndlessFly = false;
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    } else {
                        if (playerdata.doesEndlessFly) {
                            player.setAllowFlight(true);
                            player.sendMessage(ChatColor.GREEN + "fly効果は無期限で継続中です");
                            expman.changeExp(decrease);
                        } else if (playerdata.isFlying) {
                            final int minute = playerdata.flyMinute;
                            if (minute <= 0) {
                                player.sendMessage(ChatColor.GREEN + "fly効果が終了しました");
                                playerdata.isFlying = false;
                                player.setAllowFlight(false);
                                player.setFlying(false);
                            } else {
                                player.setAllowFlight(true);
                                player.sendMessage(ChatColor.GREEN + "fly効果はあと"
                                        + minute + "分です");
                                playerdata.flyMinute--;
                                expman.changeExp(decrease);
                            }
                        }
                    }

                    return BoxedUnit.UNIT;
        });
    }
}
