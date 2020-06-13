package com.github.unchama.buildassist.data;

import com.github.unchama.buildassist.BuildAssist;
import com.github.unchama.buildassist.util.Util;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.BuildCount;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.unchama.buildassist.BuildBlock.*;

public final class PlayerData {
    public final String name;
    public final UUID uuid;
    public int level;
    /**
     * トータル設置ブロック数
     */
    public BigDecimal totalBuildCount;

    // 飛行関連
    public boolean isFlying;
    public int flyingTime;
    public boolean doesEndlessFly;

    // 一括設置関連
    public boolean isEnabledBulkBlockPlace;
    public boolean fillSurface;
    // FIXME: 殺れ！！！！！！
    public int actualRangeIndex;
    /**
     * ブロックを並べるスキル設定フラグ
     * 0: OFF
     * 1: 上
     * 2: 下
     */
    public int lineFillFlag;
    public int lineUpStepFlag;
    public int breakLightBlockFlag;
    public int preferMineStackI;
    /**
     * ブロック範囲設置スキル設定フラグ
     */
    public boolean preferMineStackZ;
    /**
     * 1分のブロック設置数
     */
    public BigDecimal buildCountBuffer;

    //プレイヤーデータクラスのコンストラクタ
    public PlayerData(final Player player) {
        //初期値を設定
        name = Util.getName(player);
        uuid = player.getUniqueId();
        totalBuildCount = BigDecimal.ZERO;
        level = 1;
        isFlying = false;
        flyingTime = 0;
        doesEndlessFly = false;
        isEnabledBulkBlockPlace = false;
        fillSurface = true;
        actualRangeIndex = 2;

        lineFillFlag = 0;
        lineUpStepFlag = 0;
        breakLightBlockFlag = 0;
        preferMineStackI = 0;

        preferMineStackZ = false;

        buildCountBuffer = BigDecimal.ZERO;

    }

    /**
     * レベルを更新
     */
    public void updateLevel(final Player player) {
        calcPlayerLevel(player);
    }

    /**
     * プレイヤーレベルを計算し、更新する。
     */
    private void calcPlayerLevel(final Player player) {
        //現在のランクの次を取得
        int i = level;
        //ランクが上がらなくなるまで処理
        while (((int) BuildAssist.levellist().apply(i)) <= totalBuildCount.doubleValue() && (i + 2) <= BuildAssist.levellist().size()) {
            if (!BuildAssist.DEBUG()) {
                //レベルアップ時のメッセージ
                player.sendMessage(ChatColor.GOLD + "ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(" + i + ")→建築Lv(" + (i + 1) + ")】");
            }
            i++;
            if ((i + 1) == BuildAssist.levellist().size()) {
                player.sendMessage(ChatColor.GOLD + "最大Lvに到達したよ(`･ω･´)");
            }
        }
        level = i;
    }

    /**
     * オフラインかどうか
     */
    public boolean isOffline() {
        return Bukkit.getServer().getPlayer(uuid) == null;
    }

    /**
     * 建築系データを読み込む
     *
     * @param player
     * @return true:読み込み成功　false:読み込み失敗
     */
    public boolean load(final Player player) {
        final com.github.unchama.seichiassist.data.player.PlayerData playerdata_s = SeichiAssist.playermap().getOrElse(uuid, () -> null);
        if (playerdata_s == null) {
            return false;
        }
        final int server_num = SeichiAssist.seichiAssistConfig().getServerNum();

        final BuildCount oldBuildCount = playerdata_s.buildCount();

        totalBuildCount = playerdata_s.buildCount().count();
        //ブロック設置カウントが統合されてない場合は統合する
        if (server_num >= 1 && server_num <= 3) {
            byte f = playerdata_s.buildCount().migrationFlag();
            if ((f & (0x01 << server_num)) == 0) {
                if (f == 0) {
                    // 初回は加算じゃなくベースとして代入にする
                    totalBuildCount = calcBuildBlock(player);
                } else {
                    totalBuildCount = totalBuildCount.add(calcBuildBlock(player));
                }
                f = (byte) (f | (0x01 << server_num));
                final BuildCount updatedBuildCount = playerdata_s.buildCount().copy(oldBuildCount.lv(), totalBuildCount, f);
                playerdata_s.buildCount_$eq(updatedBuildCount);

                player.sendMessage(ChatColor.GREEN + "サーバー" + server_num + "の建築データを統合しました");
                if (f == 0x0E) {
                    player.sendMessage(ChatColor.GREEN + "全サーバーの建築データを統合しました");
                }
            }
        }
        level = playerdata_s.buildCount().lv();
        updateLevel(player);
        return true;
    }

    /**
     * 建築系データを保存
     */
    public void save(final Player player) {
        final com.github.unchama.seichiassist.data.player.PlayerData playerData = SeichiAssist.playermap().getOrElse(uuid, () -> null);
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "建築系データ保存失敗しました");
            return;
        }

        final BuildCount oldBuildCount = playerData.buildCount();

        //1分制限の判断
        final BigDecimal newBuildCount;
        if (buildCountBuffer.doubleValue() <= BuildAssist.config().getBuildNum1minLimit()) {
            newBuildCount = totalBuildCount.add(buildCountBuffer);
        } else {
            newBuildCount = totalBuildCount.add(new BigDecimal(BuildAssist.config().getBuildNum1minLimit()));
        }

        playerData.buildCount_$eq(new BuildCount(level, newBuildCount, oldBuildCount.migrationFlag()));
    }

}
