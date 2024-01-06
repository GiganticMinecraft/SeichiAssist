package com.github.unchama.seichiassist.subsystems.mana.application.process

import cats.effect.concurrent.Ref
import cats.Monad
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.{ContextCoercion, Diff}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.mana.domain.LevelCappedManaAmount

object UpdateManaCaps {

  import cats.implicits._

  def using[F[_], G[_]: Monad: ContextCoercion[*[_], F], Player](
    repository: KeyedDataRepository[Player, Ref[G, LevelCappedManaAmount]]
  )(implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): fs2.Stream[F, Unit] =
    breakCountReadAPI.seichiLevelUpdates.evalMap {
      case (player, Diff(_, newLevel)) =>
        ContextCoercion {
          repository
            .lift(player)
            .traverse { ref => ref.updateMaybe(_.withHigherLevelOption(newLevel)) }
            .as(())
        }
    }

}
