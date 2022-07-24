package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.AppleOpenState
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: FairyAPI[F]
}

object System {

  def wired[F[_]: Sync: OnMinecraftServerThread]: System[F] = {
    val persistence = new JdbcFairyPersistence[F]
    new System[F] {
      override val api: FairyAPI[F] = new FairyAPI[F] {
        override def appleOpenState(uuid: UUID): F[AppleOpenState] =
          persistence.appleOpenState(uuid)

        override def updateAppleOpenState(uuid: UUID, appleOpenState: AppleOpenState): F[Unit] =
          persistence.changeAppleOpenState(uuid, appleOpenState)
      }
    }
  }

}
