package com.github.unchama.itemconversionstorage

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

case class ConversionResultSet(list: List[ConversionResult]) {
  def giveEffect: TargetedEffect[Player] = {
    Util.grantItemStacksEffect[IO](list.map(_.itemStack).filter(_.nonEmpty).map(_.get): _*)
  }

  def convertedCount: Int = list.count {
    case ConversionResult.Mapped(_) => true
    case _ => false
  }
}
