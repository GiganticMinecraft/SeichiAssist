package com.github.unchama.seichiassist.data;

import com.github.unchama.seichiassist.LevelThresholds;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class ExpBar {
	private BossBar expbar;
	private Player p;
	private PlayerData pd;

	public ExpBar(PlayerData parent, Player player) {
		this.pd = parent;
		this.p = player;
		expbar = p.getServer().createBossBar("", BarColor.YELLOW, BarStyle.SOLID);
		expbar.setVisible(false);
	}

	public void remove() {
		// ExpBar表示済みなら一度削除
		try {
			expbar.removeAll();
		} catch (NullPointerException e) {
		}
	}

	public void calculate() {
		// ExpBar有効時のみ
		if (expbar.isVisible()) {
			remove();
			// レベル上限の人
			if (pd.getLevel() >= LevelThresholds.INSTANCE.getLevelExpThresholds().size()) {
				// BarをMAXにして総整地量を表示
				String bartext = ChatColor.GOLD + "" + ChatColor.BOLD + "Lv " + pd.getLevel() + "(総整地量: " + String.format("%,d", pd.getTotalbreaknum()) + ")";
				expbar = p.getServer().createBossBar(bartext, BarColor.YELLOW, BarStyle.SOLID);
				expbar.setProgress(1.0);
			} else {
				// 現在のLvにおける割合をBarに配置
				long exp = pd.getTotalbreaknum() - LevelThresholds.INSTANCE.getLevelExpThresholds().get(pd.getLevel() - 1);
				int expmax = LevelThresholds.INSTANCE.getLevelExpThresholds().get(pd.getLevel()) - LevelThresholds.INSTANCE.getLevelExpThresholds().get(pd.getLevel() - 1);
				String bartext = ChatColor.GOLD + "" + ChatColor.BOLD + "Lv " + pd.getLevel() + "(" + String.format("%,d", pd.getTotalbreaknum()) + "/"
						+ String.format("%,d", LevelThresholds.INSTANCE.getLevelExpThresholds().get(pd.getLevel())) + ")";
				expbar = p.getServer().createBossBar(bartext, BarColor.YELLOW, BarStyle.SOLID);
				// 範囲チェック
				if(exp >= expmax) {
					// レベルアップ前にログアウトした場合、次回ログイン時のレベルアップ処理までに100%を超えている場合がある
					expbar.setProgress(1.0);
				} else if(exp <= 0) {
					expbar.setProgress(0.0);
				} else {
					expbar.setProgress((double) exp / expmax);
				}
			}
			// 描画処理
			expbar.addPlayer(p);
			pd.getActiveskilldata().mana.display(p, pd.getLevel());
		}
	}

	public void setVisible(boolean visible) {
		expbar.setVisible(visible);
		calculate();
	}

	public boolean isVisible() {
		return expbar.isVisible();
	}
}
