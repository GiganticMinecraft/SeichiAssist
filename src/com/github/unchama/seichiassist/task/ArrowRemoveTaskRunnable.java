package com.github.unchama.seichiassist.task;

import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

public class ArrowRemoveTaskRunnable extends BukkitRunnable{
	Projectile proj;

	public ArrowRemoveTaskRunnable(Projectile proj) {
		this.proj = proj;
	}

	@Override
	public void run() {
		proj.remove();
	}

}
