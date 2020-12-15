package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect

object DragonNightTimeTask {
  def startDragonNightTime(): Unit = {
    // FastDiggingEffectはミュータブルなのでプレイヤーごとにインスタンス化する
    SeichiAssist.playermap.values.foreach( 
      _.effectdatalist.addOne(new FastDiggingEffect(20 * 60 * 60, 10, 3))
    )
  }
}
