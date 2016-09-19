package com.github.unchama.seichiassist.task;

import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

public class ExplosionTaskRunnable extends BukkitRunnable{
	Projectile proj;

	public ExplosionTaskRunnable(Projectile proj) {
		this.proj = proj;
	}

	@Override
	public void run() {
		proj.remove();
	}

}

