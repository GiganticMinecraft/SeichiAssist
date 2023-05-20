package com.github.unchama.seichiassist.util

import cats.data
import cats.data.Kleisli
import cats.effect.SyncIO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.util.bukkit.ItemStackUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.{ItemStack, PlayerInventory}

object InventoryOperations {

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

  def removeItemfromPlayerInventory(
    inventory: PlayerInventory,
    itemstack: ItemStack,
    count: Int
  ): Boolean = {
    // 持っているアイテムを減らす処理
    if (itemstack.getAmount == count) {
      // アイテムをcount個使うので、プレイヤーの手を素手にする
      inventory.setItemInMainHand(new ItemStack(Material.AIR))
      true
    } else if (itemstack.getAmount > count) {
      // プレイヤーが持っているアイテムをcount個減らす
      itemstack.setAmount(itemstack.getAmount - count)
      true
    } else false
  }
}
