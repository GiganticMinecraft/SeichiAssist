package com.github.unchama.seichiassist.subsystems.gachapoint

import cats.data.Kleisli
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint

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
  def addGachaPoint(point: GachaPoint): Kleisli[F, Player, Unit]

  /**
   * @return プレーヤーのガチャポイントを減らす作用。
   */
  def subtractGachaPoint(point: GachaPoint): Kleisli[F, Player, Unit]

}
