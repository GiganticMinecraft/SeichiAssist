package com.github.unchama.menuinventory.router

import cats.data.Kleisli
import com.github.unchama.menuinventory.Menu
import org.bukkit.entity.Player

/**
 * `F` の文脈で `M` をプレーヤーに開かせる作用を提供するtrait。
 */
trait CanOpen[F[_], M <: Menu] {

  def open(menu: M): Kleisli[F, Player, Unit]

}
