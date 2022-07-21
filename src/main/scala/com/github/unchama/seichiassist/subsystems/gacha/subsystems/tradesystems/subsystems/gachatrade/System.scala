package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gachatrade

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners.GachaTradeListener
import org.bukkit.event.Listener

object System {

  def wired[F[_]: ConcurrentEffect: GachaPrizesDataOperations: GachaAPI]: Subsystem[F] = {
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new GachaTradeListener[F]())
    }
  }

}
