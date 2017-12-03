package com.github.unchama.seichiassist.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.PlayerData;

// マナ自動回復用タスク
// 現在リスナー停止により無効化中
public class ManaRegeneTaskRunnable extends BukkitRunnable {
	private Player p;
	public ManaRegeneTaskRunnable(Player player) {
		p = player;
	}

	@Override
	public void run() {
		PlayerData pd = SeichiAssist.playermap.get(p.getUniqueId());
		Mana mana = pd.activeskilldata.mana;
		int lv = pd.level;
		// 最大マナを取得する
		double max = mana.calcMaxManaOnly(p, pd.level);
		// マナを1%回復する
		mana.increaseMana(max * 0.01, p, lv);
	}
}
