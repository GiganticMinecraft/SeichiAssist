package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.InventoryOperations
import org.bukkit.command.TabExecutor
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}
import org.bukkit.ChatColor.{RESET, WHITE, GOLD}

import java.time.LocalDate
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object StickCommand {
  val executor: TabExecutor = playerCommandBuilder
    .buildWithExecutionF { context =>
      val sender = context.sender
      for {
        thisMonth <- IO(LocalDate.now().getMonth.getValue)
        stickLore <- IO.pure(
          List(
            s"$RESET${WHITE}棒を持って右クリックもしくは",
            s"$RESET${WHITE}左クリックでメニューを開きます。",
            s"$RESET${WHITE}各メニューの詳細は公式サイトで確認できます。",
            "",
            s"$RESET$GOLD- Monthly Stick Vol.$thisMonth -"
          )
        )
        stickItemStack <- IO(new ItemStack(Material.STICK, 1).tap { itemStack =>
          import itemStack._
          val meta = getItemMeta
          meta.setDisplayName(s"$RESET${WHITE}木の棒メニュー(${thisMonth}月)")
          meta.setLore(stickLore.asJava)
          setItemMeta(meta)
        })
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
