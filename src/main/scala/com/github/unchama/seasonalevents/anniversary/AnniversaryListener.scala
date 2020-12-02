package com.github.unchama.seasonalevents.anniversary

import java.time.LocalDate

import com.github.unchama.seasonalevents.anniversary.Anniversary.{ANNIVERSARY_COUNT, EVENT_DATE, blogArticleUrl}
import com.github.unchama.seasonalevents.anniversary.AnniversaryItemData.mineHead
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util.{addItem, isPlayerInventoryFull}
import org.bukkit.ChatColor._
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Bukkit, Sound}

object AnniversaryListener extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (LocalDate.now().isEqual(EVENT_DATE)) {
      List(
        s"${BLUE}本日でギガンティック☆整地鯖は${ANNIVERSARY_COUNT}周年を迎えます。",
        s"${BLUE}これを記念し、限定アイテムを入手可能です。詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(player.sendMessage(_))
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    }
  }

  @EventHandler
  def onPlayerDeath(event: PlayerDeathEvent): Unit = {
    val player = event.getEntity
    val playerUuid = player.getUniqueId

    // この条件分岐がtrueになる可能性は通常ない（ログインしている限りplayerMapにはそのMCIDのデータが有るはずだ）が、なっている事例があるので念の為
    // 参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/707
    if (!SeichiAssist.playermap.contains(playerUuid)) {
      Bukkit.getServer.getLogger.info(s"${player.getName}のプレイヤーデータが存在しなかったため、周年記念ヘッドを配布できませんでした。")
      player.sendMessage(s"${RED}内部的なエラーによりアイテムを配布できませんでした。管理者にお問い合わせください。")
      return
    }

    val playerData = SeichiAssist.playermap(playerUuid)
    if (!playerData.anniversary) return

    if (isPlayerInventoryFull(player)) {
      player.sendMessage(s"${RED}インベントリに空きがなかったため、アイテムを配布できませんでした。")
    } else {
      addItem(player, mineHead)
      playerData.anniversary_$eq(false)
      SeichiAssist.databaseGateway.playerDataManipulator.setAnniversary(false, Some.apply(playerUuid))
      player.sendMessage(s"${BLUE}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年の記念品を入手しました。")
    }
    player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
  }
}