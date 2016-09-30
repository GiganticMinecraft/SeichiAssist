package com.github.unchama.seichiassist.breakeffect;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.BreakUtil;

public class ExplosionTaskRunnable extends BukkitRunnable{
	Player player;
	PlayerData playerdata;
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
	Location explosionloc;


	public ExplosionTaskRunnable(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist, Coordinate start,
			Coordinate end, Location droploc) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.breaklist = breaklist;
		this.start = start;
		this.end = end;
		this.droploc = droploc;
		breaklength = ActiveSkill.BREAK.getBreakLength(playerdata.activeskilldata.skillnum);
	}

	@Override
	public void run() {
		for(int x = start.x + 1 ; x < end.x ; x=x+2){
			for(int z = start.z + 1 ; z < end.z ; z=z+2){
				for(int y = start.y + 1; y < end.y ; y=y+2){
					explosionloc = droploc.clone();
					explosionloc.add(x, y, z);
					if(isBreakBlock(explosionloc)){
						player.getWorld().createExplosion(explosionloc, 0, false);
					}
				}
			}
		}
		if(playerdata.activeskilldata.skillnum > 2){
			for(Block b : breaklist){
				BreakUtil.BreakBlock(player, b, droploc, tool, false);
				SeichiAssist.allblocklist.remove(b);
			}
		}else{
			for(Block b : breaklist){
				BreakUtil.BreakBlock(player, b, droploc, tool, true);
				SeichiAssist.allblocklist.remove(b);
			}
		}
	}

	private boolean isBreakBlock(Location loc) {
		Block b = loc.getBlock();
		if(breaklist.contains(b))return true;
		for(int x = -1 ; x < 2 ; x++){
			for(int z = -1 ; z < 2 ; z++){
				for(int y = -1; y < 2 ; y++){
					if(breaklist.contains(b.getRelative(x, y, z)))return true;
				}
			}
		}
		return false;
	}

}

