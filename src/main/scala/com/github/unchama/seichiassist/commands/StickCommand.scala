package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.TabExecutor
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object StickCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender
      // 初見プレイヤー用とは別に簡潔な説明
      val stickLore = List(
        "棒を持って右クリックもしくは左クリックでメニューを開きます。",
        "各メニューの詳細は公式サイトで確認できます。"
      )
      val stickItemStack = new ItemStack(Material.STICK, 1).tap { itemStack =>
        import itemStack._
        val meta = getItemMeta
        meta.setDisplayName("棒メニューが開ける棒")
        meta.setLore(stickLore.asJava)
        setItemMeta(meta)
      }

      if (!InventoryOperations.isPlayerInventoryFull(sender)) {
        InventoryOperations.addItem(sender, stickItemStack)
        sender.playSound(sender.getLocation, Sound.ENTITY_ITEM_PICKUP, 0.1f, 1.0f)
      } else {
        InventoryOperations.dropItem(sender, stickItemStack)
      }

      IO(TargetedEffect.emptyEffect)
    }
    .build()
    .asNonBlockingTabExecutor()
}
