package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect

object DragonNightTimeTask {
  def startDragonNightTime(): Unit = {
    val effectData = new FastDiggingEffect(20 * 60 * 60, 10, 3)
    SeichiAssist.playermap.values.foreach(_.effectdatalist.addOne(effectData))
  }
}
