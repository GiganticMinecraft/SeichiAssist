package com.github.unchama.seichiassist.task;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.unchama.seichiassist.SeichiAssist;

public class AsyncEntityRemover extends BukkitRunnable{
	Entity e;

	public AsyncEntityRemover(Entity e) {
		this.e = e;
	}

	@Override
	public void run() {
		SeichiAssist.Companion.getEntitylist().remove(e);
		e.remove();
	}

}
