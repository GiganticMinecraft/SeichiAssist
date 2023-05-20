package com.github.unchama.seichiassist.subsystems.anywhereender

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.anywhereender.domain.AnywhereEnderAccessPermitted
import org.bukkit.entity.Player

trait AnywhereEnderChestAPI[F[_]] {

  /**
   * 与えられたプレーヤーがどこでもエンダーチェストにアクセスできるかどうかを確認する作用。
   */
  def canAccessAnywhereEnderChest(player: Player): F[AnywhereEnderAccessPermitted]

  /**
   * [[canAccessAnywhereEnderChest]]が
   *   - `Left` を返す場合はエラーメッセージを表示し。
   *   - `true`を返す場合はどこでもエンダーチェストを開ける。
   *
   * @return
   *   上記したような作用を記述する[[Kleisli]]
   */
  def openEnderChestOrNotifyInsufficientLevel: Kleisli[F, Player, AnywhereEnderAccessPermitted]

}
