package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain.BreakSkillTriggerConfigKey

trait BreakSkillTriggerConfigAPI[F[_], Player] {

  /**
   * @return 破壊トリガをトグルする作用
   */
  def toggleBreakSkillTriggerConfig(
    configKey: BreakSkillTriggerConfigKey
  ): Kleisli[F, Player, Unit]

  /**
   * @return 現在の破壊トリガを取得する作用
   */
  def breakSkillTriggerConfig(player: Player, configKey: BreakSkillTriggerConfigKey): F[Boolean]

}

object BreakSkillTriggerConfigAPI {

  def apply[F[_], Player](
    implicit ev: BreakSkillTriggerConfigAPI[F, Player]
  ): BreakSkillTriggerConfigAPI[F, Player] = ev

}
