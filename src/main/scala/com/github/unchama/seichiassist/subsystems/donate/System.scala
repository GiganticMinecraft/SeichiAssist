package com.github.unchama.seichiassist.subsystems.donate

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.subsystems.donate.bukkit.commands.DonationCommand
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePersistence,
  DonatePremiumEffectPoint,
  PremiumEffectPurchaseData
}
import com.github.unchama.seichiassist.subsystems.donate.infrastructure.JdbcDonatePersistence
import org.bukkit.command.TabExecutor

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {

  val api: DonatePremiumPointAPI[F]

}

object System {

  def wired[F[_]: ConcurrentEffect]: System[F] = {
    implicit val persistence: DonatePersistence[F] = new JdbcDonatePersistence[F]

    new System[F] {
      override val api: DonatePremiumPointAPI[F] = new DonatePremiumPointAPI[F] {
        override def currentPoint(uuid: UUID): F[DonatePremiumEffectPoint] =
          persistence.currentPremiumEffectPoints(uuid)

        override def fetchGrantHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]] =
          persistence.donatePremiumEffectPointPurchaseHistory(uuid)

        override def fetchUseHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]] =
          persistence.donatePremiumEffectPointUsageHistory(uuid)

        override def useDonatePremiumEffectPoint(
          uuid: UUID,
          activeSkillPremiumEffect: ActiveSkillPremiumEffect
        ): F[Unit] =
          persistence.useDonatePremiumEffectPoint(uuid, activeSkillPremiumEffect)
      }

      override val commands: Map[String, TabExecutor] = Map(
        "donation" -> new DonationCommand[F].executor
      )
    }

  }

}
