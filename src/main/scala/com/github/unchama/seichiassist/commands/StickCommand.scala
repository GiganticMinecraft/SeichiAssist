package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.InventoryOperations
import org.bukkit.command.TabExecutor
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object StickCommand {
  val executor: TabExecutor = playerCommandBuilder
    .buildWithExecutionF { context =>
      // 初見プレイヤー用とは別に簡潔な説明
      val stickLore = List("棒を持って右クリックもしくは左クリックでメニューを開きます。", "各メニューの詳細は公式サイトで確認できます。")
      val stickItemStack = new ItemStack(Material.STICK, 1).tap { itemStack =>
        import itemStack._
        val meta = getItemMeta
        meta.setDisplayName("木の棒メニュー")
        meta.setLore(stickLore.asJava)
        setItemMeta(meta)
      }

      val sender = context.sender
      for {
        inventoryNotFull <- IO(!InventoryOperations.isPlayerInventoryFull(sender))
        _ <-
          if (inventoryNotFull) IO {
            InventoryOperations.addItem(sender, stickItemStack)
            sender.playSound(sender.getLocation, Sound.ENTITY_ITEM_PICKUP, 0.1f, 1.0f)
          }
          else
            IO {
              InventoryOperations.dropItem(sender, stickItemStack)
            }
      } yield ()
    }
    .asNonBlockingTabExecutor()
}
