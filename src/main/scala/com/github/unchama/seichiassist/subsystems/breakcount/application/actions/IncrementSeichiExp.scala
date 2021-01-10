package com.github.unchama.seichiassist.subsystems.breakcount.application.actions

import cats.Monad
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.{Diff, RefExtra}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.ClassifyPlayerWorld

trait IncrementSeichiExp[F[_], Player] {

  def of(player: Player, by: SeichiExpAmount): F[Unit]

}

object IncrementSeichiExp {

  import cats.implicits._

  def apply[F[_], Player](implicit ev: IncrementSeichiExp[F, Player]): IncrementSeichiExp[F, Player] = ev

  def using[
    F[_]
    : Monad
    : ClassifyPlayerWorld[*[_], Player]
    : NotifyLevelUp[*[_], Player],
    Player
  ](dataRepository: KeyedDataRepository[Player, Ref[F, SeichiAmountData]]): IncrementSeichiExp[F, Player] =
    (player, by) => {
      for {
        dataPair <- RefExtra.getAndUpdateAndGet(dataRepository(player))(_.addExpAmount(by))
        _ <- Diff
          .ofPairBy(dataPair)(_.levelCorrespondingToExp)
          .traverse(NotifyLevelUp[F, Player].ofSeichiLevelTo(player))
        _ <- Diff
          .ofPairBy(dataPair)(_.starLevelCorrespondingToExp)
          .traverse(NotifyLevelUp[F, Player].ofSeichiStarLevelTo(player))
      } yield ()
    }
}
