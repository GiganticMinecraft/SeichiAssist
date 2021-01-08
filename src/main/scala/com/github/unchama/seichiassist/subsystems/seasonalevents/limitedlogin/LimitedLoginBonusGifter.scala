package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.github.unchama.seichiassist.data.GachaSkullData
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, SeichiAssist}
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

object LimitedLoginBonusGifter extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    implicit val eventStatus: LimitedLoginEvent = LimitedLoginEvents.findActiveEvent.getOrElse(return)

    implicit val player: Player = event.getPlayer
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val lastCheckedDate = {
      val lastChecked = playerData.lastcheckdate
      val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

      LocalDate.parse(lastChecked, formatter)
    }

    // 今日すでにこの処理をしていたならば
    // TODO この処理javaの頃はなかったが、なくても大丈夫なのか？
    if (lastCheckedDate.equals(LocalDate.now())) return

    // 開催期間内初のログイン時だったら（=lastCheckedDateがイベント開始日より前だったら）1、そうでなければ（=開催期間中ならば）playerData.LimitedLoginCount + 1
    val loginDays = {
      if (lastCheckedDate.isBefore(eventStatus.period.startDate)) 1
      else playerData.LimitedLoginCount + 1
    }

    // 0日目のアイテムは毎日配布される
    giveLoginBonus(0)
    giveLoginBonus(loginDays)

    playerData.LimitedLoginCount = loginDays
  }

  private def giveLoginBonus(day: Int)(implicit player: Player, eventStatus: LimitedLoginEvent): Unit = {
    val loginBonusSet = eventStatus.loginBonusAt(day) match {
      case Some(loginBonusSet) if loginBonusSet.nonEmpty => loginBonusSet
      case _ => throw new NoSuchElementException("存在しないアイテムデータが指定されました。")
    }

    loginBonusSet.foreach { loginBonus =>
      loginBonus.itemId match {
        case LoginBonusGachaTicket =>
          val messageOfDay = if (day == 0) "毎日" else s"${day}日目"
          player.sendMessage(s"【限定ログボ：$messageOfDay】${loginBonus.amount}個のガチャ券をプレゼント！")

          val skull = GachaSkullData.gachaSkull
          giveItem("ガチャ券", loginBonus.amount, skull)
      }
    }
  }

  private def giveItem(itemName: String, amount: Int, item: ItemStack)(implicit player: Player): Unit = {
    import cats.implicits._
    DefaultEffectEnvironment.runEffectAsync(
      s"${itemName}を付与する",
      List.fill(amount)(
        grantItemStacksEffect(item).run(player)
      ).sequence
    )
  }
}