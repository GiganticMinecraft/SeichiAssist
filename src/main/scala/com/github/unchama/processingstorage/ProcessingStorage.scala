package com.github.unchama.processingstorage

import cats.data
import cats.effect.IO
import com.github.unchama.menuinventory.{LayoutPreparationContext, MenuFrame, MenuSession, MenuSlotLayout}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

/**
 * processing-storageのトレイト。
 *
 * このトレイトを実装するオブジェクトは、Bukkitの[[org.bukkit.inventory.Inventory]]を入力とした処理を行う作用を持っている。
 * これらのオブジェクトを、processing-storageと呼ぶ。
 */
trait ProcessingStorage {
  type Environment

  val frame: MenuFrame

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  def doOperation(player: Player)(implicit environment: Environment): IO[MenuSlotLayout]

  /**
   * メニューを[Player]に開かせる[TargetedEffect].
   */
  def open(implicit environment: Environment,
           ctx: LayoutPreparationContext,
           onMainThread: OnMinecraftServerThread[IO]): TargetedEffect[Player] = data.Kleisli { player =>
    for {
      session <- MenuSession.createNewSessionWith[IO](frame)
      _ <- session.openInventory.run(player)
      _ <- IO.shift(ctx)
      layout <- doOperation(player)
      _ <- session.overwriteViewWith(layout)
    } yield ()
  }
}
