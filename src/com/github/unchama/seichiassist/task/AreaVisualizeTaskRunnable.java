package com.github.unchama.seichiassist.task;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class AreaVisualizeTaskRunnable extends BukkitRunnable{
	Player player;
	PlayerData playerdata;
	//プレイヤーがターゲットしているブロックを取得
	Block targetblock;
	//プレイヤーの足のy座標を取得
	int playerlocy;
	//プレイヤーの向いている方角を取得
	String dir;


	public AreaVisualizeTaskRunnable(Player player, PlayerData playerdata) {
		this.player = player;
		this.playerdata = playerdata;
	}

	@Override
	public void run() {
		this.targetblock = player.getTargetBlock(SeichiAssist.transparentmateriallist, 40);
		this.playerlocy = player.getLocation().getBlockY() - 1 ;
		this.dir = Util.getCardinalDirection(player);
		BreakArea area = ActiveSkill.
	}

}
