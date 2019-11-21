package com.github.unchama.seichiassist.commands

import cats.Applicative
import cats.data.EitherT
import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.{ItemListSerialization, Util}
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

object ShareInvCommand {

  import com.github.unchama.targetedeffect.syntax._

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
        serial <- EitherT(databaseGateway.playerDataManipulator.loadShareInv(player, playerData))
        _ <- EitherT.cond[IO](serial != "", (), s"$RESET$RED${BOLD}収納アイテムが存在しません。".asMessageEffect())
        _ <- EitherT(databaseGateway.playerDataManipulator.clearShareInv(player, playerData))
        playerInventory = player.getInventory
        _ <- EitherT.right {
          IO {
            // アイテムを取り出す. 手持ちはドロップさせる
            playerInventory.getContents
              .filterNot(_ == null)
              .filterNot(_.getType == Material.AIR)
              .foreach(stack => dropIfNotEmpty(Some(stack), player))
            playerInventory.setContents(ItemListSerialization.deserializeFromBase64(serial).asScala.toArray)

            playerData.contentsPresentInSharedInventory = false
            Bukkit.getLogger.info(s"${player.getName}がアイテム取り出しを実施(DB書き換え成功)")
          }
        }
        successful <- EitherT.rightT[IO, TargetedEffect[Player]](s"${GREEN}アイテムを取得しました。手持ちにあったアイテムはドロップしました。".asMessageEffect())
      } yield successful
      }.merge
  }

  def dropIfNotEmpty(itemStackOption: Option[ItemStack], to: Player): Unit = {
    itemStackOption match {
      case Some(itemStack) => Util.dropItem(to, itemStack)
      case None =>
    }
  }

  private def depositToSharedInventory(player: Player): IO[TargetedEffect[Player]] = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val databaseGateway = SeichiAssist.databaseGateway

    val playerInventory = player.getInventory

    def takeIfNotNull[App[_] : Applicative, E, A](a: A, fail: E): EitherT[App, E, A] =
      EitherT.cond[App](a != null, a, fail)

    {
      for {
        inventory <- EitherT.rightT[IO, TargetedEffect[Player]](playerInventory.getContents.toList.asJava)
        serializedInventory <-
          takeIfNotNull[IO, TargetedEffect[Player], String](
            ItemListSerialization.serializeToBase64(inventory),
            s"$RESET$RED${BOLD}収納アイテムの変換に失敗しました。".asMessageEffect()
          )
        _ <- EitherT(databaseGateway.playerDataManipulator.saveSharedInventory(player, playerData, serializedInventory))
        successEffect <- EitherT.right[TargetedEffect[Player]] {
          IO {
            // 現所持アイテムを全て削除
            playerInventory.clear()
            playerData.contentsPresentInSharedInventory = true

            // 木の棒を取得させる
            player.performCommand("stick")

            Bukkit.getLogger.info(s"${player.getName}がアイテム収納を実施(SQL送信成功)")
            s"${GREEN}アイテムを収納しました。10秒以上あとに、手持ちを空にして取り出してください。".asMessageEffect()
          }
        }
      } yield successEffect
      }.merge
  }
}