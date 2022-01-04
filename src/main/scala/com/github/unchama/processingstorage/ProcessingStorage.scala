package com.github.unchama.processingstorage

import cats.data
import cats.effect.IO
import com.github.unchama.menuinventory.{LayoutPreparationContext, MenuFrame, MenuSession}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
   * インベントリを閉じたときに発火される作用。
   * @return `inventory`から変換されたアイテムのリストを計算する[[IO]]
   */
  def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: Environment): IO[List[ItemStack]]

  /**
   *
   * @param itemStack 変換する前のアイテム
   * @return 返す対象である場合変換後の[[ItemStack]]を包んだ[[Some]]、返さない場合[[None]]を返す[[IO]]。
   */
  def doMap(itemStack: ItemStack): IO[Option[ItemStack]]

  /**
   * @return 全ての変換が終わり、アイテム付与が終わったあとに発火されるプレイヤーに対するエフェクト。
   */
  def postEffect: IO[Unit] = IO.unit

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
      _ <- doOperation(player, session.getInventory.getContents.zipWithIndex.map(_.swap).toMap)
      _ <- postEffect
    } yield ()
  }
}
