package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain

case class BreakSkillTriggerConfig(config: Map[BreakSkillTriggerConfigKey, Boolean]) {

  /**
   * @return `configKey`の破壊トリガをトグルする
   */
  def toggleBreakSkillTriggerConfig(
    configKey: BreakSkillTriggerConfigKey
  ): BreakSkillTriggerConfig =
    this.copy(this.config + (configKey -> !this.config.getOrElse(configKey, false)))

  /**
   * @return 現在の破壊トリガを取得する
   */
  def breakSkillTriggerConfig(configKey: BreakSkillTriggerConfigKey): Boolean =
    this.config.getOrElse(configKey, false)

}

object BreakSkillTriggerConfig {

  /**
   * [[BreakSkillTriggerConfig]]の初期値
   */
  val initial: BreakSkillTriggerConfig = BreakSkillTriggerConfig(Map.empty)

}
