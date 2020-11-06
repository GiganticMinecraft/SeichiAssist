package com.github.unchama.seasonalevents.anniversary

import java.time.LocalDate

import com.github.unchama.seasonalevents.anniversary.Anniversary.{EVENT_DATE, blogArticleUrl}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util.isPlayerInventoryFull
import org.bukkit.ChatColor._
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Bukkit, Sound}

object AnniversaryListener extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (player != null && LocalDate.now().isEqual(EVENT_DATE)) {
      List(
        s"${BLUE}本日でギガンティック☆整地鯖は1周年を迎えます。",
        s"${BLUE}これを記念し、限定アイテムを入手可能です。詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(player.sendMessage)
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    }
  }

  @EventHandler
  def onPlayerDeath(event: PlayerDeathEvent): Unit = {
    val player = event.getEntity
    if (player == null) return
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
      // TOOD mine_chamaというMCIDのヘッドで直接指定できる
      val command = s"""give ${player.getName} skull 1 3 {display:{Name:"まいんちゃん",Lore:["", ${YELLOW}整地サーバー1周年記念だよ！"]},SkullOwner:{Id:"fac7c46e-3e89-4249-bef6-948d5eb528c9",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDhmNTQ0OGI0ZDg4ZTQwYjE0YzgyOGM2ZjFiNTliMzg1NDVkZGE5MzNlNzNkZmYzZjY5NWU2ZmI0Mjc4MSJ9fX0="}]}}}"""
      Bukkit.dispatchCommand(Bukkit.getConsoleSender, command)
      player.sendMessage(s"${BLUE}ギガンティック☆整地鯖1周年の記念品を入手しました。")

      playerData.anniversary_$eq(false)
      SeichiAssist.databaseGateway.playerDataManipulator.setAnniversary(false, Some.apply(playerUuid))
    }
    player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
  }
}