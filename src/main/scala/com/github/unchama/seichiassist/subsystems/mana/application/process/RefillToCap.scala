package com.github.unchama.seichiassist.subsystems.mana.application.process

import cats.effect.concurrent.Ref
import cats.implicits._
import cats.{Functor, Monad}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.{ContextCoercion, Diff}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.mana.domain.LevelCappedManaAmount

/**
 * 整地スターレベルが上がったときの処理が記述されたストリームを格納するオブジェクト
 */
object RefillToCap {

  def using[F[_]: Functor, G[_]: Monad: ContextCoercion[*[_], F], Player](
    repository: KeyedDataRepository[Player, Ref[G, LevelCappedManaAmount]]
  )(implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): fs2.Stream[F, Unit] = {
    breakCountReadAPI.seichiStarLevelUpdates.evalMap {
      case (player, Diff(_, _)) =>
        ContextCoercion {
          repository
            .lift(player)
            .traverse {
              _.update(_.fillToCap)
            }
            .as(())
        }
    }
  }

}
