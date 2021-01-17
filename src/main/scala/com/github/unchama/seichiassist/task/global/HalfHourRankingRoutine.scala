package com.github.unchama.seichiassist.task.global

import cats.effect.{IO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

import scala.concurrent.duration.FiniteDuration

object HalfHourRankingRoutine {
  def apply()(implicit context: RepeatingTaskContext): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      if (SeichiAssist.DEBUG) 20.seconds else 30.minutes
    }

    val routineAction: IO[Unit] = IO {
      Util.sendEveryMessageWithoutIgnore("--------------30分間整地ランキング--------------")

      var totalBreakCount = 0

      // playermapに入っているすべてのプレイヤーデータについて処理
      SeichiAssist.playermap.values.foreach { playerData =>
        val player = Bukkit.getPlayer(playerData.uuid)
        val halfHourBlock = playerData.halfhourblock

        //プレイヤーがオンラインの時の処理
        if (player != null) {
          val totalBreakNum = playerData.totalbreaknum

          halfHourBlock.after = totalBreakNum
          halfHourBlock.setIncrease()
          halfHourBlock.before = totalBreakNum

          //increaseが0超過の場合プレイヤー個人に個人整地量を通知
          if (halfHourBlock.increase > 0) {
            player.sendMessage(s"あなたの整地量は $AQUA${halfHourBlock.increase}$WHITE でした")
          }
        } else {
          //ﾌﾟﾚｲﾔｰがオフラインの時の処理
          //前回との差を０に設定
          halfHourBlock.increase = 0
        }

        //allに30分間の採掘量を加算
        totalBreakCount += halfHourBlock.increase.toInt
      }

      // ここで、0 => 第一位、 1 => 第二位、・・・n => 第(n+1)位にする (つまり降順)
      val sortedPlayerData = SeichiAssist.playermap.values.toList
        .filter {
          _.halfhourblock.increase != 0L
        }
        .sortBy {
          _.halfhourblock.increase
        }
        .reverse

      Util.sendEveryMessageWithoutIgnore("全体の整地量は " + AQUA + totalBreakCount + WHITE + " でした")

      sortedPlayerData.headOption.foreach { _ =>
        val rankingPositionColor = List(DARK_PURPLE, BLUE, DARK_AQUA)

        sortedPlayerData
          .take(3) // 1から3位まで
          .zip(rankingPositionColor)
          .zipWithIndex
          .foreach { case ((playerData, positionColor), index) =>
            val starLevel = playerData.starLevels.total()
            val playerNameText =
              if(starLevel == 0)
                s"$positionColor[ Lv${playerData.level} ]${playerData.lowercaseName}$WHITE"
              else
                s"$positionColor[ Lv${playerData.level}☆$starLevel ]${playerData.lowercaseName}$WHITE"
            val increaseAmountText = s"$AQUA${playerData.halfhourblock.increase}$WHITE"

            Util.sendEveryMessageWithoutIgnore(s"整地量第${index + 1}位は${playerNameText}で${increaseAmountText}でした")
          }
      }

      Util.sendEveryMessageWithoutIgnore("--------------------------------------------------")
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
