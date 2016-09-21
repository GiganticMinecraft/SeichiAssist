package com.github.unchama.seichiassist.skilleffect;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class ExplosionTaskRunnable extends BukkitRunnable{
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
	Location explosionloc;


	public ExplosionTaskRunnable(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist, Coordinate start,
			Coordinate end, Location standard) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.breaklist = breaklist;
		this.start = start;
		this.end = end;
		this.standard = standard;
		breaklength = ActiveSkill.BREAK.getBreakLength(playerdata.level);
	}

	@Override
	public void run() {
		Random rand = new Random();
		for(int x = start.x + 1 ; x < end.x ; x=x+2){
			for(int z = start.z + 1 ; z < end.z ; z=z+2){
				for(int y = start.y + 1; y < end.y ; y=y+2){
					explosionloc = standard.clone();
					player.getWorld().createExplosion(explosionloc.add(x, y, z), 0, false);
					//player.spawnParticle(Particle.EXPLOSION_NORMAL,explosionloc.add(x, y, z),1);
					//player.playSound(explosionloc.add(x, y, z), Sound.ENTITY_GENERIC_EXPLODE, (float)1, (float)((rand.nextDouble()*0.4)+0.8));
					//player.getWorld().playEffect(explosionloc.add(x, y, z), Effect.EXPLOSION, 0,(int)10);
				}
			}
		}
		for(Block b : breaklist){
			Util.BreakBlock(player, b, standard, tool, false);
			playerdata.activeskilldata.blocklist.remove(b);
		}
	}

}

