package com.github.unchama.seichiassist.arroweffect;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.data.PlayerData;

public class ArrowBlizzardTaskRunnable extends BukkitRunnable{
	SeichiAssist plugin = SeichiAssist.plugin;
	HashMap<UUID,PlayerData> playermap = SeichiAssist.playermap;
	Player player;
	Location ploc;
	UUID uuid;
	PlayerData playerdata;
	long tick;
	Snowball proj;

	public ArrowBlizzardTaskRunnable(Player player) {
		this.tick = 0;
		this.player = player;
		//プレイヤーの位置を取得
		this.ploc = player.getLocation();
		//UUIDを取得
		this.uuid = player.getUniqueId();
		//ぷれいやーでーたを取得
		this.playerdata = playermap.get(uuid);

		//発射する音を再生する.
    	player.playSound(ploc, Sound.ENTITY_SNOWBALL_THROW, 1, (float)1.3);

    	//スキルを実行する処理
        Location loc = player.getLocation().clone();
        loc.add(loc.getDirection()).add(0,1.6,0);
        Vector vec = loc.getDirection();
        double k = 1.0;
        vec.setX(vec.getX() * k);
        vec.setY(vec.getY() * k);
        vec.setZ(vec.getZ() * k);
        proj = player.getWorld().spawn(loc, Snowball.class);
        SeichiAssist.entitylist.add(proj);
        proj.setShooter(player);
        proj.setGravity(false);
        //読み込み方法
        /*
         * Projectile proj = event.getEntity();
		    if ( proj instanceof Arrow && proj.hasMetadata("ArrowSkill") ) {
		    }
         */
        proj.setMetadata("ArrowSkill", new FixedMetadataValue(plugin, true));
        proj.setVelocity(vec);

	}

	@Override
	public void run() {
		tick ++;
		if(tick > 100){
			proj.remove();
			SeichiAssist.entitylist.remove(proj);
			this.cancel();
			return;
		}
	}

}
