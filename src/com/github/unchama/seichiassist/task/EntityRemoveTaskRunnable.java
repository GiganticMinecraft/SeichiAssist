package com.github.unchama.seichiassist.task;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityRemoveTaskRunnable extends BukkitRunnable{
	Entity e;

	public EntityRemoveTaskRunnable(Entity e) {
		this.e = e;
	}

	@Override
	public void run() {
		e.remove();
	}

}
