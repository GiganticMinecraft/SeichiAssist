package com.github.unchama.menuinventory

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.entity.Player

/**
 * メニュー一つに対応するオブジェクトへの抽象インターフェース
 */
trait Menu {

  val frame: InventoryFrame

  def computeMenuLayout(player: Player): IO[IndexedSlotLayout]

  /**
   * オブジェクトが表すメニューを[Player]に開かせる[TargetedEffect].
   */
  val open: TargetedEffect[Player] = { player =>
    for {
      session <- frame.createNewSession()
      _ <- session.openInventory(player)
      layout <- computeMenuLayout(player)
      _ <- session.overwriteViewWith(layout)
    } yield ()
  }

}