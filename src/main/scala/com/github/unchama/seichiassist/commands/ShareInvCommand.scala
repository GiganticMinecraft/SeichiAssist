package com.github.unchama.seichiassist.commands

import cats.Applicative
import cats.data.EitherT
import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.{InventoryOperations, ItemListSerialization}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

object ShareInvCommand {

  import scala.jdk.CollectionConverters._

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val senderData = SeichiAssist.playermap(context.sender.getUniqueId)

      if (senderData.contentsPresentInSharedInventory) {
        withdrawFromSharedInventory(context.sender)
      } else {
        depositToSharedInventory(context.sender)
      }
    }
    .build()
    .asNonBlockingTabExecutor()

  private def withdrawFromSharedInventory(player: Player): IO[TargetedEffect[Player]] = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val databaseGateway = SeichiAssist.databaseGateway

    {
      for {
        serial <- EitherT(databaseGateway.playerDataManipulator.loadShareInv(player))
        _ <- EitherT
          .cond[IO](serial != "", (), MessageEffect(s"$RESET$RED${BOLD}収納アイテムが存在しません。"))
        _ <- EitherT(databaseGateway.playerDataManipulator.clearShareInv(player, playerData))
        playerInventory = player.getInventory
        _ <- EitherT.right {
          IO {
            // アイテムを取り出す. 手持ちはドロップさせる
            playerInventory
              .getContents
              .filterNot(_ == null)
              .filterNot(_.getType == Material.AIR)
              .foreach(stack => dropIfNotEmpty(Some(stack), player))
            playerInventory.setContents(
              ItemListSerialization.deserializeFromBase64(serial).asScala.toArray
            )

            playerData.contentsPresentInSharedInventory = false
            Bukkit.getLogger.info(s"${player.getName}がアイテム取り出しを実施(DB書き換え成功)")
          }
        }
        successful <- EitherT.rightT[IO, TargetedEffect[Player]](
          MessageEffect(s"${GREEN}アイテムを取得しました。手持ちにあったアイテムはドロップしました。")
        )
      } yield successful
    }.merge
  }

  def dropIfNotEmpty(itemStackOption: Option[ItemStack], to: Player): Unit = {
    itemStackOption match {
      case Some(itemStack) => InventoryOperations.dropItem(to, itemStack)
      case None            =>
    }
  }

}
