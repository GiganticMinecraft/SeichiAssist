package com.github.unchama.seichiassist.subsystems.breakflags

import com.github.unchama.seichiassist.subsystems.breakflags.domain.{BreakFlag, BreakFlagName}

trait BreakFlagAPI[F[_], Player] {

  /**
   * @return 破壊フラグを有効にする作用
   */
  def turnOnBreakFlag(player: Player, breakFlagName: BreakFlagName): F[Unit]

  /**
   * @return 破壊フラグを無効にする作用
   */
  def turnOffBreakFlag(player: Player, breakFlagName: BreakFlagName): F[Unit]

  /**
   * @return 現在の破壊フラグを取得する作用
   */
  def breakFlag(player: Player, breakFlagName: BreakFlagName): F[Boolean]

}

object BreakFlagAPI {

  def apply[F[_], Player](implicit ev: BreakFlagAPI[F, Player]): BreakFlagAPI[F, Player] = ev

}
