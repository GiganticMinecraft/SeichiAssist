package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillNormalEffect;
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.stream.IntStream;

public class ActiveSkillData_Legacy {
    //アクティブスキルポイント
    public int skillpoint;
    //アクティブスキルエフェクトポイント
    public int effectpoint;
    //プレミアムアクティブスキルエフェクトポイント
    public int premiumeffectpoint;
    //投てきスキル獲得量
    public int arrowskill;
    //連続破壊スキル獲得量
    public int multiskill;
    //破壊スキル獲得量
    public int breakskill;
    //凝固スキル獲得量
    public int fluidcondenskill;
    //水凝固スキル獲得量
    public int watercondenskill;
    //熔岩凝固スキル獲得量
    public int lavacondenskill;
    //アクティブスキルの種類番号を格納
    public int skilltype;
    //アサルトスキルの種類番号を格納
    public int assaulttype;
    //選択されているアクティブスキルの番号を格納
    public int skillnum;
    //選択されているアサルトスキルの番号を格納
    public int assaultnum;
    //スキルクールダウン用フラグ
    public boolean skillcanbreakflag;
    //採掘用アクティブスキルのフラグ 0:なし 1:上破壊 2:下破壊
    public int mineflagnum;
    //アサルトスキル.コンデンススキルのtask
    private BukkitTask assaulttask;
    //自然マナ回復のtask
    public BukkitTask manaregenetask;
    //アサルトスキルのフラグ
    public boolean assaultflag;
    //エフェクトの獲得フラグリスト<エフェクト番号,エフェクト獲得フラグ>
    public HashSet<ActiveSkillNormalEffect> obtainedSkillEffects = new HashSet<>();
    //スペシャルエフェクトの獲得フラグリスト<エフェクト番号,エフェクト獲得フラグ>
    public HashSet<ActiveSkillPremiumEffect> obtainedSkillPremiumEffects = new HashSet<>();
    //選択されているアクティブスキルの番号を格納
    public int effectnum; // TODO 100以下ならプレミアムスキル、という判定ロジックを隠すべき
    //通常スキルで破壊されるエリア
    public BreakArea_Legacy area;
    //アサルトスキルで破壊されるエリア
    public BreakArea_Legacy assaultarea;
    //マナクラス
    public Mana mana;

    public ActiveSkillData_Legacy() {
        mineflagnum = 0;
        assaultflag = false;
        assaulttask = null;
        skilltype = 0;
        skillnum = 0;
        skillcanbreakflag = true;
        skillpoint = 0;
        effectpoint = 0;
        premiumeffectpoint = 0;
        arrowskill = 0;
        multiskill = 0;
        breakskill = 0;
        watercondenskill = 0;
        lavacondenskill = 0;
        fluidcondenskill = 0;
        effectnum = 0;

        area = null;
        assaultarea = null;

        mana = new Mana();
    }

    private static int decreasePoint(int level) {
        return level * 10;
    }

    //activeskillpointをレベルに従って更新
    public void updateActiveSkillPoint(Player player, int level) {
        // 1..10 -> 1pt/each
        // 11..20 -> 2pt/each
        // ...
        // 10n-9..10n -> npt/each
        int point = IntStream.rangeClosed(1, level).map(i -> (int) Math.ceil(((double) i) / 10.0)).sum();
        //レベルに応じたスキルポイント量を取得
        if (SeichiAssist.DEBUG()) {
            player.sendMessage("あなたのレベルでの獲得アクティブスキルポイント：" + point);
        }
        //取得しているスキルを確認してその分のスキルポイントを引く
        //遠距離スキル
        // arrowskill -> 4は(arrowskill-4).repeatと同じ
        point -= IntStream.rangeClosed(4, arrowskill)
                .map(ActiveSkillData_Legacy::decreasePoint)
                .sum();
        //マルチ破壊スキル
        point -= IntStream.rangeClosed(4, multiskill)
                .map(ActiveSkillData_Legacy::decreasePoint)
                .sum();
        //破壊スキル
        point -= IntStream.rangeClosed(1, breakskill)
                .map(ActiveSkillData_Legacy::decreasePoint)
                .sum();
        //水凝固スキル
        point -= IntStream.rangeClosed(7, watercondenskill)
                .map(ActiveSkillData_Legacy::decreasePoint)
                .sum();
        //熔岩凝固スキル
        point -= IntStream.rangeClosed(7, lavacondenskill)
                .map(ActiveSkillData_Legacy::decreasePoint)
                .sum();
        if (fluidcondenskill == 10) {
            point -= 110;
        }

        if (SeichiAssist.DEBUG()) {
            player.sendMessage("獲得済みスキルを考慮したアクティブスキルポイント：" + point);
            point += 10000;
        }
        if (point < 0) {
            reset();
            player.sendMessage("アクティブスキルポイントが負の値となっていたため、リセットしました。");
            updateActiveSkillPoint(player, level);
        } else {
            skillpoint = point;
        }
    }

    public void reset() {
        //タスクを即終了
        if (assaultflag) try {
            this.assaulttask.cancel();
        } catch (NullPointerException e) {
        }

        //初期化
        arrowskill = 0;
        multiskill = 0;
        breakskill = 0;
        watercondenskill = 0;
        lavacondenskill = 0;
        fluidcondenskill = 0;
        skilltype = 0;
        skillnum = 0;
        assaulttype = 0;
        assaultnum = 0;

        assaultflag = false;

    }

    public void updateOnJoin(Player player, int level) {
        updateActiveSkillPoint(player, level);
        mana.initialize(player, level);
    }

    public void updateOnQuit() {
        mana.hide();
    }
}
