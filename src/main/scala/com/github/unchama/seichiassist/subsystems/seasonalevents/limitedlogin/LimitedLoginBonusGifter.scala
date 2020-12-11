package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import com.github.unchama.seichiassist.data.GachaSkullData
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LimitedLoginEvent.{START_DATE, isInEvent}
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LoginBonusItemData.loginBonusAt
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, SeichiAssist}
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object LimitedLoginBonusGifter extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    if (!isInEvent) return

    implicit val player: Player = event.getPlayer
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val lastCheckedDate = {
      val lastChecked = playerData.lastcheckdate
      val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

      LocalDate.parse(lastChecked, formatter)
    }

    // 開催期間内初のログイン時だったら（=lastCheckedDateがイベント開始日より前だったら）1、そうでなければ（=開催期間中ならば）playerData.LimitedLoginCount + 1
    val loginDays =
      if (lastCheckedDate.isBefore(START_DATE)) 1
      else playerData.LimitedLoginCount + 1

    // 0日目のアイテムは毎日配布される
    giveLoginBonus(0)
    giveLoginBonus(loginDays)

    playerData.LimitedLoginCount = loginDays
  }

  private def giveLoginBonus(day: Int)(implicit player: Player): Unit = {
    val loginBonus = loginBonusAt(day) match {
      case Some(loginBonus) => loginBonus
      case None => throw new NoSuchElementException("存在しないアイテムデータが指定されました。")
    }

    loginBonus.itemId match {
      case LoginBonusGachaTicket =>
        val messageofDay = if (day == 0) "毎日" else s"${day}日目"
        player.sendMessage(s"【限定ログボ：$messageofDay】${loginBonus.amount}個のガチャ券をプレゼント！")

        val skull = GachaSkullData.gachaSkull

        import cats.implicits._
        DefaultEffectEnvironment.runEffectAsync(
          "ガチャ券を付与する",
          List.fill(loginBonus.amount)(
            grantItemStacksEffect(skull).run(player)
          ).sequence
        )
    }
  }
}