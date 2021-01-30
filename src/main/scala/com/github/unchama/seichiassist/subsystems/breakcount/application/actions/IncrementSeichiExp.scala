package com.github.unchama.seichiassist.subsystems.breakcount.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

trait IncrementSeichiExp[F[_], Player] {

  def of(player: Player, by: SeichiExpAmount): F[Unit]

}

object IncrementSeichiExp {

  def apply[F[_], Player](implicit ev: IncrementSeichiExp[F, Player]): IncrementSeichiExp[F, Player] = ev

  def using[
    F[_]
    : Sync
    : ClassifyPlayerWorld[*[_], Player],
    G[_] : Effect : ContextCoercion[F, *[_]],
    Player
  ](dataRepository: KeyedDataRepository[Player, Ref[F, SeichiAmountData]]): IncrementSeichiExp[F, Player] =
    (player, by) => {
      val F: Monad[F] = implicitly

      F.ifM(ClassifyPlayerWorld[F, Player].isInSeichiCountingWorld(player))(
        dataRepository(player).update(_.addExpAmount(by)),
        F.unit
      )
    }
}
