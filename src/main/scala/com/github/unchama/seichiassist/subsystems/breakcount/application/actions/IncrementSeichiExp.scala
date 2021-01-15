package com.github.unchama.seichiassist.subsystems.breakcount.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.{ContextCoercion, Diff, RefExtra}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import fs2.concurrent.Topic

trait IncrementSeichiExp[F[_], Player] {

  def of(player: Player, by: SeichiExpAmount): F[Unit]

}

object IncrementSeichiExp {

  import cats.implicits._

  def apply[F[_], Player](implicit ev: IncrementSeichiExp[F, Player]): IncrementSeichiExp[F, Player] = ev

  def using[
    F[_]
    : Sync
    : ClassifyPlayerWorld[*[_], Player]
    : NotifyLevelUp[*[_], Player],
    G[_] : Effect : ContextCoercion[F, *[_]],
    Player
  ](dataRepository: KeyedDataRepository[Player, Ref[F, SeichiAmountData]],
    topic: Topic[G, Option[(Player, Diff[SeichiAmountData])]]): IncrementSeichiExp[F, Player] =
    (player, by) => {
      val F: Monad[F] = implicitly

      F.ifM(ClassifyPlayerWorld[F, Player].isInSeichiCountingWorld(player))(
        for {
          dataPair <- RefExtra.getAndUpdateAndGet(dataRepository(player))(_.addExpAmount(by))
          _ <- EffectExtra.runAsyncAndForget[G, F, Option[Unit]] {
            Diff
              .ofPair(dataPair)
              .traverse(diff => topic.publish1(Some(player, diff)))
          }
          _ <- Diff
            .ofPairBy(dataPair)(_.levelCorrespondingToExp)
            .traverse(NotifyLevelUp[F, Player].ofSeichiLevelTo(player))
          _ <- Diff
            .ofPairBy(dataPair)(_.starLevelCorrespondingToExp)
            .traverse(NotifyLevelUp[F, Player].ofSeichiStarLevelTo(player))
        } yield (),
        F.unit
      )
    }
}
