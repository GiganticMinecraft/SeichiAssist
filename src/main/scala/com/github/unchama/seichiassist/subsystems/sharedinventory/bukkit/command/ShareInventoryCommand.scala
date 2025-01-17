package com.github.unchama.seichiassist.subsystems.sharedinventory.bukkit.command

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.sharedinventory.SharedInventoryAPI
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.SharedFlag
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import com.github.unchama.targetedeffect.player.CommandEffectF
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffectF}
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

class ShareInventoryCommand[F[_]: ConcurrentEffect: OnMinecraftServerThread](
  implicit sharedInventoryAPI: SharedInventoryAPI[F, Player]
) {

  import cats.implicits._

  val executor: TabExecutor = playerCommandBuilder
    .buildWithExecutionCSEffect { context =>
      val sender = context.sender

      Kleisli.liftF(sharedInventoryAPI.sharedFlag(sender)).flatMap { sharedFlag =>
        if (sharedFlag == SharedFlag.Sharing) {
          withdrawFromSharedInventory(sender)
        } else {
          depositToSharedInventory(sender)
        }
      }
    }
    .asNonBlockingTabExecutor()

  private def withdrawFromSharedInventory(player: Player): TargetedEffectF[F, Player] = {
    val uuid = player.getUniqueId
    (for {
      oldSharedFlag <- Kleisli.liftF(sharedInventoryAPI.sharedFlag(player))
      loadedInventory <- Kleisli.liftF(sharedInventoryAPI.load(uuid))
      _ <- Kleisli.liftF(sharedInventoryAPI.clear(uuid))
      newSharedFlag <- Kleisli.liftF(sharedInventoryAPI.sharedFlag(player))
      _ <- Kleisli.liftF(
        Sync[F]
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
      )
    } yield {
      if (oldSharedFlag != newSharedFlag && loadedInventory.nonEmpty)
        MessageEffectF(s"${GREEN}アイテムを取得しました。手持ちにあったアイテムはドロップしました。")
      else if (oldSharedFlag == newSharedFlag)
        MessageEffectF(s"$RESET$RED${BOLD}もう少し待ってからアイテム取り出しを行ってください。")
      else
        MessageEffectF(s"$RESET$RED${BOLD}収納アイテムが存在しません。")
    }).flatten
  }

  private def depositToSharedInventory(player: Player): TargetedEffectF[F, Player] = {
    val uuid = player.getUniqueId
    val playerInventory = player.getInventory
    val inventoryContents = playerInventory.getContents.toList

    if (inventoryContents.forall(_ == null))
      return MessageEffectF(s"$RESET$RED${BOLD}収納アイテムが存在しません。")

    (for {
      oldSharedFlag <- Kleisli.liftF(sharedInventoryAPI.sharedFlag(player))
      _ <- Kleisli.liftF(sharedInventoryAPI.save(uuid, InventoryContents(inventoryContents)))
      newSharedFlag <- Kleisli.liftF(sharedInventoryAPI.sharedFlag(player))
      _ <- Kleisli.liftF(
        Sync[F]
          .delay {
            playerInventory.clear()
            Bukkit.getLogger.info(s"${player.getName}がアイテム収納を実施(SQL送信成功)")
          }
          .whenA(oldSharedFlag != newSharedFlag)
      )
    } yield {
      if (oldSharedFlag == newSharedFlag)
        MessageEffectF(s"$RESET$RED${BOLD}もう少し待ってからアイテムを収納してください。")
      else
        SequentialEffect(
          CommandEffectF("stick"),
          MessageEffectF(s"${GREEN}アイテムを収納しました。10秒以上あとに、手持ちを空にして取り出してください。")
        )
    }).flatten
  }

  private def dropIfNotEmpty(itemStackOption: Option[ItemStack], to: Player): Unit =
    itemStackOption.foreach(InventoryOperations.dropItem(to, _))

}
