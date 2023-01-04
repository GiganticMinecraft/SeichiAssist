package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import cats.effect.IO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LimitedLoginEvent.{
  START_DATE,
  isInEvent
}
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LoginBonusDay.{
  Everyday,
  TotalDay
}
import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LoginBonusItemList.bonusAt
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, SeichiAssist}
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LimitedLoginBonusGifter(implicit ioOnMainThread: OnMinecraftServerThread[IO])
    extends Listener {
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

    // 今日すでにこの処理をしていたならば
    if (lastCheckedDate.equals(LocalDate.now())) return

    // 開催期間内初のログイン時だったら（=lastCheckedDateがイベント開始日より前だったら）1、そうでなければ（=開催期間中ならば）playerData.LimitedLoginCount + 1
    val loginDays =
      if (lastCheckedDate.isBefore(START_DATE)) 1
      else playerData.LimitedLoginCount + 1

    giveLoginBonus(Everyday)
    giveLoginBonus(TotalDay(loginDays))

    playerData.LimitedLoginCount = loginDays
  }

  private def giveLoginBonus(day: LoginBonusDay)(implicit player: Player): Unit = {
    val loginBonusSet =
      bonusAt(day).getOrElse(throw new NoSuchElementException("存在しないアイテムデータが指定されました。"))

    loginBonusSet.foreach { loginBonus =>
      val messageOfDay = day match {
        case TotalDay(count) => s"${count}日目"
        case Everyday        => "毎日"
      }

      loginBonus.itemId match {
        case LoginBonusGachaTicket =>
          player.sendMessage(s"【限定ログボ：$messageOfDay】${loginBonus.amount}個のガチャ券をプレゼント！")

          val skull = BukkitGachaSkullData.gachaSkull
          giveItem("ガチャ券", loginBonus.amount, skull)
      }
    }
  }

  private def giveItem(itemName: String, amount: Int, item: ItemStack)(
    implicit player: Player
  ): Unit = {
    import cats.implicits._

    DefaultEffectEnvironment.unsafeRunEffectAsync(
      s"${itemName}を付与する",
      List.fill(amount)(grantItemStacksEffect(item)).sequence.run(player)
    )
  }
}
