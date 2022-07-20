package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gttosiina

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gttosiina.bukkit.listeners.GtToSiinaringo
import org.bukkit.event.Listener

object System {

  def wired[F[_]: ConcurrentEffect: GachaPrizesDataOperations: GachaAPI]: Subsystem[F] = {
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new GtToSiinaringo[F]())
    }
  }

}
