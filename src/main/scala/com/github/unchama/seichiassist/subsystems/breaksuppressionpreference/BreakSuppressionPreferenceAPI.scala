package com.github.unchama.seichiassist.subsystems.breaksuppressionpreference

import cats.data.Kleisli

trait BreakSuppressionPreferenceAPI[F[_], Player] {

  /**
   * @return 破壊抑制の設定をトグルする作用
   */
  def toggleBreakSuppression: Kleisli[F, Player, Unit]

  /**
   * @return 現在の破壊抑制の設定を取得する作用
   */
  def isBreakSuppressionEnabled(player: Player): F[Boolean]

}

object BreakSkillTriggerConfigAPI {

  def apply[F[_], Player](
    implicit ev: BreakSuppressionPreferenceAPI[F, Player]
  ): BreakSuppressionPreferenceAPI[F, Player] = ev

}
