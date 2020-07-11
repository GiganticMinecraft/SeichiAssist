package com.github.unchama.buildassist;

import com.github.unchama.seichiassist.PackagePrivate;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.BuildCount;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public final class PlayerData {
    public final String name;
    public final UUID uuid;
    public int level;
    /**
     * トータル設置ブロック数
     */
    public BigDecimal totalbuildnum;

    public boolean flyflag;

    public int flytime;

    public boolean endlessfly;

    public boolean ZoneSetSkillFlag;

    public boolean zsSkillDirtFlag;

    // TODO: こいつは殺す
    public int AREAint;
    /**
     * ブロックを並べるスキル設定フラグ
     */
    public int line_up_flg;
    public int line_up_step_flg;
    public int line_up_des_flg;
    public int line_up_minestack_flg;
    /**
     * ブロック範囲設置スキル設定フラグ
     */
    public boolean zs_minestack_flag;
    /**
     * 1分のブロック設置数
     */
    @PackagePrivate
    BigDecimal build_num_1min;

    //プレイヤーデータクラスのコンストラクタ
    public PlayerData(final Player player) {
        //初期値を設定
        name = Util.getName(player);
        uuid = player.getUniqueId();
        totalbuildnum = BigDecimal.ZERO;
        level = 1;
        flyflag = false;
        flytime = 0;
        endlessfly = false;
        ZoneSetSkillFlag = false;
        zsSkillDirtFlag = true;
        AREAint = 2;

        line_up_flg = 0;
        line_up_step_flg = 0;
        line_up_des_flg = 0;
        line_up_minestack_flg = 0;

        zs_minestack_flag = false;

        build_num_1min = BigDecimal.ZERO;

    }

    /**
     * プレイヤーレベルを計算し、更新する。
     */
    void updateLevel(final Player player) {
        // NOTE:
        // levellist[i] に入っているのは、level iからlevel i+1になるための必要totalbuildnum。
        // levelは1から始まるので、当然levellist[0]にはアクセスされない。
        // よって、BuildAssist.levellist().size()は最大レベルと同値となる。
        // はずだった。
        // 100->101を実現する筈のlevellist[100]は存在するが、
        // その遷移は元のコードでは境界値により封じられている。
        // また、遷移に必要なtotalbuildnumも5倍となっている。
        // 恐らく、実装によりlevellistの最終要素を無視しなければならない。

        int maxLevel = BuildAssist.levellist().size() - 1;

        if (maxLevel <= level) {
            return;
        }

        for (;level < maxLevel; level++) {
            if (totalbuildnum.doubleValue() < (int) BuildAssist.levellist().apply(level)) {
                break;
            }

            player.sendMessage(ChatColor.GOLD + "ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(" + level + ")→建築Lv(" + (level + 1) + ")】");
        }

        if (level == maxLevel) {
            player.sendMessage(ChatColor.GOLD + "最大Lvに到達したよ(`･ω･´)");
        }
    }

    /**
     * オフラインかどうか
     */
    boolean isOffline() {
        return Bukkit.getServer().getPlayer(uuid) == null;
    }

    /**
     * 建築系データを読み込む
     *
     * @param player
     * @return true:読み込み成功　false:読み込み失敗
     */
    boolean buildload(final Player player) {
        final com.github.unchama.seichiassist.data.player.PlayerData playerdata_s = SeichiAssist.playermap().getOrElse(uuid, () -> null);
        if (playerdata_s == null) {
            return false;
        }
        final int server_num = SeichiAssist.seichiAssistConfig().getServerNum();

        final BuildCount oldBuildCount = playerdata_s.buildCount();

        totalbuildnum = playerdata_s.buildCount().count();
        //ブロック設置カウントが統合されてない場合は統合する
        if (server_num >= 1 && server_num <= 3) {
            byte f = playerdata_s.buildCount().migrationFlag();
            if ((f & (0x01 << server_num)) == 0) {
                if (f == 0) {
                    // 初回は加算じゃなくベースとして代入にする
                    totalbuildnum = BuildBlock.calcBuildBlock(player);
                } else {
                    totalbuildnum = totalbuildnum.add(BuildBlock.calcBuildBlock(player));
                }
                f = (byte) (f | (0x01 << server_num));
                final BuildCount updatedBuildCount = playerdata_s.buildCount().copy(oldBuildCount.lv(), totalbuildnum, f);
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
    public void buildsave(final Player player) {
        final com.github.unchama.seichiassist.data.player.PlayerData playerData = SeichiAssist.playermap().getOrElse(uuid, () -> null);
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "建築系データ保存失敗しました");
            return;
        }

        final BuildCount oldBuildCount = playerData.buildCount();

        //1分制限の判断
        final BigDecimal newBuildCount;
        if (build_num_1min.doubleValue() <= BuildAssist.config().getBuildNum1minLimit()) {
            newBuildCount = totalbuildnum.add(build_num_1min);
        } else {
            newBuildCount = totalbuildnum.add(new BigDecimal(BuildAssist.config().getBuildNum1minLimit()));
        }

        playerData.buildCount_$eq(new BuildCount(level, newBuildCount, oldBuildCount.migrationFlag()));
    }

}
