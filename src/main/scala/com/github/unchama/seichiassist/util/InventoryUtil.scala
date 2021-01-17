package com.github.unchama.seichiassist.util

import cats.data
import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.{Bukkit, Material}
import org.bukkit.entity.Player
import org.bukkit.inventory.{ItemStack, PlayerInventory}

import scala.jdk.CollectionConverters._

object InventoryUtil {

  /**
   * プレイヤーに複数のアイテムを一度に付与する。
   * インベントリに入り切らなかったアイテムはプレーヤーの立ち位置にドロップされる。
   *
   * @param itemStacks 付与するアイテム
   */
  def grantItemStacksEffect(itemStacks: ItemStack*): TargetedEffect[Player] = data.Kleisli { player =>
    val toGive: Seq[ItemStack] = itemStacks.filter(_.getType != Material.AIR)

    for {
      _ <- IO {
        if (toGive.size != itemStacks.size)
          Bukkit.getLogger.warning("attempt to add Material.AIR to player inventory")
      }
      _ <- PluginExecutionContexts.syncShift.shift
      _ <- IO {
        player.getInventory
          .addItem(itemStacks: _*)
          .values().asScala
          .filter(_.getType != Material.AIR)
          .foreach(dropItem(player, _))
      }
    } yield ()
  }

  //プレイヤーのインベントリがフルかどうか確認
  def isPlayerInventoryFull(player: Player): Boolean = player.getInventory.firstEmpty() == -1

  //指定されたアイテムを指定されたプレイヤーにドロップする
  def dropItem(player: Player, itemstack: ItemStack): Unit = {
    player.getWorld.dropItemNaturally(player.getLocation, itemstack)
  }

  //指定されたアイテムを指定されたプレイヤーインベントリに追加する
  def addItem(player: Player, itemstack: ItemStack): Unit = {
    player.getInventory.addItem(itemstack)
  }

  def removeItemfromPlayerInventory(inventory: PlayerInventory,
                                    itemstack: ItemStack, count: Int): Boolean = {
    //持っているアイテムを減らす処理
    if (itemstack.getAmount == count) {
      // アイテムをcount個使うので、プレイヤーの手を素手にする
      inventory.setItemInMainHand(new ItemStack(Material.AIR))
    } else if (itemstack.getAmount > count) {
      // プレイヤーが持っているアイテムをcount個減らす
      itemstack.setAmount(itemstack.getAmount - count)
    } else
      return itemstack.getAmount >= count
    true
  }
}
