package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.entity.Entity
import org.bukkit.scheduler.BukkitRunnable

class AsyncEntityRemover(var e: Entity) extends BukkitRunnable {
  override def run(): Unit = {
    SeichiAssist.managedEntities.$minus$eq(e)
    e.remove()
  }
}