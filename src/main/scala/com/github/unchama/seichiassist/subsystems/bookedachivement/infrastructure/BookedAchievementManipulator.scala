package com.github.unchama.seichiassist.subsystems.bookedachivement.infrastructure

import cats.data.EitherT
import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ResponseEffectOrResult
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.util.ActionStatus
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BookedAchievementManipulator(private val gateway: DatabaseGateway) {
  private val tableReference: String = s"${gateway.databaseName}.${DatabaseConstants.BOOKED_ACHIEVEMENT_TABLENAME}"

  /**
   * 指定した `achievementId` の実績をプレイヤーの `uuid` とともに記録します.
   */
  def bookAchievement(uuid: String, achievementId: Int): IO[ResponseEffectOrResult[CommandSender, Unit]] = {
    EitherT(IO {
      val insertCommand =
        s"insert into $tableReference (player_uuid, achievement_id, is_received) values ('$uuid', $achievementId, false)"

      if (gateway.executeUpdate(insertCommand) == ActionStatus.Fail) {
        Bukkit.getLogger.warning("SQL failed. => saveBookedAchievement")
        Left(MessageEffect(s"$RED[実績予約システム] 実績の予約に失敗しました。"))
      } else {
        Right()
      }
    }).value
  }

  import com.github.unchama.util.syntax.ResultSetSyntax._

  /**
   * `player` がまだ受け取っていない予約済み実績の番号を返します.
   */
  def loadNotGivenBookedAchievementsOf(player: Player): IO[ResponseEffectOrResult[CommandSender, List[Int]]] = {
    EitherT.right(IO {
      val selectCommand =
        s"select achievement_id from $tableReference where player_uuid = '${player.getUniqueId}' and is_received = false"

      gateway.executeQuery(selectCommand).recordIteration(rs => rs.getInt("achievement_id"))
    }).value
  }

  /**
   * `player` がまだ受け取っていない予約済みの実績を受け取り済みにします.
   */
  def makeAllOfAchivementsReceived(player: Player): IO[ResponseEffectOrResult[CommandSender, Unit]] = {
    EitherT(IO {
      val updateCommand =
        s"update $tableReference set is_received = true where player_uuid = '${player.getUniqueId}' and is_received = false"

      if (gateway.executeUpdate(updateCommand) == ActionStatus.Fail) {
        Bukkit.getLogger.warning("SQL failed. => makeAllOfAchievementsReceived")
        Left(MessageEffect(s"$RED[実績予約システム] 実績の受け取り状態の変更に失敗しました。"))
      } else {
        Right()
      }
    })
  }.value

  /**
   * プレイヤー名が `playerName` なプレイヤーの `uuid` を探します.
   */
  def findPlayerUuid(playerName: String): IO[ResponseEffectOrResult[CommandSender, String]] = {
    EitherT(IO {
      val selectCommand =
        s"select uuid, name from ${gateway.databaseName}.${DatabaseConstants.PLAYERDATA_TABLENAME}"

      val maybeUuid = gateway.executeQuery(selectCommand)
        .recordIteration(rs => (rs.getString("name"), rs.getString("uuid")))
        .find { case (name, _) =>
          name == playerName
        }
        .map { case (_, uuid) =>
          uuid
        }
      if (maybeUuid.isEmpty) {
        Bukkit.getLogger.warning("SQL failed. => loadPlayerUuid")
        Left(MessageEffect(s"$RED[実績予約システム] プレイヤー名($playerName)のUUIDを発見できませんでした。"))
      } else {
        Right(maybeUuid.get)
      }
    }).value
  }
}
