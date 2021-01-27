package com.github.unchama.seichiassist.subsystems.ranking

import cats.effect.{Concurrent, Timer}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.ranking.application.RefreshingRankingCache
import com.github.unchama.seichiassist.subsystems.ranking.domain.{RankingRecordPersistence, SeichiRanking}
import com.github.unchama.seichiassist.subsystems.ranking.infrastructure.JdbcRankingRecordPersistence
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

trait System[F[_], H[_]] extends Subsystem[H] {

  val getSeichiRanking: F[SeichiRanking]

}

object System {

  import cats.implicits._

  def wired[
    F[_] : Timer : Concurrent,
    H[_]
  ]: F[System[F, H]] = {
    val persistence: RankingRecordPersistence[F] = new JdbcRankingRecordPersistence[F]

    RefreshingRankingCache.withPersistence(persistence).map { getSeichiRankingCache =>
      new System[F, H] {
        override val getSeichiRanking: F[SeichiRanking] = getSeichiRankingCache
        override val listeners: Seq[Listener] = Vector.empty
        override val managedFinalizers: Seq[PlayerDataFinalizer[H, Player]] = Vector.empty
        override val commands: Map[String, TabExecutor] = Map.empty
      }
    }
  }
}
