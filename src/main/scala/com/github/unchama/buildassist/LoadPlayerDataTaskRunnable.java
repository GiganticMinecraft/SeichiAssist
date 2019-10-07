package com.github.unchama.buildassist;


import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import scala.collection.mutable.HashMap;

import java.util.UUID;


public class LoadPlayerDataTaskRunnable extends BukkitRunnable {
    private final HashMap<UUID, PlayerData> playermap = BuildAssist.playermap();
    private final Player p;
    private final UUID uuid;
    private int retryCount;

    public LoadPlayerDataTaskRunnable(final Player p) {
        this.p = p;
        uuid = this.p.getUniqueId();
        retryCount = 0;
    }

    @Override
    public void run() {
        // 接続時にスケジュールで実行する処理

        //対象プレイヤーがオフラインなら処理終了
        if (Bukkit.getServer().getPlayer(uuid) == null) {
            cancel();
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + p.getName() + "はオフラインの為取得処理を中断");
            return;
        }
        //読み込み失敗が規定回数超えたら終わる
        if (retryCount >= 7) {
            cancel();
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + p.getName() + "の建築系データが読み込めませんでした");
            p.sendMessage(ChatColor.RED + "建築系データが読み込めませんでした。再接続をお願いします。改善されない場合はお手数ですがお問い合わせください");
            p.kickPlayer(ChatColor.RED + "建築系データが読み込めませんでした。再接続をお願いします。改善されない場合はお手数ですがお問い合わせください");
            return;
        }
        //DBから読み込み終わるまで待つ
        final com.github.unchama.seichiassist.data.player.PlayerData playerdata_s = SeichiAssist.playermap().getOrElse(uuid, () -> null);
        if (playerdata_s == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + p.getName() + "の建築系データ取得待機…(" + (retryCount + 1) + "回目)");
            retryCount++;
            return;
        }
        cancel();
        final PlayerData playerdata;
        if (!playermap.isDefinedAt(uuid)) {
            //リストにplayerdataが無い場合は新規作成
            playerdata = new PlayerData(p);
            //PlayerDataをリストに登録
            playermap.put(uuid, playerdata);
        } else {
            //リストにある場合はそれを読み込む
            playerdata = playermap.get(uuid).get();
        }
        //建築系データ読み込み
        playerdata.buildload(p);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "建築系データ読み込み完了");
    }
}
