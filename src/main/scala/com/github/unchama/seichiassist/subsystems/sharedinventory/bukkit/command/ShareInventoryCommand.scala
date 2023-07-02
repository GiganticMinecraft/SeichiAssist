package com.github.unchama.seichiassist.subsystems.sharedinventory.bukkit.command

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO, Sync}
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.sharedinventory.SharedInventoryAPI
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.SharedFlag
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.CommandEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

class ShareInventoryCommand[F[_]: ConcurrentEffect](
  implicit sharedInventoryAPI: SharedInventoryAPI[F, Player]
) {

  import cats.implicits._

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender
      for {
        sharedFlag <- sharedInventoryAPI.sharedFlag(sender).toIO
        eff <-
          if (sharedFlag == SharedFlag.Sharing) {
            withdrawFromSharedInventory(sender)
          } else {
            depositToSharedInventory(sender)
          }
      } yield eff
    }
    .build()
    .asNonBlockingTabExecutor()

  private def withdrawFromSharedInventory(player: Player): IO[TargetedEffect[Player]] = {
    val uuid = player.getUniqueId
    val eff = for {
      oldSharedFlag <- sharedInventoryAPI.sharedFlag(player)
      loadedInventory <- sharedInventoryAPI.load(uuid)
      _ <- sharedInventoryAPI.clear(uuid)
      newSharedFlag <- sharedInventoryAPI.sharedFlag(player)
      _ <- Sync[F]
        .delay {
          val playerInventory = player.getInventory
          val inventoryContents = loadedInventory.get.inventoryContents
          // 手持ちのアイテムをドロップする
          playerInventory
            .getContents
            .filterNot(itemStack => itemStack == null || itemStack.getType == Material.AIR)
            .foreach(itemStack => dropIfNotEmpty(Some(itemStack), player))
          // 取り出したアイテムをセットする
          playerInventory.setContents(inventoryContents.toArray)
          Bukkit.getLogger.info(s"${player.getName}がアイテム取り出しを実施(DB)書き換え成功")
        }
        .whenA(oldSharedFlag != newSharedFlag && loadedInventory.nonEmpty)
    } yield {
      if (oldSharedFlag != newSharedFlag && loadedInventory.nonEmpty)
        MessageEffect(s"${GREEN}アイテムを取得しました。手持ちにあったアイテムはドロップしました。")
      else if (oldSharedFlag == newSharedFlag)
        MessageEffect(s"$RESET$RED${BOLD}もう少し待ってからアイテム取り出しを行ってください。")
      else
        MessageEffect(s"$RESET$RED${BOLD}収納アイテムが存在しません。")
    }

    eff.toIO
  }

  private def depositToSharedInventory(player: Player): IO[TargetedEffect[Player]] = {
    val uuid = player.getUniqueId
    val playerInventory = player.getInventory
    val inventoryContents = playerInventory.getContents.toList

    if (inventoryContents.forall(_ == null))
      return IO.pure(MessageEffect(s"$RESET$RED${BOLD}収納アイテムが存在しません。"))

    val eff = for {
      oldSharedFlag <- sharedInventoryAPI.sharedFlag(player)
      _ <- sharedInventoryAPI.save(uuid, InventoryContents(inventoryContents))
      newSharedFlag <- sharedInventoryAPI.sharedFlag(player)
      _ <- Sync[F]
        .delay {
          playerInventory.clear()
          Bukkit.getLogger.info(s"${player.getName}がアイテム収納を実施(SQL送信成功)")
        }
        .whenA(oldSharedFlag != newSharedFlag)
    } yield {
      if (oldSharedFlag == newSharedFlag)
        MessageEffect(s"$RESET$RED${BOLD}もう少し待ってからアイテムを収納してください。")
      else
        SequentialEffect(
          CommandEffect("stick"),
          MessageEffect(s"${GREEN}アイテムを収納しました。10秒以上あとに、手持ちを空にして取り出してください。")
        )
    }

    eff.toIO
  }

  private def dropIfNotEmpty(itemStackOption: Option[ItemStack], to: Player): Unit =
    itemStackOption.foreach(InventoryOperations.dropItem(to, _))

}
