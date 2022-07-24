package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.ConcurrentEffect
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.bukkit.FairyLoreTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  AppleOpenState,
  FairyLore,
  FairySummonCost
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: FairyAPI[F]
}

object System {

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread]: System[F] = {
    val persistence = new JdbcFairyPersistence[F]
    new System[F] {
      override val api: FairyAPI[F] = new FairyAPI[F] {
        override def appleOpenState(uuid: UUID): F[AppleOpenState] =
          persistence.appleOpenState(uuid)

        override def updateAppleOpenState(uuid: UUID, appleOpenState: AppleOpenState): F[Unit] =
          persistence.changeAppleOpenState(uuid, appleOpenState)

        import cats.implicits._

        override def getFairyLore(uuid: UUID): F[FairyLore] = for {
          state <- appleOpenState(uuid)
        } yield {
          FairyLoreTable.loreTable(state.amount)
        }

        override def updateFairySummonCost(
          uuid: UUID,
          fairySummonCost: FairySummonCost
        ): F[Unit] =
          persistence.updateFairySummonCost(uuid, fairySummonCost)

        override def fairySummonCost(uuid: UUID): F[FairySummonCost] =
          persistence.fairySummonCost(uuid)
      }
    }
  }

}
