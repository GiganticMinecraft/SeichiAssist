package com.github.unchama.seichiassist.subsystems.buildcount.application.actions

import com.github.unchama.seichiassist.subsystems.buildcount.application.BuildExpMultiplier
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount

trait IncrementBuildExpWhenBuiltWithSkill[F[_], Player] {

  def of(player: Player): F[Unit] = of(player, BuildExpAmount.ofNonNegative(1))

  def of(player: Player, by: BuildExpAmount): F[Unit]

}

object IncrementBuildExpWhenBuiltWithSkill {

  def apply[F[_]: [f[_]] =>> IncrementBuildExpWhenBuiltWithSkill[f, Player], Player]
    : IncrementBuildExpWhenBuiltWithSkill[F, Player] = implicitly

  def withConfig[F[_]: [f[_]] =>> IncrementBuildExpWhenBuiltByHand[f, Player], Player](
    config: BuildExpMultiplier
  ): IncrementBuildExpWhenBuiltWithSkill[F, Player] =
    (player: Player, by: BuildExpAmount) =>
      IncrementBuildExpWhenBuiltByHand[F, Player]
        .of(player, by.mapAmount(_ * config.withBuildSkills))

}
