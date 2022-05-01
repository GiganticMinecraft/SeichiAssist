package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrize,
  GachaPrizeId,
  GachaPrizesDataOperations
}
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.JdbcGachaPersistence

trait System[F[_]] extends Subsystem[F] {
  val api: GachaReadAPI[F] with GachaWriteAPI[F]
}

object System {

  def wired[F[_]: NonServerThreadContextShift: Sync]: System[F] = {
    val persistence = new JdbcGachaPersistence[F]()
    new GachaPrizesDataOperations[F].loadGachaPrizes(persistence)

    new System[F] {
      override implicit val api: GachaAPI[F] = new GachaAPI[F] {
        override def upsert(gachaPrize: GachaPrize): F[Unit] = persistence.upsert(gachaPrize)

        override def remove(id: GachaPrizeId): F[Boolean] = persistence.remove(id)

        override def list: F[Vector[GachaPrize]] = persistence.list
      }
    }
  }

}
