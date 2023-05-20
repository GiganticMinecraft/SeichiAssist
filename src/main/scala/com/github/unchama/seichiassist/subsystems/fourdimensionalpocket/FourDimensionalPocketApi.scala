package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket

import cats.data.Kleisli
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize

trait FourDimensionalPocketApi[F[_], Player] {

  /**
   * プレーヤーに関連付けられた四次元ポケットをプレーヤーに開かせる作用。
   *
   * プレーヤーが四次元ポケットへのアクセス権を（整地レベル制約により）持っていない場合、 この作用による副作用は一切起こらない。
   */
  val openPocketInventory: Kleisli[F, Player, Unit]

  /**
   * プレーヤーの四次元ポケットのサイズを提供する [[KeyedDataRepository]]。
   */
  val currentPocketSize: KeyedDataRepository[Player, ReadOnlyRef[F, PocketSize]]

}
