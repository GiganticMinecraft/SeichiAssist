package com.github.unchama.seichiassist.subsystems.everywhereender

import cats.data.Kleisli
import org.bukkit.entity.Player

trait EverywhereEnderChestAPI[F[_]] {
  /**
   * どこでもエンダーチェストにアクセスできるかどうかを計算する作用を返す。
   * @return どこでもエンダーチェストにアクセスできるかどうかを計算する作用
   */
  def canAccessEverywhereEnderChest(player: Player): F[Boolean]

  /**
   * [[canAccessEverywhereEnderChest]]が
   *   - `false`を返す場合はエラーメッセージを表示する。
   *   - `true`を返す場合はどこでもエンダーチェストを開ける。
   * @return 上記したような作用を記述する[[Kleisli]]
   */
  def openEnderChestOrError(player: Player): Kleisli[F, Player, Unit]
}
