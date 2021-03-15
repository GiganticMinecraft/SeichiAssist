package com.github.unchama.seichiassist.subsystems.gachapoint

import cats.data.Kleisli
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings.GachaTicketReceivingSettings

trait GachaPointSettingsApi[G[_], Player] {

  /**
   * プレーヤーの現在のガチャ券受け取り設定を保持するデータリポジトリ。
   */
  val ticketReceivingSettings: KeyedDataRepository[Player, ReadOnlyRef[G, GachaTicketReceivingSettings]]

  /**
   * プレーヤーのガチャ券受け取り設定をトグルする作用。
   */
  val toggleTicketReceivingSettings: Kleisli[G, Player, Unit]

}

trait GachaPointApi[F[_], G[_], Player] {

  /**
   * プレーヤーのガチャポイントを保持するデータリポジトリ。
   */
  val gachaPoint: KeyedDataRepository[Player, ReadOnlyRef[G, GachaPoint]]

  /**
   * プレーヤーのガチャポイントをガチャ券に変換して一括で受け取る作用。
   */
  val receiveBatch: Kleisli[F, Player, Unit]

  /**
   * プレーヤーのガチャポイントを増やす作用。
   */
  def addGachaPoint(point: GachaPoint): Kleisli[G, Player, Unit]

}
