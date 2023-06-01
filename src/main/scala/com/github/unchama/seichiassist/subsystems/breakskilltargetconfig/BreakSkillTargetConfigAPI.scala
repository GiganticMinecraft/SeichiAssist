package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.BreakSkillTargetConfigKey

trait BreakSkillTargetConfigAPI[F[_], Player] {

  /**
   * @return 破壊フラグをトグルする作用
   */
  def toggleBreakSkillTargetConfig(
    breakFlagName: BreakSkillTargetConfigKey
  ): Kleisli[F, Player, Unit]

  /**
   * @return 現在の破壊フラグを取得する作用
   */
  def breakSkillTargetConfig(
    player: Player,
    breakFlagName: BreakSkillTargetConfigKey
  ): F[Boolean]

}

object BreakSkillTargetConfigAPI {

  def apply[F[_], Player](
    implicit ev: BreakSkillTargetConfigAPI[F, Player]
  ): BreakSkillTargetConfigAPI[F, Player] = ev

}
