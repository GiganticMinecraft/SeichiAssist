package com.github.unchama.seichiassist.commands

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.{ItemListSerialization, Util}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import kotlin.Suppress
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

object ShareInvCommand {
  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def dropIfNotEmpty(itemStack: ItemStack?, to: Player) {
    if (itemStack != null && itemStack.getType !== Material.AIR) {
      Util.dropItem(to, itemStack)
    }
  }

  private @SuspendingMethod def withdrawFromSharedInventory(player: Player): TargetedEffect[Player] = {
    val playerData = SeichiAssist.playermap[player.uniqueId]
    val databaseGateway = SeichiAssist.databaseGateway

    val serial = when (val either = databaseGateway.playerDataManipulator.loadShareInv(player, playerData)) {
      is Either.Left => return either.a
      is Either.Right => either.b
    }

    if (serial == s"") return "${RESET}${RED}${BOLD}収納アイテムが存在しません。".asMessageEffect()

    val playerInventory = player.inventory

    // 永続データをクリア
    when (val clearResult = databaseGateway.playerDataManipulator.clearShareInv(player, playerData)) {
      is Either.Left => return clearResult.a
    }

    // アイテムを取り出す. 手持ちはドロップさせる
    playerInventory.contents.forEach { dropIfNotEmpty(it, player) }
    playerInventory.contents = ItemListSerialization.deserializeFromBase64(serial).toTypedArray()

    playerData.contentsPresentInSharedInventory = false

    Bukkit.getLogger().info(s"${player.name}がアイテム取り出しを実施(DB書き換え成功)")
    return s"${GREEN}アイテムを取得しました。手持ちにあったアイテムはドロップしました。".asMessageEffect()
  }

  private @SuspendingMethod def depositToSharedInventory(player: Player): TargetedEffect[Player] = {
    val playerData = SeichiAssist.playermap[player.uniqueId]
    val databaseGateway = SeichiAssist.databaseGateway

    val playerInventory = player.inventory

    // アイテム一覧をシリアル化する
    val serializedInventory = ItemListSerialization
      .serializeToBase64(playerInventory.contents.toList())
      ?: return s"${RESET}${RED}${BOLD}収納アイテムの変換に失敗しました。".asMessageEffect()

    return databaseGateway.playerDataManipulator.saveSharedInventory(player, playerData, serializedInventory).map {
      // 現所持アイテムを全て削除
      playerInventory.clear()
      playerData.contentsPresentInSharedInventory = true

      // 木の棒を取得させる
      player.performCommand("stick")

      Bukkit.getLogger().info(s"${player.name}がアイテム収納を実施(SQL送信成功)")
      s"${GREEN}アイテムを収納しました。10秒以上あとに、手持ちを空にして取り出してください。".asMessageEffect()
    }.merge()
  }

  val executor = playerCommandBuilder
      .execution { context =>
        val senderData = SeichiAssist.playermap[context.sender.uniqueId]

        if (senderData.contentsPresentInSharedInventory) {
          withdrawFromSharedInventory(context.sender)
        } else {
          depositToSharedInventory(context.sender)
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}