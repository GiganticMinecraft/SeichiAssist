package com.github.unchama.seichiassist.util

import cats.data
import cats.data.Kleisli
import cats.effect.SyncIO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import InventoryUtil.dropItem
import com.github.unchama.util.bukkit.ItemStackUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object InventoryUtil {

  import scala.jdk.CollectionConverters._

  /**
   * プレイヤーに複数のアイテムを一度に付与する。 インベントリに入り切らなかったアイテムはプレーヤーの立ち位置にドロップされる。
   *
   * @param itemStacks
   *   付与するアイテム
   */
  def grantItemStacksEffect[F[_]: OnMinecraftServerThread](
    itemStacks: ItemStack*
  ): Kleisli[F, Player, Unit] =
    data.Kleisli { player =>
      val amalgamated = ItemStackUtil.amalgamate(itemStacks).filter(_.getType != Material.AIR)

      OnMinecraftServerThread[F].runAction(SyncIO {
        player
          .getInventory
          .addItem(amalgamated: _*)
          .values()
          .asScala
          .filter(_.getType != Material.AIR)
          .foreach(dropItem(player, _))
      })
    }

  // プレイヤーのインベントリがフルかどうか確認
  def isPlayerInventoryFull(player: Player): Boolean = player.getInventory.firstEmpty() == -1

  // 指定されたアイテムを指定されたプレイヤーにドロップする
  def dropItem(player: Player, itemstack: ItemStack): Unit = {
    player.getWorld.dropItemNaturally(player.getLocation, itemstack)
  }

  // 指定されたアイテムを指定されたプレイヤーインベントリに追加する
  def addItem(player: Player, itemstack: ItemStack): Unit = {
    player.getInventory.addItem(itemstack)
  }
}
