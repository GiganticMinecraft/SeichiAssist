package com.github.unchama.seichiassist.subsystems.playerheadskin

import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasName.instance
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.playerheadskin.application.PlayerHeadUrlRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.{
  HeadSkinUrl,
  PlayerHeadSkinUrlFetcher
}
import com.github.unchama.seichiassist.subsystems.playerheadskin.infrastructure.PlayerHeadSkinUrlFetcherByMojangAPI
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {
  val api: PlayerHeadSkinAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: Sync, G[_]: SyncEffect: ContextCoercion[*[_], F]]: F[System[F, Player]] = {
    implicit val playerHeadSkinUrlFetcher: PlayerHeadSkinUrlFetcher[G] =
      new PlayerHeadSkinUrlFetcherByMojangAPI[G]

    for {
      playerHeadUrlRepositoryControls <- ContextCoercion(
        BukkitRepositoryControls.createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              PlayerHeadUrlRepositoryDefinition.initialization[G, Player],
              PlayerHeadUrlRepositoryDefinition.finalization[G, Player]
            )
        )
      )
    } yield {
      new System[F, Player] {
        override val api: PlayerHeadSkinAPI[F, Player] = new PlayerHeadSkinAPI[F, Player] {
          override def playerHeadSkinUrl(player: Player): F[Option[HeadSkinUrl]] =
            ContextCoercion(playerHeadUrlRepositoryControls.repository(player).readUrl)
        }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          playerHeadUrlRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }

}
