package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class MineStackSettings[F[_]: Sync] {

  private val autoMineStack: Ref[F, Boolean] = Ref.unsafe(false)

  /**
   * @return AutoMineStackをonに切り替えます
   */
  def toggleAutoMineStackTurnOn: F[Unit] = autoMineStack.set(true)

  /**
   * @return AutoMineStackをoffに切り替えます
   */
  def toggleAutoMineStackTurnOff: F[Unit] = autoMineStack.set(false)

  /**
   * @return 現在のステータスを取得します
   */
  def currentState: F[Boolean] = autoMineStack.get

}
