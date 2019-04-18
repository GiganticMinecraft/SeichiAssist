package com.github.unchama.seichiassist.task;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;

public class EntityRemoveTaskRunnable extends BukkitRunnable{
	Entity e;

	public EntityRemoveTaskRunnable(Entity e) {
		this.e = e;
	}

	@Override
	public void run() {
		SeichiAssist.entitylist.remove(e);
		e.remove();
	}

}
