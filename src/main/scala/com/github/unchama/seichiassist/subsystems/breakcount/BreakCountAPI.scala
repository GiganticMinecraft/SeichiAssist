package com.github.unchama.seichiassist.subsystems.breakcount

import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.Diff
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.IncrementSeichiExp
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{SeichiExpAmount, SeichiLevel, SeichiStarLevel}

trait BreakCountWriteAPI[G[_], Player] {
  /**
   * プレーヤーの整地量データを増加させるアクション
   */
  val incrementSeichiExp: IncrementSeichiExp[G, Player]
}

trait BreakCountReadAPI[F[_], G[_], Player] {
  /**
   * プレーヤーの整地量データの読み取り専用リポジトリ
   */
  val seichiAmountDataRepository: KeyedDataRepository[Player, ReadOnlyRef[G, SeichiAmountData]]

  /**
   * プレーヤーの整地量データの最新値が流れる [[fs2.Stream]]。
   */
  val seichiAmountUpdates: fs2.Stream[F, (Player, SeichiAmountData)]

  /**
   * プレーヤーの整地量データの差分が流れる [[fs2.Stream]]。
   */
  final lazy val seichiAmountUpdateDiffs: fs2.Stream[F, (Player, Diff[SeichiAmountData])] =
    StreamExtra.keyedValueDiffs(seichiAmountUpdates)

  import cats.implicits._

  /**
   * プレーヤーの整地レベルの更新差分が流れる [[fs2.Stream]]
   */
  final lazy val seichiLevelUpdates: fs2.Stream[F, (Player, Diff[SeichiLevel])] =
    seichiAmountUpdateDiffs.mapFilter { case (player, Diff(left, right)) =>
      Diff
        .fromValues(left.levelCorrespondingToExp, right.levelCorrespondingToExp)
        .map((player, _))
    }

  final lazy val seichiStarLevelUpdates: fs2.Stream[F, (Player, Diff[SeichiStarLevel])] =
    seichiAmountUpdateDiffs.mapFilter { case (player, Diff(left, right)) =>
      Diff
        .fromValues(left.starLevelCorrespondingToExp, right.starLevelCorrespondingToExp)
        .map((player, _))
    }

  /**
   * プレーヤーの整地量データの増加分が流れる [[fs2.Stream]]。
   */
  final lazy val seichiAmountIncreases: fs2.Stream[F, (Player, SeichiExpAmount)] =
    seichiAmountUpdateDiffs.map { case (player, Diff(oldData, newData)) =>
      val expDiff = SeichiExpAmount.orderedMonus.subtractTruncate(newData.expAmount, oldData.expAmount)
      (player, expDiff)
    }
}

trait BreakCountAPI[F[_], G[_], Player] extends BreakCountWriteAPI[G, Player] with BreakCountReadAPI[F, G, Player]
