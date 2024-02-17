package com.github.unchama.seichiassist.subsystems.playerheadskin.application

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.minecraft.algebra.HasName
import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.{
  PlayerHeadSkinUrlFetcher,
  PlayerHeadUrlRepository
}

object PlayerHeadUrlRepositoryDefinition {

  import cats.implicits._

  def initialization[F[_]: Sync, Player: HasName](
    implicit playerHeadSkinUrlFetcher: PlayerHeadSkinUrlFetcher[F]
  ): TwoPhasedRepositoryInitialization[F, Player, PlayerHeadUrlRepository[F]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, PlayerHeadUrlRepository[F]] { player =>
        for {
          playerHeadSkinUrl <-
            playerHeadSkinUrlFetcher.fetchHeadSkinUrl(HasName[Player].of(player))

        } yield new PlayerHeadUrlRepository[F](playerHeadSkinUrl)
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, PlayerHeadUrlRepository[F]] =
    RepositoryFinalization.trivial

}
