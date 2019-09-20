package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArrowControlTask extends BukkitRunnable{

	Projectile proj;
	Location standard;
	Location projloc;
	long tick;
	double speed;
	public ArrowControlTask(Projectile proj, Location standard) {
		this.proj = proj;
		this.standard = standard;
		this.tick = 0;
		this.speed = 6;
	}

	@Override
	public void run() {
		tick++;
		if(tick > 15){
			proj.remove();
			SeichiAssist.Companion.getEntitylist().remove(proj);
			this.cancel();
		}
		projloc = proj.getLocation();



		Vector vec = new Vector(standard.getX()-projloc.getX(),standard.getY()-projloc.getY(),standard.getZ()-projloc.getZ());
		vec.normalize();

		proj.setVelocity(vec.multiply(speed));

	}

}
