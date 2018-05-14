package com.github.unchama.seichiassist.breakeffect;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.task.ArrowControlTaskRunnable;
import com.github.unchama.seichiassist.util.BreakUtil;

public class MeteoTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
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
	//音の聞こえる距離
	int soundradius;
	//音距離を設定するフラグ
	boolean soundflag;
	//隕石
	LargeFireball proj;

	public MeteoTaskRunnable(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist, Coordinate start,
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

		//音の聞こえる範囲を設定
		soundradius = 5;

		//音を設定するか設定
        soundflag = playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum();
		if(playerdata.activeskilldata.skillnum > 2){
			launchFireball();
		}

	}





	private void launchFireball() {
		//メテオの発射位置を決定
		Random rand = new Random();
		Location launchloc = ploc.add((rand.nextDouble() * 20)-10 ,(rand.nextDouble() * 20) - 10 + 60,(rand.nextDouble() * 20)-10);
		Vector vec = new Vector(centerbreakloc.getX()-launchloc.getX(),centerbreakloc.getY()-launchloc.getY(),centerbreakloc.getZ()-launchloc.getZ());
		launchloc.setDirection(vec);
		vec.normalize();
		proj = player.getWorld().spawn(launchloc,LargeFireball.class);
        proj.setShooter(player);
        proj.setMetadata("Effect", new FixedMetadataValue(plugin, true));
		double k = 1.0;
        vec.setX(vec.getX() * k);
        vec.setY(vec.getY() * k);
        vec.setZ(vec.getZ() * k);
        proj.setShooter(player);
        proj.setMetadata("Effect", new FixedMetadataValue(plugin, true));
        proj.setVelocity(vec);
        new ArrowControlTaskRunnable(proj,centerbreakloc).runTaskTimer(plugin, 0, 1);
	}

	@Override
	public void run() {
		for(int x = start.x + 1 ; x < end.x ; x=x+2){
			for(int z = start.z + 1  ; z < end.z ; z=z+2){
				for(int y = start.y + 1 ; y < end.y ; y=y+2){
					effectloc = droploc.clone();
					effectloc.add(x,y,z);
					if(isBreakBlock(effectloc)){
						player.getWorld().playEffect(effectloc,Effect.EXPLOSION_HUGE,1);
					}
					//player.spawnParticle(Particle.EXPLOSION_NORMAL,explosionloc.add(x, y, z),1);
					//player.playSound(explosionloc.add(x, y, z), Sound.ENTITY_GENERIC_EXPLODE, (float)1, (float)((rand.nextDouble()*0.4)+0.8));
					//player.getWorld().playEffect(explosionloc.add(x, y, z), Effect.EXPLOSION, 0,(int)10);
				}
			}
		}
		player.getWorld().playSound(centerbreakloc, Sound.ENTITY_WITHER_BREAK_BLOCK, (float)1, (float)((Math.random()*0.4)+0.8));
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

