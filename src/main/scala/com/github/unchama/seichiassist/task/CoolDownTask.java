package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import scala.collection.mutable.HashMap;

import java.util.UUID;

public class CoolDownTask extends BukkitRunnable {
    private static final String VOTE = "VOTE";
    private static final String GACHA = "GACHA";
    private final HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private final UUID uuid;
    private final PlayerData playerdata;
    private boolean voteflag = false;
    private boolean gachaflag = false;
    private final Player player;

    //newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
    public CoolDownTask(Player player, boolean voteflag, boolean gachaflag) {
        this.player = player;
        this.voteflag = voteflag;
        this.gachaflag = gachaflag;
        //UUIDを取得
        uuid = player.getUniqueId();
        //playerdataを取得
        playerdata = playermap.apply(uuid);
        if (voteflag) {
            playerdata.votecooldownflag_$eq(false);
        } else if (gachaflag) {
            playerdata.gachacooldownflag_$eq(false);
        }
    }

    @Override
    public void run() {
        if (voteflag) {
            playerdata.votecooldownflag_$eq(true);
        } else if (gachaflag) {
            playerdata.gachacooldownflag_$eq(true);
        }
        //デバッグ用
        if (SeichiAssist.DEBUG()) {
            player.sendMessage("クールダウンタイム終了");
        }
    }

}
