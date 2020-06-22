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
    public static final String SHAREINV = "SHAREINV";
    private final HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private final UUID uuid;
    private final PlayerData playerdata;
    private boolean voteflag = false;
    private boolean gachaflag = false;
    private boolean shareinvflag = false;
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

    // 拡張版
    public CoolDownTask(Player player, String tag) {
        this.player = player;
        //UUIDを取得
        uuid = player.getUniqueId();
        //playerdataを取得
        playerdata = playermap.apply(uuid);
        switch (tag) {
            case VOTE:
                voteflag = true;
                playerdata.votecooldownflag_$eq(false);
                break;
            case GACHA:
                gachaflag = true;
                playerdata.gachacooldownflag_$eq(false);
                break;
            case SHAREINV:
                shareinvflag = true;
                playerdata.samepageflag_$eq(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        if (voteflag) {
            playerdata.votecooldownflag_$eq(true);
        } else if (gachaflag) {
            playerdata.gachacooldownflag_$eq(true);
        } else if (shareinvflag) {
            playerdata.shareinvcooldownflag_$eq(true);
        }
        //デバッグ用
        if (SeichiAssist.DEBUG()) {
            player.sendMessage("クールダウンタイム終了");
        }
    }

}
