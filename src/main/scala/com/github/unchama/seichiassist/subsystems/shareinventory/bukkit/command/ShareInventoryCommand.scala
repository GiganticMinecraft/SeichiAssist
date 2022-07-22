package com.github.unchama.seichiassist.subsystems.shareinventory.bukkit.command

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO, Sync}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.shareinventory.SharedInventoryAPI
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.SharedFlag
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

class ShareInventoryCommand[F[_]: ConcurrentEffect](
  implicit sharedInventoryAPI: SharedInventoryAPI[F, Player]
) {

  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender

      if (sharedInventoryAPI.sharedFlag(sender).toIO.unsafeRunSync() == SharedFlag.Sharing)
        withdrawFromSharedInventory(sender)
      else depositToSharedInventory(sender)

    }
    .build()
    .asNonBlockingTabExecutor()

  import cats.implicits._

  private def withdrawFromSharedInventory(player: Player): IO[TargetedEffect[Player]] = {
    val uuid = player.getUniqueId
    val eff = for {
      _ <- checkInventoryOperationCoolDown(player)
      loadedInventory <- sharedInventoryAPI.load(uuid)
      _ <- sharedInventoryAPI.clear(uuid)
    } yield {
      val playerInventory = player.getInventory
      val inventoryContents =
        loadedInventory
          .getOrElse(return IO.pure(MessageEffect(s"$RESET$RED${BOLD}収納アイテムが存在しません。")))
          .inventoryContents
      // 手持ちのアイテムをドロップする
      playerInventory
        .getContents
        .filterNot(itemStack => itemStack == null || itemStack.getType == Material.AIR)
        .foreach(itemStack => dropIfNotEmpty(Some(itemStack), player))

      // 取り出したアイテムをセットする
      playerInventory.setContents(inventoryContents.toArray)

      sharedInventoryAPI.setNotSharing(player)
      Bukkit.getLogger.info(s"${player.getName}がアイテム取り出しを実施(DB)書き換え成功")
      MessageEffect(s"${GREEN}アイテムを取得しました。手持ちにあったアイテムはドロップしました。")
    }

    eff.toIO
  }

  private def depositToSharedInventory(player: Player): IO[TargetedEffect[Player]] = {
    val uuid = player.getUniqueId
    val playerInventory = player.getInventory
    val inventoryContents = playerInventory.getContents.toList

    if (inventoryContents.isEmpty)
      return IO.pure(MessageEffect(s"$RESET$RED${BOLD}収納アイテムが存在しません。"))

    val eff = for {
      _ <- sharedInventoryAPI.save(uuid, InventoryContents(inventoryContents))
    } yield {
      playerInventory.clear()
      sharedInventoryAPI.setSharing(player)

      // 木の棒付与
      player.performCommand("/stick")

      Bukkit.getLogger.info(s"${player.getName}がアイテム収納を実施(SQL送信成功)")
      MessageEffect(s"${GREEN}アイテムを収納しました。10秒以上あとに、手持ちを空にして取り出してください。")
    }

    eff.toIO
  }

  private def dropIfNotEmpty(itemStackOption: Option[ItemStack], to: Player): Unit =
    itemStackOption.foreach(InventoryOperations.dropItem(to, _))

  private def checkInventoryOperationCoolDown(
    player: Player
  ): F[Either[TargetedEffect[CommandSender], Unit]] = Sync[F].delay {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    // 連打による負荷防止
    if (!playerData.shareinvcooldownflag)
      Left(MessageEffect(s"${RED}しばらく待ってからやり直してください"))
    else {
      new CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(SeichiAssist.instance, 200)
      Right(())
    }
  }

}
