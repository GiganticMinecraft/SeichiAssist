package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.{ConcurrentEffect, Sync, SyncIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrize,
  GachaPrizeId,
  GachaPrizesDataOperations
}
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.JdbcGachaPersistence
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketPersistence
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import org.bukkit.command.TabExecutor

trait System[F[_]] extends Subsystem[F] {
  val api: GachaReadAPI[F] with GachaWriteAPI[F]
}

object System {

  def wired[F[_]: NonServerThreadContextShift: Sync: ConcurrentEffect](
    implicit syncUuidRepository: UuidRepository[SyncIO]
  ): System[F] = {
    val gachaPersistence = new JdbcGachaPersistence[F]()
    new GachaPrizesDataOperations[F].loadGachaPrizes(gachaPersistence)
    implicit val gachaTicketPersistence: JdbcGachaTicketPersistence[F] =
      new JdbcGachaTicketPersistence[F]

    new System[F] {
      override implicit val api: GachaAPI[F] = new GachaAPI[F] {
        override def upsert(gachaPrize: GachaPrize): F[Unit] =
          gachaPersistence.upsert(gachaPrize)

        override def remove(id: GachaPrizeId): F[Boolean] = gachaPersistence.remove(id)

        override def list: F[Vector[GachaPrize]] = gachaPersistence.list
      }
      override val commands: Map[String, TabExecutor] = Map(
        "gacha" -> new GachaCommand[F]().executor
      )
    }
  }

}
