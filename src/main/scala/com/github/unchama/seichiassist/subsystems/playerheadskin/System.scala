package com.github.unchama.seichiassist.subsystems.playerheadskin

import cats.effect.Sync
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.{
  PlayerHeadSkinUrlFetcher,
  PlayerHeadUrlRepository
}
import com.github.unchama.seichiassist.subsystems.playerheadskin.infrastructure.{
  JdbcPlayerHeadSkinPersistence,
  PlayerHeadSkinUrlFetcherByMojangAPI
}
import org.bukkit.entity.Player

import java.util.UUID

trait System[F[_], Player] extends Subsystem[F] {
  val api: PlayerHeadSkinAPI[F, Player]
}

object System {

  def wired[F[_]: Sync]: System[F, Player] = {
    implicit val playerHeadSkinUrlFetcher: PlayerHeadSkinUrlFetcher[F] =
      new PlayerHeadSkinUrlFetcherByMojangAPI[F]
    val repository = new PlayerHeadUrlRepository[F]
    val persistence = new JdbcPlayerHeadSkinPersistence[F]

    new System[F, Player] {
      import cats.implicits._

      override val api: PlayerHeadSkinAPI[F, Player] = (player: UUID) =>
        for {
          playerName <- persistence.fetchLastSeenPlayerName(player)
          url <- playerName.flatTraverse(repository.readUrl)
        } yield url
    }
  }

}
