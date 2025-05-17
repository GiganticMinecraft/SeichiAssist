package com.github.unchama.seichiassist.task

import cats.Monad
import cats.effect.Sync
import com.github.unchama.seichiassist.data.player.{NicknameStyle, PlayerData}
import com.github.unchama.seichiassist.seichiskill.effect.{
  ActiveSkillEffect,
  UnlockableActiveSkillEffect
}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.util.ActionStatus
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

import java.sql.{SQLException, Statement}

object PlayerDataSaveTask {

  /**
   * プレイヤーデータをDBに同期的に保存する処理 DBにセーブしたい値が増えた/減った場合は更新すること
   *
   * @param playerdata
   *   保存するプレーヤーデータ
   * @author
   *   unchama
   */
  def savePlayerData[F[_]: Sync](player: Player, playerdata: PlayerData): F[Unit] = {
    val databaseGateway = SeichiAssist.databaseGateway

    def updateActiveSkillEffectUnlockState(stmt: Statement): Unit = {
      val playerUuid = player.getUniqueId.toString
      val effectsObtained = playerdata.skillEffectState.obtainedEffects

      stmt.executeUpdate {
        s"delete from seichiassist.unlocked_active_skill_effect where player_uuid = '$playerUuid'"
      }

      if (effectsObtained.nonEmpty) {
        stmt.executeUpdate {
          val data =
            effectsObtained.map(e => s"('$playerUuid', '${e.entryName}')").mkString(",")

          s"insert into seichiassist.unlocked_active_skill_effect(player_uuid, effect_name) values $data"
        }
      }
    }

    def updateSeichiSkillUnlockState(stmt: Statement): Unit = {
      val playerUuid = player.getUniqueId.toString
      val skillsObtained = playerdata.skillState.get.unsafeRunSync().obtainedSkills

      stmt.executeUpdate {
        s"delete from seichiassist.unlocked_seichi_skill where player_uuid = '$playerUuid'"
      }

      if (skillsObtained.nonEmpty) {
        stmt.executeUpdate {
          val data = skillsObtained.map(e => s"('$playerUuid', '${e.entryName}')").mkString(",")

          s"insert into seichiassist.unlocked_seichi_skill(player_uuid, skill_name) values $data"
        }
      }
    }

    def updatePlayerDataColumns(stmt: Statement): Unit = {
      val playerUuid = player.getUniqueId.toString

      // 実績のフラグ(BitSet)保存用変換処理
      val flagString = playerdata.TitleFlags.toBitMask.map(_.toHexString).mkString(",")

      val skillState = playerdata.skillState.get.unsafeRunSync()

      val command = {
        ("update seichiassist.playerdata set"
          + " name = '" + playerdata.name + "'"

          + ",serialized_usage_mode = " + skillState.usageMode.value
          + ",selected_effect = " + {
            playerdata.skillEffectState.selection match {
              case effect: UnlockableActiveSkillEffect => s"'${effect.entryName}'"
              case ActiveSkillEffect.NoEffect          => "null"
            }
          }
          + ",selected_active_skill = " + skillState
            .activeSkill
            .map(skill => s"'${skill.entryName}'")
            .getOrElse("null")
          + ",selected_assault_skill = " + skillState
            .assaultSkill
            .map(skill => s"'${skill.entryName}'")
            .getOrElse("null")

          + ",playtick = " + playerdata.playTick
          + ",killlogflag = " + playerdata.settings.shouldDisplayDeathMessages
          + ",worldguardlogflag = " + playerdata.settings.shouldDisplayWorldGuardLogs

          + ",multipleidbreakflag = " + playerdata
            .settings
            .performMultipleIDBlockBreakWhenOutsideSeichiWorld

          + ",pvpflag = " + playerdata.settings.pvpflag
          + ",totalexp = " + playerdata.totalexp
          + ",everysound = " + playerdata
            .settings
            .getBroadcastMutingSettings
            .unsafeRunSync()
            .shouldMuteSounds
          + ",everymessage = " + playerdata
            .settings
            .getBroadcastMutingSettings
            .unsafeRunSync()
            .shouldMuteMessages

          + ",displayTypeLv = " + (playerdata.settings.nickname.style == NicknameStyle.Level)
          + ",displayTitle1No = " + playerdata.settings.nickname.id1
          + ",displayTitle2No = " + playerdata.settings.nickname.id2
          + ",displayTitle3No = " + playerdata.settings.nickname.id3
          + ",giveachvNo = " + playerdata.giveachvNo
          + ",achvPointMAX = " + playerdata.achievePoint.fromUnlockedAchievements
          + ",achvPointUSE = " + playerdata.achievePoint.used
          + ",achvChangenum = " + playerdata.achievePoint.conversionCount

          + ",lastcheckdate = '" + playerdata.lastcheckdate + "'"
          + ",ChainJoin = " + playerdata.loginStatus.consecutiveLoginDays
          + ",TotalJoin = " + playerdata.loginStatus.totalLoginDay
          + ",LimitedLoginCount = " + playerdata.LimitedLoginCount

          + ",GBstage = " + playerdata.giganticBerserk.stage
          + ",GBexp = " + playerdata.giganticBerserk.exp
          + ",GBlevel = " + playerdata.giganticBerserk.level
          + ",isGBStageUp = " + playerdata.giganticBerserk.canEvolve
          + ",TitleFlags = '" + flagString + "'"

          + " where uuid = '" + playerUuid + "'")
      }

      stmt.executeUpdate(command)
    }

    def executeUpdate(): ActionStatus = {
      try {
        // sqlコネクションチェック
        databaseGateway.ensureConnection()

        // 同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
        val localStatement = databaseGateway.con.createStatement()
        updateActiveSkillEffectUnlockState(localStatement)
        updateSeichiSkillUnlockState(localStatement)
        updatePlayerDataColumns(localStatement)
        ActionStatus.Ok
      } catch {
        case exception: SQLException =>
          println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
          exception.printStackTrace()
          ActionStatus.Fail
      }
    }

    val commitUpdate: F[ActionStatus] = Sync[F].delay(executeUpdate())

    import cats.implicits._

    Monad[F].tailRecM(3) { remaining =>
      if (remaining == 0) {
        Sync[F]
          .delay {
            println(s"$RED${playerdata.name}のプレイヤーデータ保存失敗")
          }
          .as(Right(()))
      } else {
        commitUpdate.flatMap { result =>
          if (result == ActionStatus.Ok) {
            Sync[F]
              .delay {
                println(s"$GREEN${player.getName}のプレイヤーデータ保存完了")
              }
              .as(Right(()))
          } else {
            Monad[F].pure(Left(remaining - 1))
          }
        }
      }
    }
  }
}
