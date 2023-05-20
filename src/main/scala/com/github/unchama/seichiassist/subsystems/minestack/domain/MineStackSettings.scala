package com.github.unchama.seichiassist.subsystems.minestack.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.PlayerSettingPersistence

class MineStackSettings[F[_]: Sync, Player: HasUuid](player: Player)(
  implicit playerSettingPersistence: PlayerSettingPersistence[F]
) {

  import cats.implicits._

  private val autoMineStack: F[Ref[F, Boolean]] = for {
    settingState <- playerSettingPersistence.autoMineStackState(HasUuid[Player].of(player))
    reference <- Ref.of(settingState)
  } yield reference

  /**
   * @return 自動収集を有効にする作用
   */
  def turnOnAutoCollect: F[Unit] = for {
    _ <- playerSettingPersistence.turnOnAutoMineStack(HasUuid[Player].of(player))
    reference <- autoMineStack
  } yield reference.set(true)

  /**
   * @return 自動収集を無効にする作用
   */
  def turnOffAutoCollect: F[Unit] = for {
    _ <- playerSettingPersistence.turnOffAutoMineStack(HasUuid[Player].of(player))
    reference <- autoMineStack
  } yield reference.set(false)

  /**
   * 現在自動収集が有効になっているかを取得します
   * @return 有効になっていればtrue、なっていなければfalseを返す作用
   */
  def isAutoCollectionTurnedOn: F[Boolean] = for {
    reference <- autoMineStack
    state <- reference.get
  } yield state

}
