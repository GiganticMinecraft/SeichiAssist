package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.EffectUtil;
import com.github.unchama.seichiassist.util.Util;

public class AreaVisualizeTaskRunnable extends BukkitRunnable{
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
	//スキル発動中かどうかのフラグ
	int skillflagnum;


	public AreaVisualizeTaskRunnable(Player player, BreakArea area,boolean assaultflag) {
		this.player = player;
		UUID uuid = player.getUniqueId();
		this.playerdata = playermap.get(uuid);
		this.skillflagnum = playerdata.activeskilldata.mineflagnum;
		this.area = area;
		this.assaultflag = assaultflag;
		area.makeArea(assaultflag);
	}

	@Override
	public void run() {
		//初期終了フラグ
		if(this.skillflagnum == 0){
			cancel();
			return;
		}
		targetblock = player.getTargetBlock(SeichiAssist.transparentmateriallist, 40);
		playerlocy = player.getLocation().getBlockY() - 1 ;

		//もし前回とプレイヤーの向いている方向が違ったらBreakAreaを取り直す
		dir = Util.getCardinalDirection(player);
		if(!area.getDir().equals(dir)){
			area.setDir(dir);
			area.makeArea(assaultflag);
		}

		//アサルトスキルであるときの処理
		if(assaultflag){
			AssaultAreaVisualize();
		}
		//他スキルの時の処理
		else{
			AreaVisualise();
		}



	}

	private void AreaVisualise() {
		// TODO 自動生成されたメソッド・スタブ

	}

	private void AssaultAreaVisualize() {
		//areaが変わる要因で範囲を変更する。
		//シフトを押すと即落ちるので変更する必要はない。
		EffectUtil.playEffectSquare(player, Effect.INSTANT_SPELL, 6.5);
	}

}
