package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.MaterialSets;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillNormalEffect;
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillPremiumEffect;
import com.github.unchama.seichiassist.task.AssaultTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.stream.IntStream;

public class ActiveSkillData {
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
    public BreakArea area;
    //アサルトスキルで破壊されるエリア
    public BreakArea assaultarea;
    //マナクラス
    public Mana mana;
    private SeichiAssist plugin = SeichiAssist.instance();

    public ActiveSkillData() {
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
        int point = IntStream.rangeClosed(1, level).map(i -> i / 10 + 1).sum();
        //レベルに応じたスキルポイント量を取得
        if (SeichiAssist.DEBUG()) {
            player.sendMessage("あなたのレベルでの獲得アクティブスキルポイント：" + point);
        }
        //取得しているスキルを確認してその分のスキルポイントを引く
        //遠距離スキル
        // arrowskill -> 4は(arrowskill-4).repeatと同じ
        point -= IntStream.rangeClosed(4, arrowskill)
                .map(ActiveSkillData::decreasePoint)
                .sum();
        //マルチ破壊スキル
        point -= IntStream.rangeClosed(4, multiskill)
                .map(ActiveSkillData::decreasePoint)
                .sum();
        //破壊スキル
        point -= IntStream.rangeClosed(1, breakskill)
                .map(ActiveSkillData::decreasePoint)
                .sum();
        //水凝固スキル
        point -= IntStream.rangeClosed(7, watercondenskill)
                .map(ActiveSkillData::decreasePoint)
                .sum();
        //熔岩凝固スキル
        point -= IntStream.rangeClosed(7, lavacondenskill)
                .map(ActiveSkillData::decreasePoint)
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

    public void RemoveAllTask() {
        try {
            assaulttask.cancel();
        } catch (NullPointerException e) {
        }
    }

    public void updateSkill(int type, int skilllevel, int mineflagnum) {
        this.skilltype = type;
        this.skillnum = skilllevel;
        this.mineflagnum = mineflagnum;
        //スキルが選択されていなければ終了
        if (skilltype == 0) {
            return;
        }
        //スキルフラグがオンの時の処理
        if (mineflagnum != 0) {
            this.area = new BreakArea(type, skilllevel, mineflagnum, false);
        }

    }

    public void updateAssaultSkill(Player player, int type, int skilllevel, int mineflagnum) {
        this.assaulttype = type;
        this.assaultnum = skilllevel;
        this.mineflagnum = mineflagnum;

        try {
            this.assaulttask.cancel();
        } catch (NullPointerException e) {
        }
        //スキルが選択されていなければ終了
        if (assaulttype == 0) {
            return;
        }
        if (mineflagnum != 0) {
            //スキルフラグがオンの時の処理
            this.assaultarea = new BreakArea(type, skilllevel, mineflagnum, true);
            this.assaultflag = true;

            // オフハンドを取得
            ItemStack offHandItem = player.getInventory().getItemInOffHand();

            //場合分け
            if (!MaterialSets.breakMaterials().contains(offHandItem.getType())) {
                // サブハンドにツールを持っていないとき
                player.sendMessage(ChatColor.GREEN + "使うツールをオフハンドにセット(fキー)してください");
            } else if (offHandItem.getDurability() > offHandItem.getType().getMaxDurability() &&
                    !offHandItem.getItemMeta().isUnbreakable()) {
                //耐久値がマイナスかつ耐久無限ツールでない時
                player.sendMessage(ChatColor.GREEN + "不正な耐久値です。");
            } else {
                this.assaulttask = new AssaultTask(player, offHandItem).runTaskTimer(plugin, 10, 10);
            }
        } else {
            //オフの時の処理
            this.assaultflag = false;
        }
    }

    //スキル使用中の場合Taskを実行する
    private void runTask(Player player) {

        //アサルトスキルの実行
        if (this.assaultflag && this.assaulttype != 0) {
            this.updateAssaultSkill(player, this.assaulttype, this.assaultnum, this.mineflagnum);
            String name = ActiveSkill.getActiveSkillName(this.assaulttype, this.assaultnum);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "アサルトスキル:" + name + "  を選択しています。");
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 0.1f);
        }

        //通常スキルの実行
        if (this.skilltype != 0) {
            this.updateSkill(this.skilltype, this.skillnum, this.mineflagnum);
            String name = ActiveSkill.getActiveSkillName(this.skilltype, this.skillnum);
            player.sendMessage(ChatColor.GREEN + "アクティブスキル:" + name + "  を選択しています。");
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 0.1f);
        }


    }

    public void clearSelection(Player player) {
        this.skilltype = 0;
        this.skillnum = 0;
        this.mineflagnum = 0;
        this.assaultnum = 0;
        this.assaulttype = 0;
        this.assaultflag = false;
        try {
            this.assaulttask.cancel();
        } catch (NullPointerException e) {
        }
        player.sendMessage(ChatColor.GREEN + "全ての選択を削除しました。");
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 0.1f);

    }

    public void updateOnJoin(Player player, int level) {
        updateActiveSkillPoint(player, level);
        runTask(player);
        mana.initialize(player, level);
    }

    public void updateOnQuit() {
        mana.hide();
    }
}
