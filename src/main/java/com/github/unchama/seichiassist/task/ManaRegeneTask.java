package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Mana;
import com.github.unchama.seichiassist.data.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * マナ自動回復用タスク
 * 現在リスナー停止により無効化中
 * @author たぶんtar0ss
 *
 */
public class ManaRegeneTask extends BukkitRunnable {
	private Player p;
	public ManaRegeneTask(Player player) {
		p = player;
	}

	@Override
	public void run() {
		PlayerData pd = SeichiAssist.getPlayermap().get(p.getUniqueId());
		Mana mana = pd.getActiveskilldata().mana;
		int lv = pd.getLevel();
		// 最大マナを取得する
		double max = mana.calcMaxManaOnly(p, pd.getLevel());
		// マナを1%回復する
		mana.increase(max * 0.01, p, lv);
	}
}
