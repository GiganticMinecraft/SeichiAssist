package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.PlayerSettingPersistence

class MineStackSettings[F[_]: Sync, Player](player: Player)(
  implicit playerSettingPersistence: Player => PlayerSettingPersistence[F]
) {

  import cats.implicits._

  private val autoMineStack: F[Ref[F, Boolean]] = for {
    settingState <- playerSettingPersistence(player).autoMineStackState
    reference <- Ref.of(settingState)
  } yield reference

  /**
   * @return 自動収集を有効にする作用
   */
  def toggleAutoMineStackTurnOn: F[Unit] = for {
    _ <- playerSettingPersistence(player).turnOnAutoMineStack
    reference <- autoMineStack
  } yield reference.set(true)

  /**
   * @return 自動収集を無効にする作用
   */
  def toggleAutoMineStackTurnOff: F[Unit] = for {
    _ <- playerSettingPersistence(player).turnOffAutoMineStack
    reference <- autoMineStack
  } yield reference.set(false)

  /**
   * 現在自動収集が有効になっているかを取得します
   * @return 有効になっていればtrue、なっていなければfalseを返す作用
   */
  def currentState: F[Boolean] = for {
    reference <- autoMineStack
    state <- reference.get
  } yield state

}
