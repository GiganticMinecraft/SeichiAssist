package com.github.unchama.seichiassist.breakeffect;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.ActiveSkill;
import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.Coordinate;
import com.github.unchama.seichiassist.data.PlayerData;
import com.github.unchama.seichiassist.util.Util;

public class MeteoTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	//プレイヤー情報
	Player player;
	//プレイヤーデータ
	PlayerData playerdata;
	//プレイヤーの位置情報
	Location ploc;
	//使用するツール
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
	//音距離を設定するフラグ
	boolean soundflag;
	//飛ばすもの
	Projectile proj;

	public MeteoTaskRunnable(Player player,PlayerData playerdata,ItemStack tool,List<Block> breaklist, Coordinate start,
			Coordinate end, Location standard) {
		this.player = player;
		this.playerdata = playerdata;
		this.tool = tool;
		this.breaklist = breaklist;
		this.start = start;
		this.end = end;
		this.standard = standard;

		this.ploc = player.getLocation();

		//音の聞こえる範囲を設定
		soundradius = 5;

		//音を設定するか設定
		if(playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum()){
			soundflag = true;
		}else{
			soundflag = false;
		}


		switch(playerdata.activeskilldata.skillnum){
		case 1:
		case 2:
		case 3:
			launchFireball(SmallFireball.class);
			break;
		case 4:
		case 5:
		case 6:
			launchFireball(Fireball.class);
			break;
		case 7:
		case 8:
		case 9:
			launchFireball(LargeFireball.class);
			break;
		}
	}

	private <T> void launchFireball(Class<T> clazz) {
		/*
		//blockの位置を取得
		Location loc = standard.clone();
		//プレイヤーの位置を取得
		Location ploc = player.getLocation();
		//メテオの発射位置を決定
		Location mloc = ploc.add(0,60,0);
		double speed = 0.0001;
		//メテオ発射
		Vector vec = new Vector(loc.getX()-mloc.getX(),loc.getY()-mloc.getY(),loc.getZ()-mloc.getZ());
		vec.multiply(speed);
		//Vector vec = new Vector(0,-1,0);
		final LargeFireball meteo = loc.getWorld().spawn(mloc, LargeFireball.class);
		meteo.setDirection(vec);
		meteo.setVelocity(vec);
		Location loc = player.getLocation().clone();
		Vector direction = ploc.getDirection().clone();
		direction.setX(direction.getX()*-15);
		direction.setZ(direction.getY()+60);
		direction.setZ(direction.getZ()*-15);
		loc.add(direction).add(0,60,0);

		Location launchloc = player.getLocation().clone().add(direction);
		Vector vec = launchloc.getDirection();
		vec.setX(vec.getX()*15);
        vec.setY(vec.getY()-60);
        vec.setZ(vec.getZ()*15);
        vec.normalize();
		double k = 1.0;
        vec.setX(vec.getX() * k);
        vec.setY(vec.getY() * k);
        vec.setZ(vec.getZ() * k);
		proj = player.getWorld().spawn(launchloc, Snowball.class);
        proj.setShooter(player);
        proj.setMetadata("Effect", new FixedMetadataValue(plugin, true));
        proj.setVelocity(vec);
*/
	}

	@Override
	public void run() {
		for(int x = start.x ; x < end.x ; x++){
			for(int z = start.z  ; z < end.z ; z++){
				for(int y = start.y ; y < end.y ; y++){
					effectloc = standard.clone();
					player.getWorld().playEffect(effectloc.add(x,y,z),Effect.SNOWBALL_BREAK,1);
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

