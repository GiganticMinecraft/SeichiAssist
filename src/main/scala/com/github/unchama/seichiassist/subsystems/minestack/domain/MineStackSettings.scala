package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class MineStackSettings[F[_]: Sync, Player](player: Player)(
  implicit playerSettingPersistence: Player => PlayerSettingPersistence[F]
) {

  import cats.implicits._

  private val autoMineStack: F[Ref[F, Boolean]] = for {
    settingState <- playerSettingPersistence(player).autoMineStackState
    reference <- Ref.of(settingState)
  } yield reference

  /**
   * @return AutoMineStackをonに切り替えます
   */
  def toggleAutoMineStackTurnOn: F[Unit] = for {
    _ <- playerSettingPersistence(player).turnOnAutoMineStack
    reference <- autoMineStack
  } yield reference.set(true)

  /**
   * @return AutoMineStackをoffに切り替えます
   */
  def toggleAutoMineStackTurnOff: F[Unit] = for {
    _ <- playerSettingPersistence(player).turnOffAutoMineStack
    reference <- autoMineStack
  } yield reference.set(false)

  /**
   * @return 現在のステータスを取得します
   */
  def currentState: F[Boolean] = for {
    reference <- autoMineStack
    state <- reference.get
  } yield state

}
