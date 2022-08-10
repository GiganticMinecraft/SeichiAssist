package com.github.unchama.seichiassist.subsystems.donate

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.donate.bukkit.commands.DonationCommand
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePersistence,
  DonatePremiumEffectPoint,
  PlayerName
}
import com.github.unchama.seichiassist.subsystems.donate.infrastructure.JdbcDonatePersistence
import org.bukkit.command.TabExecutor

trait System[F[_]] extends Subsystem[F] {

  val api: DonateAPI[F]

}

object System {

  def wired[F[_]: ConcurrentEffect]: System[F] = {
    implicit val persistence: DonatePersistence[F] = new JdbcDonatePersistence[F]

    new System[F] {
      override val api: DonateAPI[F] = new DonateAPI[F] {}

      override val commands: Map[String, TabExecutor] = Map(
        "donation" -> new DonationCommand[F].executor
      )
    }

  }

}
