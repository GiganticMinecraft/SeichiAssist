package com.github.unchama.seasonalevents.limitedlogin

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.github.unchama.seasonalevents.limitedlogin.LimitedLoginEvent.{EVENT_PERIOD, isInEvent}
import com.github.unchama.seasonalevents.limitedlogin.LimitedLoginItemData.getItemData
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, SeichiAssist}
import com.github.unchama.seichiassist.data.GachaSkullData
import com.github.unchama.seichiassist.util.Util.{addItem, dropItem, grantItemStacksEffect, isPlayerInventoryFull}
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

object LimitedLoginBonusGifter extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer
    val playerUuid = player.getUniqueId

    // この条件分岐がtrueになる可能性は通常ない（ログインしている限りplayerMapにはそのMCIDのデータが有るはずだ）が、なっている事例があるので念の為
    // 参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/707
    if (!SeichiAssist.playermap.contains(playerUuid)) return

    val playerData = SeichiAssist.playermap(playerUuid)
    val lastChecked = playerData.lastcheckdate
    var loginDays = playerData.LimitedLoginCount
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val lastCheckedDate = LocalDate.parse(lastChecked, formatter)
    if (isInEvent) {
      if (EVENT_PERIOD.contains(lastCheckedDate)) {
        loginDays = 0
      }
      loginDays += 1
      var days = 0
      do {
        val itemNumber = getItemData(days)._1
        val amount = getItemData(days)._2

        itemNumber match {
          case 1 => {
            player.sendMessage(s"【限定ログボ：${days}日目】${amount}個のガチャ券をプレゼント！")
            val skull = GachaSkullData.gachaSkull
            for (_ <- 1 to amount) {
              DefaultEffectEnvironment.runEffectAsync(
                "ガチャ券を付与する",
                grantItemStacksEffect(skull).run(player)
              )
            }
          }
          case _ =>
        }

        days += loginDays
      } while (days == loginDays)
    }
    playerData.LimitedLoginCount_$eq(loginDays)
  }
}