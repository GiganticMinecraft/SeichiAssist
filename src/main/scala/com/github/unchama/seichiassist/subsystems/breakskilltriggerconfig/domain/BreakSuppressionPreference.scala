package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain

case class BreakSuppressionPreference(doBreakSuppression: Boolean) {

  /**
   * @return 破壊抑制の設定をトグルする
   */
  def toggleBreakSuppression(): BreakSuppressionPreference =
    this.copy(doBreakSuppression = !this.doBreakSuppression)

  /**
   * @return 現在の破壊抑制の設定を取得する
   */
  def isBreakSuppressionEnabled: Boolean = doBreakSuppression
}

object BreakSuppressionPreference {

  /**
   * [[BreakSuppressionPreference]]の初期値
   */
  val initial: BreakSuppressionPreference = BreakSuppressionPreference(doBreakSuppression = false)
}
