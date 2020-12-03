package com.github.unchama.seasonalevents.limitedlogin

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.github.unchama.seasonalevents.limitedlogin.LimitedLoginEvent.{START_DATE, isInEvent}
import com.github.unchama.seasonalevents.limitedlogin.LimitedLoginItemData.getItemData
import com.github.unchama.seichiassist.data.GachaSkullData
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, SeichiAssist}
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

object LimitedLoginBonusGifter extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    if (!isInEvent) return

    val player = event.getPlayer
    val playerUuid = player.getUniqueId

    // この条件分岐がtrueになる可能性は通常ない（ログインしている限りplayerMapにはそのMCIDのデータが有るはずだ）が、なっている事例があるので念の為
    // 参照：https://github.com/GiganticMinecraft/SeichiAssist/issues/707
    if (!SeichiAssist.playermap.contains(playerUuid)) return

    val playerData = SeichiAssist.playermap(playerUuid)
    val lastChecked = playerData.lastcheckdate
    // 開催期間内初のログイン時だったら（=lastCheckedDateがイベント開始日より前だったら）0、そうでなければ（=開催期間中ならば）playerData.LimitedLoginCount
    var loginDays =
      if (lastCheckedDate.isBefore(START_DATE)) 0
      else playerData.LimitedLoginCount
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val lastCheckedDate = LocalDate.parse(lastChecked, formatter)

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

    playerData.LimitedLoginCount_$eq(loginDays)
  }
}