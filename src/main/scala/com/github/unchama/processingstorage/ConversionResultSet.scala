package com.github.unchama.processingstorage

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

case class ConversionResultSet(list: List[ConversionResult]) {
  def giveEffect: TargetedEffect[Player] = {
    Util.grantItemStacksEffect[IO](list.map(_.itemStack).filter(_.nonEmpty).map(_.get): _*)
  }
}
