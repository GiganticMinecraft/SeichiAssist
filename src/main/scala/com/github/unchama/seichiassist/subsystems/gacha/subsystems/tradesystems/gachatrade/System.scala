package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gachatrade

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gachatrade.bukkit.listeners.GachaTradeListener
import org.bukkit.event.Listener

object System {

  def wired[F[_]: Sync: ConcurrentEffect](
    implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): Subsystem[F] = {
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new GachaTradeListener[F]())
    }
  }

}
