package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import com.github.unchama.seichiassist.data.GachaSkullData
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, SeichiAssist}
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object LimitedLoginBonusGifter extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val activeEvents: Set[LimitedLoginEvent] = LimitedLoginEvents.findActiveEvents
    if (activeEvents.isEmpty) return

    activeEvents.foreach { implicit activeEvent =>
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
        if (lastCheckedDate.isBefore(activeEvent.period.startDate)) 1
        else playerData.LimitedLoginCount + 1
      }

      giveLoginBonus(Everyday)
      giveLoginBonus(EventLoginCount(loginDays))

      playerData.LimitedLoginCount = loginDays
    }
  }

  private def giveLoginBonus(index: LoginBonusIndex)(implicit player: Player, event: LimitedLoginEvent): Unit = {
    val loginBonusSet = event.bonusAt(index)
      .getOrElse(throw new NoSuchElementException("存在しないアイテムデータが指定されました。"))

    loginBonusSet.foreach { loginBonus =>
      val messageOfDay = index match {
        case EventLoginCount(count) => s"${count}日目"
        case Everyday => "毎日"
      }

      loginBonus.itemId match {
        case LoginBonusGachaTicket =>
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