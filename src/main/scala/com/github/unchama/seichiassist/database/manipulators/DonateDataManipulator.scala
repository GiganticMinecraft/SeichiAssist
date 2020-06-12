package com.github.unchama.seichiassist.database.manipulators

import cats.effect.IO
import com.github.unchama.generic.effect.SyncExtra
import com.github.unchama.seichiassist.database.manipulators.DonateDataManipulator.{Obtained, PremiumPointTransaction, Used}
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect
import com.github.unchama.util.ActionStatus
import org.bukkit.entity.Player

class DonateDataManipulator(private val gateway: DatabaseGateway) {

  import com.github.unchama.util.syntax.ResultSetSyntax._

  private def tableReference: String = s"${gateway.databaseName}.${DatabaseConstants.DONATEDATA_TABLENAME}"

  def recordPremiumEffectPurchase(player: Player, effect: ActiveSkillPremiumEffect): IO[ActionStatus] = {
    val command =
      s"insert into $tableReference (playername,playeruuid,effectname,usepoint,date) " +
        s"value('${player.getName}','${player.getUniqueId.toString}','${effect.entryName}',${effect.usePoint},cast(now() as datetime))"

    IO { gateway.executeUpdate(command) }
  }

  def addDonate(name: String, point: Int): ActionStatus = {
    val command = ("insert into " + tableReference
      + " (playername,getpoint,date) "
      + "value("
      + "'" + name + "',"
      + point + ","
      + "cast( now() as datetime )"
      + ")")
    gateway.executeUpdate(command)
  }

  def loadTransactionHistoryFor(player: Player): IO[List[PremiumPointTransaction]] = {
    val command = s"select * from $tableReference where playername like '${player.getName}'"

    SyncExtra.recoverWithStackTrace(
      "プレミアムエフェクト購入のトランザクション履歴の読み込みに失敗しました。",
      List(),
      IO {
        gateway.executeQuery(command).recordIteration { lrs =>
          //ポイント購入の処理
          val getPoint = lrs.getInt("getpoint")
          val usePoint = lrs.getInt("usepoint")
          val date = lrs.getString("date")

          if (getPoint > 0) {
            Obtained(getPoint, date)
          } else if (usePoint > 0) {
            val effectName = lrs.getString("effectname")
            val nameOrEffect = ActiveSkillPremiumEffect.withNameOption(effectName).toRight(effectName)
            Used(usePoint, date, nameOrEffect)
          } else {
            throw new IllegalStateException("usepointまたはgetpointが正である必要があります")
          }
        }
      }
    )
  }

  def currentPremiumPointFor(player: Player): IO[Int] = {
    loadTransactionHistoryFor(player).map { history =>
      history.map {
        case Obtained(p, _) => p
        case Used(p, _, _) => -p
      }.sum
    }
  }
}

object DonateDataManipulator {
  sealed trait PremiumPointTransaction
  case class Obtained(amount: Int, date: String) extends PremiumPointTransaction
  case class Used(amount: Int, date: String, forPurchaseOf: Either[String, ActiveSkillPremiumEffect]) extends PremiumPointTransaction
}