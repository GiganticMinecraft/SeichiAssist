package com.github.unchama.menuinventory

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.entity.Player

/**
 * 「メニュー」のtrait.
 *
 * このtraitを実装するオブジェクトは, インベントリ上で展開される意味づけされたUIの情報を持っている.
 * これらのUIをメニューインベントリ, または単にメニューと呼ぶこととする.
 */
trait Menu {

  /**
   * メニューのサイズとタイトルに関する情報
   */
  val frame: InventoryFrame

  /**
   * @return `player`からメニューの[[IndexedSlotLayout]]を計算する[[IO]]
   */
  def computeMenuLayout(player: Player): IO[IndexedSlotLayout]

  /**
   * メニューを[Player]に開かせる[TargetedEffect].
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