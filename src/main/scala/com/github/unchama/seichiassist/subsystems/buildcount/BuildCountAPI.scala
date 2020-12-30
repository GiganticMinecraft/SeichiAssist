package com.github.unchama.seichiassist.subsystems.buildcount

import com.github.unchama.concurrent.ReadOnlyRef
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.{IncrementBuildExpWhenBuiltByHand, IncrementBuildExpWhenBuiltWithSkill}
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

trait BuildCountAPI[F[_], Player] {

  val incrementBuildExpWhenBuiltByHand: IncrementBuildExpWhenBuiltByHand[F, Player]

  val incrementBuildExpWhenBuiltWithSkill: IncrementBuildExpWhenBuiltWithSkill[F, Player]

  val playerBuildAmountRepository: KeyedDataRepository[Player, ReadOnlyRef[F, BuildAmountData]]

}
