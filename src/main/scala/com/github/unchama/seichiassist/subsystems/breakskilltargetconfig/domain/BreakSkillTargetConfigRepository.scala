package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain

trait BreakSkillTargetConfigRepository[F[_], Player] {

  /**
   * @return `configKey`の破壊フラグをトグルする作用
   */
  def toggleBreakSkillTargetConfig(
    player: Player,
    configKey: BreakSkillTargetConfigKey
  ): F[Unit]

  /**
   * @return `player`の現在の破壊フラグを取得する作用
   */
  def breakSkillTargetConfig(player: Player, configKey: BreakSkillTargetConfigKey): F[Boolean]

}
