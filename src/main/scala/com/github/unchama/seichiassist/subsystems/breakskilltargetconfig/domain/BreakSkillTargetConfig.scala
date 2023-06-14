package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain

case class BreakSkillTargetConfig(config: Map[BreakSkillTargetConfigKey, Boolean]) {

  /**
   * @return `configKey`の破壊フラグをトグルする作用
   */
  def toggleBreakSkillTargetConfig(
    configKey: BreakSkillTargetConfigKey
  ): BreakSkillTargetConfig =
    this.copy(
      this.config.filterNot(_._1 == configKey) ++ Map(
        configKey -> this.config.getOrElse(configKey, false)
      )
    )

  /**
   * @return 現在の破壊フラグを取得する作用
   */
  def breakSkillTargetConfig(configKey: BreakSkillTargetConfigKey): Boolean =
    this.config.getOrElse(configKey, false)

}

object BreakSkillTargetConfig {

  /**
   * [[BreakSkillTargetConfig]]の初期値
   */
  val initial: BreakSkillTargetConfig = BreakSkillTargetConfig(Map.empty)

}
