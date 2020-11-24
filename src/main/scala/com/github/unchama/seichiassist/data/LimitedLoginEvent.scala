package com.github.unchama.seichiassist.data

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.Util
import org.bukkit.entity.Player

object LimitedLoginEvent {
  private val config = SeichiAssist.seichiAssistConfig
}

class LimitedLoginEvent {
  private val playermap = SeichiAssist.playermap
  private var lastcheckdate: String = _

  def setLastCheckDate(s: String): Unit = lastcheckdate = s

  //ここで処理対象のユーザーと、そのtitleNoを拾って処理を行う。
  def TryGetItem(p: Player): Unit = {
    val uuid = p.getUniqueId
    val playerdata: PlayerData = playermap.getOrElse(uuid, null)
    val skull = GachaSkullData.gachaSkull
    val cal = Calendar.getInstance
    val sdf = new SimpleDateFormat("yyyy/MM/dd")
    if (lastcheckdate == null) return
    if (lastcheckdate.isEmpty) return
    try {
      val TodayDate = sdf.parse(sdf.format(cal.getTime))
      val LastDate = sdf.parse(lastcheckdate)
      val LLEStart = sdf.parse(LimitedLoginEvent.config.getLimitedLoginEventStart)
      val LLEEnd = sdf.parse(LimitedLoginEvent.config.getLimitedLoginEventEnd)
      val TodayLong = TodayDate.getTime
      val LastLong = LastDate.getTime
      val LLEStartLong = LLEStart.getTime
      val LLEEndLong = LLEEnd.getTime
      var loginDays = playerdata.LimitedLoginCount
      var configDays = 0
      var internalItemId = 0
      var amount = 0
      //開催期間内かどうか
      val today2start = (TodayLong - LLEStartLong) / (1000 * 60 * 60 * 24)
      val today2end = (TodayLong - LLEEndLong) / (1000 * 60 * 60 * 24)
      val last2start = (LastLong - LLEStartLong) / (1000 * 60 * 60 * 24)
      val last2end = (LastLong - LLEEndLong) / (1000 * 60 * 60 * 24)
      if ((today2start >= 0) && (today2end <= 0)) { //最終ログインが開催期間内だったか
        if (!((last2start >= 0) && (last2end <= 0))) { //開催期間内初のログイン時、開催終了後初のログイン時にここを処理
          //期間限定の累計ログイン数のデータをリセットする。
          loginDays = 0
        }
        loginDays += 1
        configDays = 0
        do {
          internalItemId = LimitedLoginEvent.config.getLimitedLoginEventItem(configDays)
          amount = LimitedLoginEvent.config.getLimitedLoginEventAmount(configDays)
          internalItemId match {
            case 1 => //配布対象「ガチャ券」
              val message = if (configDays == 0) "限定ログボ！"
              else "限定ログボ" + loginDays + "日目記念！"
              p.sendMessage("【" + message + "】" + amount + "個のガチャ券をプレゼント！")
              for (_ <- 1 to amount) {
                if (p.getInventory.contains(skull) || !Util.isPlayerInventoryFull(p)) Util.addItem(p, skull)
                else Util.dropItem(p, skull)
              }
            case 2 => //配布対象「未定」
            //配布処理記入場所
            //今後の追加のためのサンプルです。
          }
          configDays += loginDays
        } while ( {
          configDays == loginDays
        })
      }
      playerdata.LimitedLoginCount_$eq(loginDays)
    } catch {
      case e: ParseException =>
        e.printStackTrace()
    }
  }
}
