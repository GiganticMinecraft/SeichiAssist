package com.github.unchama.seichiassist.breakeffect;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.moveParticleTaskRunnable;
import com.github.unchama.seichiassist.util.BreakUtil;

public class VladmiaTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	//プレイヤー情報
	Player player;
	//プレイヤーデータ
	PlayerData playerdata;
	//プレイヤーの位置情報
	Location ploc;
	//ブロックの位置情報
	Location bloc;
	//破壊するブロックの中心位置
	Location centerbreakloc;
	//使用するツール
	ItemStack tool;
	//破壊するブロックリスト
	List<Block> breaklist;
	//スキルで破壊される相対座標
	Coordinate start,end;
	//スキルが発動される中心位置
	Location droploc;
	//相対座標から得られるスキルの範囲座標
	Coordinate breaklength;
	//逐一更新が必要な位置
	Location effectloc;
	Wool red;
	BlockState state;
	Material m;


	public VladmiaTaskRunnable(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist, Coordinate start,
			Coordinate end, Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.breaklist = breaklist;
		this.start = start;
		this.end = end;
		this.droploc = droploc.clone();

		this.ploc = player.getLocation().clone();
		this.centerbreakloc = this.droploc.add(start.x + (end.x-start.x)/2, start.y + (end.y-start.y)/2,start.z + (end.z-start.z)/2);



		for(Block b : breaklist){
			BreakUtil.BreakBlock(player, b, droploc, tool, false);
			new moveParticleTaskRunnable(player,b,Color.RED).runTaskTimer(plugin, 0, 3);
		}
	}
	@Override
	public void run() {
		// TODO 自動生成されたメソッド・スタブ

	}

}
