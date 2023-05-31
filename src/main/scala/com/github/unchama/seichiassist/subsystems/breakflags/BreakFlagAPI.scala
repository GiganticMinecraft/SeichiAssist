package com.github.unchama.seichiassist.subsystems.breakflags

import com.github.unchama.seichiassist.subsystems.breakflags.domain.BreakFlag

import java.util.UUID

trait BreakFlagAPI[F[_]] {

  /**
   * @return 破壊フラグを有効にする作用
   */
  def turnOnBreakFlag(player: UUID, breakFlag: BreakFlag): F[Unit]

  /**
   * @return 破壊フラグを無効にする作用
   */
  def turnOffBreakFlag(player: UUID, breakFlag: BreakFlag): F[Unit]

  /**
   * @return 現在の破壊フラグを取得する作用
   */
  def breakFlag(player: UUID): F[BreakFlag]

}

object BreakFlagAPI {

  def apply[F[_]](implicit ev: BreakFlagAPI[F]): BreakFlagAPI[F] = ev

}
