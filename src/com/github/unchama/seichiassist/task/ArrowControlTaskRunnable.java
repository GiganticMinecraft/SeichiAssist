package com.github.unchama.seichiassist.task;

import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArrowControlTaskRunnable extends BukkitRunnable{

	Projectile proj;
	Location standard;
	Location projloc;
	long tick;
	double speed;
	public ArrowControlTaskRunnable(Projectile proj, Location standard) {
		this.proj = proj;
		this.standard = standard;
		this.tick = 0;
		this.speed = 0;
	}

	@Override
	public void run() {
		tick++;
		speed = speed + 1;
		if(tick > 10){
			this.cancel();
		}
		projloc = proj.getLocation();
		Vector vec = new Vector(standard.getX()-projloc.getX(),standard.getY()-projloc.getY(),standard.getZ()-projloc.getZ());
		vec.normalize();
		proj.setVelocity(vec.multiply(speed));

	}

}
