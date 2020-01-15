package com.github.unchama.menuinventory

import cats.data
import cats.effect.IO
import com.github.unchama.concurrent.BukkitSyncExecutionContext
import com.github.unchama.menuinventory.Types.LayoutPreparationContext
import com.github.unchama.targetedeffect.TargetedEffect
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
  val frame: MenuFrame

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  def computeMenuLayout(player: Player): IO[MenuSlotLayout]

  /**
   * メニューを[Player]に開かせる[TargetedEffect].
   */
  def open(implicit ctx: LayoutPreparationContext, syncCtx: BukkitSyncExecutionContext): TargetedEffect[Player] = data.Kleisli { player =>
    for {
      session <- frame.createNewSession()
      _ <- session.openInventory.run(player)
      layout <- computeMenuLayout(player)
      _ <- session.overwriteViewWith(layout)
    } yield ()
  }

}