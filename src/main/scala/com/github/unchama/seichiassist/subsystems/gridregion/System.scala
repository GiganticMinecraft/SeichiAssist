package com.github.unchama.seichiassist.subsystems.gridregion

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {

  val api: GridRegionAPI[F, Player]

}

object System {

  def wired[F[_]]: System[F, Player] = {

  }

}
