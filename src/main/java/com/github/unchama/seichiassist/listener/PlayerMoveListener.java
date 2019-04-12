package com.github.unchama.seichiassist.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.ManaRegeneTaskRunnable;

// マナ自動回復用リスナー
// 現在リスナー停止により無効化中
public class PlayerMoveListener implements Listener {
	// プレイヤーがworld変更した際から自動回復タスクを開始
	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		startTask(event.getPlayer());
	}

	// プレイヤーが動いた際に自動回復タスクを再生成
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		startTask(event.getPlayer());
	}

	// 自動回復タスクの登録
	private void startTask(Player player) {
		PlayerData pd = SeichiAssist.playermap.get(player.getUniqueId());
		// 移動により回復カウントをキャンセルする
		try { pd.activeskilldata.manaregenetask.cancel(); } catch (NullPointerException e) {}
		// 5秒後から5秒間隔でマナ回復タスクを呼び出す
		pd.activeskilldata.manaregenetask = new ManaRegeneTaskRunnable(player).runTaskTimer(SeichiAssist.plugin, 100, 100);
	}
}
