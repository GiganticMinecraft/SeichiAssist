package com.github.unchama.seichiassist.subsystems.buildcount

import cats.implicits.toFunctorFilterOps
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.Diff
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.{
  IncrementBuildExpWhenBuiltByHand,
  IncrementBuildExpWhenBuiltWithSkill
}
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildLevel
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

trait BuildCountAPI[F[_], G[_], Player] {

  implicit val incrementBuildExpWhenBuiltByHand: IncrementBuildExpWhenBuiltByHand[G, Player]

  implicit val incrementBuildExpWhenBuiltWithSkill: IncrementBuildExpWhenBuiltWithSkill[
    G,
    Player
  ]

  implicit val playerBuildAmountRepository: KeyedDataRepository[
    Player,
    ReadOnlyRef[G, BuildAmountData]
  ]

  val buildAmountUpdates: fs2.Stream[F, (Player, BuildAmountData)]

  final lazy val buildAmountUpdateDiffs: fs2.Stream[F, (Player, Diff[BuildAmountData])] =
    StreamExtra.keyedValueDiffs(buildAmountUpdates)

  final lazy val buildLevelUpdates: fs2.Stream[F, (Player, Diff[BuildLevel])] = {
    buildAmountUpdateDiffs.mapFilter {
      case (player, Diff(left, right)) =>
        Diff
          .fromValues(left.levelCorrespondingToExp, right.levelCorrespondingToExp)
          .map((player, _))
    }
  }

}
