package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.SeichiAssist;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncEntityRemover extends BukkitRunnable{
	Entity e;

	public AsyncEntityRemover(Entity e) {
		this.e = e;
	}

	@Override
	public void run() {
		SeichiAssist.entitylist().remove(e);
		e.remove();
	}

}
