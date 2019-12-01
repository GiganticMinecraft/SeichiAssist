package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import scala.collection.mutable.HashMap;

import java.util.UUID;

public class CoolDownTask extends BukkitRunnable {
    private static final String VOTE = "VOTE";
    private static final String SOUND = "SOUND";
    private static final String GACHA = "GACHA";
    public static final String SHAREINV = "SHAREINV";
    private HashMap<UUID, PlayerData> playermap = SeichiAssist.playermap();
    private UUID uuid;
    private PlayerData playerdata;
    private boolean voteflag = false;
    private boolean soundflag = false;
    private boolean gachaflag = false;
    private boolean shareinvflag = false;
    private Player player;

    //newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
    public CoolDownTask(Player player, boolean voteflag, boolean soundflag, boolean gachaflag) {
        this.player = player;
        this.voteflag = voteflag;
        this.soundflag = soundflag;
        this.gachaflag = gachaflag;
        //UUIDを取得
        uuid = player.getUniqueId();
        //playerdataを取得
        playerdata = playermap.apply(uuid);
        if (voteflag) {
            playerdata.votecooldownflag_$eq(false);
        } else if (gachaflag) {
            playerdata.gachacooldownflag_$eq(false);
        } else {
            playerdata.activeskilldata().skillcanbreakflag = false;
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
            case SOUND:
                soundflag = true;
                playerdata.activeskilldata().skillcanbreakflag = false;
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
                // ベースに合わせて念のためdefaultはsoundに合わせておく
                soundflag = true;
                playerdata.activeskilldata().skillcanbreakflag = false;
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
        } else {
            playerdata.activeskilldata().skillcanbreakflag = true;
            if (soundflag) {
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 0.1f);
            }
        }
        //デバッグ用
        if (SeichiAssist.DEBUG()) {
            player.sendMessage("クールダウンタイム終了");
        }
    }

}
