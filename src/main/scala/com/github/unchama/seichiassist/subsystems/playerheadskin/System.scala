package com.github.unchama.seichiassist.subsystems.playerheadskin

import cats.effect.Sync
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.{
  PlayerHeadSkinUrlFetcher,
  PlayerHeadUrlRepository
}
import com.github.unchama.seichiassist.subsystems.playerheadskin.infrastructure.PlayerHeadSkinUrlFetcherByMojangAPI
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {
  val api: PlayerHeadSkinAPI[F, Player]
}

object System {

  def wired[F[_]: Sync]: System[F, Player] = {
    implicit val playerHeadSkinUrlFetcher: PlayerHeadSkinUrlFetcher[F] =
      new PlayerHeadSkinUrlFetcherByMojangAPI[F]
    val repository = new PlayerHeadUrlRepository[F]

    new System[F, Player] {
      override val api: PlayerHeadSkinAPI[F, Player] = (player: Player) =>
        repository.readUrl(player.getName)
    }
  }

}
