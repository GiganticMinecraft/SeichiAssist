package com.github.unchama.seichiassist.data;


import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.player.PlayerData;
import com.github.unchama.seichiassist.util.TypeConverter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.UUID;


public class Mana {
    //マナの値
    private double value;
    //マックスの値
    private double max;
    //バークラス
    private BossBar bar;
    //読み込まれているかどうかのフラグ
    private boolean loadflag;

    //引数なしのコンストラクタ
    @Deprecated
    public Mana() {
        value = 0;
        setMax(0);
        loadflag = false;
    }

    //必ず最初のみ実行してほしいメソッド
    public void initialize(Player player, int level) {
        //現在のレベルでのマナ上限値を計算しバーに表示
        //mの値は既に得られているはず。
        loadflag = true;
        calcAndSetMax(player, level);
        display(player, level);

    }

    //現在マナをマナバーに表示させる
    public void display(Player player, int level) {
        if (!loadflag) return;
        hide();
        setBar(player, level);
    }

    private void setBar(Player player, int level) {
        bar = player.getServer().createBossBar(ChatColor.AQUA + "" + ChatColor.BOLD + "マナ(" + TypeConverter.Decimal(value) + "/" + getMax() + ")", BarColor.BLUE, BarStyle.SOLID);

        final double beforeResetMax = getMax();
        if (value < 0 || beforeResetMax < 0 || value > beforeResetMax) {
            reset(player, level);
            player.sendMessage(ChatColor.RED + "不正な値がマナとして保存されていたためリセットしました。");
        }
        final double trueMax = getMax();

        if (trueMax <= 0) {
            return;
        }

        bar.setProgress(value / trueMax);
        bar.addPlayer(player);
    }

    private void reset(Player player, int level) {
        calcAndSetMax(player, level);
        if (value < 0.0) value = 0;
        if (value > getMax()) value = getMax();
    }

    //現在のバーを削除する（更新するときは不要）
    public void hide() {
        if (bar != null) {
            bar.removeAll();
        }
    }

    private void ensureNotOverflow() {
        if (value > getMax()) value = getMax();
    }

    public void increase(double amount, Player player, int level) {
        value += amount;
        ensureNotOverflow();
        display(player, level);
    }

    public void decrease(double amount, Player player, int level) {
        value -= amount;
        if (value < 0) value = 0;
        if (SeichiAssist.DEBUG()) value = getMax();
        display(player, level);
    }

    public boolean has(double amount) {
        return value >= amount;
    }


    //レベルアップするときに実行したい関数
    public void onLevelUp(Player player, int level) {
        calcAndSetMax(player, level);
        setFull(player, level);
    }

    //マナ最大値を計算する処理
    public void calcAndSetMax(Player player, int level) {

        //UUIDを取得
        UUID uuid = player.getUniqueId();
        //playerdataを取得
        PlayerData playerdata = SeichiAssist.playermap().getOrElse(uuid, () -> null);

        if (SeichiAssist.DEBUG()) {
            setMax(100000);
            return;
        }
        //レベルが10行っていない時レベルの値で処理を終了(修正:マナは0)
        if (level < 10) {
            //this.max = level;
            setMax(0.0);
            return;
        }
        //１０行ってる時の処理
        double t_max = 100;
        int increase = 10;
        int inc_inc = 2;
        //１１以降の処理
        for (int i = 10; i < level; i++) {
            if (i % 10 == 0 && i <= 110 && i != 10) {
                increase += inc_inc;
                inc_inc *= 2;
            }
            t_max += increase;
        }
        //貢献度ptの上昇値
        t_max += playerdata.added_mana() * SeichiAssist.seichiAssistConfig().getContributeAddedMana();

        setMax(t_max);
    }

    /**
     * @param level レベル
     * @return 最大マナ
     */
    //マナ最大値を計算する処理
    public double calcMaxManaOnly(Player player, int level) {

        //UUIDを取得
        UUID uuid = player.getUniqueId();
        //playerdataを取得
        PlayerData playerdata = SeichiAssist.playermap().getOrElse(uuid, () -> null);

		/*
		if(SeichiAssist.DEBUG){
			max = 100000;
			return;
		}
		*/
        //レベルが10行っていない時レベルの値で処理を終了
        if (level < 10) {
            //temp_max = level;
            setMax(0.0);
            return 0.0;
        }
        //１０行ってる時の処理
        double t_max = 100;
        int increase = 10;
        int inc_inc = 2;
        //１１以降の処理
        for (int i = 10; i < level; i++) {
            if (i % 10 == 0 && i <= 110 && i != 10) {
                increase += inc_inc;
                inc_inc *= 2;
            }
            t_max += increase;
        }
        //貢献度ptの上昇値
        t_max += playerdata.added_mana() * SeichiAssist.seichiAssistConfig().getContributeAddedMana();

        setMax(t_max);
        return t_max;
    }


    //マナを最大値まで回復する処理
    public void setFull(Player player, int level) {
        value = getMax();
        display(player, level);
    }

    public double getMana() {
        return value;
    }

    public void setMana(double m) {
        value = m;
    }

    public boolean isLoaded() {
        return loadflag;
    }

    public double getMax() {
        return max;
    }

    private void setMax(double max) {
        this.max = max;
    }
}
