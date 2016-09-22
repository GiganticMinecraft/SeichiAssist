package com.github.unchama.seichiassist.breakeffect;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class BlizzardTaskRunnable extends BukkitRunnable{
	Player player;
	PlayerData playerdata;
	ItemStack tool;
	//破壊するブロックリスト
	List<Block> breaklist;
	//スキルで破壊される相対座標
	Coordinate start,end;
	//スキルが発動される中心位置
	Location standard;
	//相対座標から得られるスキルの範囲座標
	Coordinate breaklength;
	//逐一更新が必要な位置
	Location effectloc;
	//音の聞こえる距離
	int soundradius;
	boolean soundflag;

	public BlizzardTaskRunnable(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist, Coordinate start,
			Coordinate end, Location standard) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.breaklist = breaklist;
		this.start = start;
		this.end = end;
		this.standard = standard;

		for(Block b : breaklist){
			Util.BreakBlock(player, b, standard, tool, false);
			b.setType(Material.PACKED_ICE);
		}
		soundradius = 5;

		if(playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum()){
			soundflag = true;
		}else{
			soundflag = false;
		}
	}

	@Override
	public void run() {
		for(int x = start.x ; x < end.x ; x++){
			for(int z = start.z  ; z < end.z ; z++){
				for(int y = start.y ; y < end.y ; y++){
					effectloc = standard.clone();
					effectloc.add(x,y,z);
					if(breaklist.contains(effectloc.getBlock())){
						player.getWorld().playEffect(effectloc,Effect.SNOWBALL_BREAK,1);
					}
					//player.spawnParticle(Particle.EXPLOSION_NORMAL,explosionloc.add(x, y, z),1);
					//player.playSound(explosionloc.add(x, y, z), Sound.ENTITY_GENERIC_EXPLODE, (float)1, (float)((rand.nextDouble()*0.4)+0.8));
					//player.getWorld().playEffect(explosionloc.add(x, y, z), Effect.EXPLOSION, 0,(int)10);
				}
			}
		}
		for(Block b : breaklist){
			b.setType(Material.AIR);
			if(soundflag){
				b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND,Material.PACKED_ICE,soundradius);
			}else{
				b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND,Material.PACKED_ICE);
			}
			playerdata.activeskilldata.blocklist.remove(b);
		}
	}

}

