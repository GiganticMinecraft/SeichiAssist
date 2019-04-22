package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BreakUtil;

public class AreaControlTaskRunnable extends BukkitRunnable{
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	PlayerData playerdata;
	//プレイヤーがターゲットしているブロックを取得
	Block targetblock;
	//プレイヤーの足のy座標を取得
	int playerlocy;
	//プレイヤーの向いている方角を取得
	String dir;

	//プレイヤーの破壊する範囲を取得
	BreakArea area;
	//アサルトスキルかどうかのフラグ
	boolean assaultflag;
	//ビジュアライズフラグ
	boolean visualizeflag;
	//スキル発動中かどうかのフラグ
	int skillflagnum;
	//tick数の確認
	int tick;


	public AreaControlTaskRunnable(Player player, BreakArea area,boolean assaultflag) {
		this.player = player;
		UUID uuid = player.getUniqueId();
		this.playerdata = playermap.get(uuid);
		this.skillflagnum = playerdata.activeskilldata.mineflagnum;
		this.area = area;
		this.assaultflag = assaultflag;
		this.tick = 0;
	}

	@Override
	public void run() {
		//初期終了フラグ
		if(this.skillflagnum == 0){
			cancel();
			return;
		}
		tick++;

		targetblock = player.getTargetBlock(SeichiAssist.transparentmateriallist, 40);
		playerlocy = player.getLocation().getBlockY() - 1 ;

		//もし前回とプレイヤーの向いている方向が違ったらBreakAreaを取り直す
		dir = BreakUtil.getCardinalDirection(player);
		if(!area.getDir().equals(dir)){
			area.setDir(dir);
			area.makeArea();
		}
	}
}
