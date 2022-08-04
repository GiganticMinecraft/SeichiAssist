package com.github.unchama.seichiassist.subsystems.donate

import cats.effect.Sync
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePremiumEffectPoint,
  PlayerName
}
import com.github.unchama.seichiassist.subsystems.donate.infrastructure.JdbcDonatePersistence

trait System[F[_]] extends Subsystem[F] {

  val api: DonateAPI[F]

}

object System {

  def wired[F[_]: Sync]: System[F] = {
    val persistence = new JdbcDonatePersistence[F]

    new System[F] {
      override val api: DonateAPI[F] = new DonateAPI[F] {
        override def addDonatePremiumEffectPoint(
          playerName: PlayerName,
          donatePremiumEffectPoint: DonatePremiumEffectPoint
        ): F[Unit] =
          persistence.addDonatePremiumEffectPoint(playerName, donatePremiumEffectPoint)
      }
    }

  }

}
