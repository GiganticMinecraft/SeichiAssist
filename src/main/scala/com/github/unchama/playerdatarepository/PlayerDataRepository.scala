package com.github.unchama.playerdatarepository

import org.bukkit.entity.Player

/**
 * ログイン中の[[Player]]に対して必ず `R` を返せるようなリポジトリのSAM trait.
 *
 * どの期間についてプレーヤーに対して値を返せるかどうかは実装に依存するが、
 * 遅くても `PlayerJoinEvent` の `EventPriority.HIGH` から、
 * 早くても `PlayerQuitEvent` の `EventPriority.HIGHEST` まではデータが存在することが保証されている。
 *
 * プレーヤーがログインしている間は、[[Player]]を適用して得られる値が不変であることを保証する。
 *
 * @tparam R レポジトリが [[Player]] に関連付ける値の型
 */
trait PlayerDataRepository[R] {

  def apply(player: Player): R

}
