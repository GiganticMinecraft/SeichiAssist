package com.github.unchama.seichiassist.task;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.particle.ParticleEffect;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.BreakArea;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.EffectUtil;
import com.github.unchama.seichiassist.util.Util;

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
		area.makeArea(assaultflag);

		if(assaultflag){
			visualizeflag = playerdata.activeskilldata.assaultareaflag;
		}else{
			visualizeflag = playerdata.activeskilldata.areaflag;
		}
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
		dir = Util.getCardinalDirection(player);
		if(!area.getDir().equals(dir)){
			area.setDir(dir);
			area.makeArea(assaultflag);
		}

		//可視化しない場合は以降の処理を実行しない。
		if(!visualizeflag){
			cancel();
			return;
		}
		//以降15tick毎に実行
		if(tick%3 != 0){
			return;
		}

		//以降ビジュアライズ処理
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
		for(int count = 0;count < area.getBreakNum() ; count++){
			Coordinate start = new Coordinate(area.getStartList().get(count));
			Coordinate end = new Coordinate(area.getEndList().get(count));
			EffectUtil.playEffectCube(player,targetblock.getLocation(),ParticleEffect.REDSTONE,start,end,Color.AQUA);
		}
	}

	private void AssaultAreaVisualize() {
		//areaが変わる要因で範囲を変更する。
		//シフトを押すと即落ちるので変更する必要はない。
		for(int count = 0;count < area.getBreakNum() ; count++){
			Coordinate start = new Coordinate(area.getStartList().get(count));
			Coordinate end = new Coordinate(area.getEndList().get(count));
			start.add(0, 1, 0);
			EffectUtil.playEffectCube(player,player.getWorld().getBlockAt(player.getLocation()).getLocation(),ParticleEffect.REDSTONE,start,end,Color.PURPLE);
		}
	}

}
