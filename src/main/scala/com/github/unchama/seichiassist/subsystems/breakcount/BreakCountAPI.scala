package com.github.unchama.seichiassist.subsystems.breakcount

import cats.Monad
import cats.effect.{Concurrent, Timer}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.Diff
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.IncrementSeichiExp
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{
  SeichiExpAmount,
  SeichiLevel,
  SeichiStarLevel
}
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{
  BatchedSeichiExpMap,
  SeichiAmountData
}

import java.util.UUID
import scala.concurrent.duration.FiniteDuration

trait BreakCountWriteAPI[G[_], Player] {

  /**
   * プレーヤーの整地量データを増加させるアクション
   */
  val incrementSeichiExp: IncrementSeichiExp[G, Player]
}

trait BreakCountReadAPI[F[_], G[_], Player] {

  import cats.implicits._

  protected implicit val _GMonad: Monad[G]

  /**
   * プレーヤーの整地量データの読み取り専用リポジトリ
   */
  val seichiAmountDataRepository: KeyedDataRepository[Player, ReadOnlyRef[G, SeichiAmountData]]

  /**
   * プレーヤーの整地レベルの読み取り専用リポジトリ
   */
  final lazy val seichiLevelRepository
    : KeyedDataRepository[Player, ReadOnlyRef[G, SeichiLevel]] =
    seichiAmountDataRepository.map(_.map(_.levelCorrespondingToExp))

  /**
   * プレーヤーの永続化された整地量データの読み取り専用リポジトリ。
   *
   * このリポジトリは統計量表示等には利用できるが、 最新の整地量データは [[seichiAmountDataRepository]] より取得すること。
   */
  val persistedSeichiAmountDataRepository: UUID => ReadOnlyRef[G, Option[SeichiAmountData]]

  /**
   * プレーヤーの整地量データの最新値が流れる [[fs2.Stream]]。
   */
  val seichiAmountUpdates: fs2.Stream[F, (Player, SeichiAmountData)]

  /**
   * プレーヤーの整地量データの差分が流れる [[fs2.Stream]]。
   */
  final lazy val seichiAmountUpdateDiffs: fs2.Stream[F, (Player, Diff[SeichiAmountData])] =
    StreamExtra.keyedValueDiffs(seichiAmountUpdates)

  /**
   * プレーヤーの整地レベルの更新差分が流れる [[fs2.Stream]]
   */
  final lazy val seichiLevelUpdates: fs2.Stream[F, (Player, Diff[SeichiLevel])] =
    seichiAmountUpdateDiffs.mapFilter {
      case (player, Diff(left, right)) =>
        Diff
          .fromValues(left.levelCorrespondingToExp, right.levelCorrespondingToExp)
          .map((player, _))
    }

  final lazy val seichiStarLevelUpdates: fs2.Stream[F, (Player, Diff[SeichiStarLevel])] =
    seichiAmountUpdateDiffs.mapFilter {
      case (player, Diff(left, right)) =>
        Diff
          .fromValues(left.starLevelCorrespondingToExp, right.starLevelCorrespondingToExp)
          .map((player, _))
    }

  /**
   * プレーヤーの整地量データの増加分が流れる [[fs2.Stream]]。
   */
  final lazy val seichiAmountIncreases: fs2.Stream[F, (Player, SeichiExpAmount)] =
    seichiAmountUpdateDiffs.map {
      case (player, Diff(oldData, newData)) =>
        val expDiff =
          SeichiExpAmount.orderedMonus.subtractTruncate(newData.expAmount, oldData.expAmount)
        (player, expDiff)
    }

  /**
   * `duration` 毎に纏められた、プレーヤーの整地量増加を流すストリーム。
   */
  def batchedIncreases(duration: FiniteDuration)(
    implicit FTimer: Timer[F],
    FConcurrent: Concurrent[F]
  ): fs2.Stream[F, BatchedSeichiExpMap[Player]] =
    StreamExtra.foldGate(
      seichiAmountIncreases,
      fs2.Stream.awakeEvery[F](duration),
      BatchedSeichiExpMap.empty[Player]
    )(_.combine)

}

trait BreakCountAPI[F[_], G[_], Player]
    extends BreakCountWriteAPI[G, Player]
    with BreakCountReadAPI[F, G, Player]
