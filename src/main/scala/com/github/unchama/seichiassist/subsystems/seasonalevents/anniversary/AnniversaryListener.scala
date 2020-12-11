package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.Anniversary.{ANNIVERSARY_COUNT, EVENT_DATE, blogArticleUrl}
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData.mineHead
import com.github.unchama.seichiassist.util.Util.{addItem, isPlayerInventoryFull}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

import java.time.LocalDate

object AnniversaryListener extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (LocalDate.now().isEqual(EVENT_DATE)) {
      List(
        s"${BLUE}本日でギガンティック☆整地鯖は${ANNIVERSARY_COUNT}周年を迎えます。",
        s"${BLUE}これを記念し、限定アイテムを入手可能です。詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(player.sendMessage)
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    }
  }

  @EventHandler
  def onPlayerDeath(event: PlayerDeathEvent): Unit = {
    val player = event.getEntity
    val playerUuid = player.getUniqueId
    val playerData = SeichiAssist.playermap(playerUuid)
    if (!playerData.anniversary) return

    if (isPlayerInventoryFull(player)) {
      player.sendMessage(s"${RED}インベントリに空きがなかったため、アイテムを配布できませんでした。")
    } else {
      addItem(player, mineHead)
      playerData.anniversary = false
      SeichiAssist.databaseGateway.playerDataManipulator.setAnniversary(anniversary = false, Some.apply(playerUuid))
      player.sendMessage(s"${BLUE}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年の記念品を入手しました。")
    }
    player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
  }
}