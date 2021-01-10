package com.github.unchama.seichiassist.subsystems.breakcount

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import org.bukkit.entity.Player

trait System[F[_], G[_]] extends Subsystem[F] {

  val api: BreakCountAPI[G, Player]

}

object System {


}
