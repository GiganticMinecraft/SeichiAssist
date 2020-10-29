package com.github.unchama.seichiassist.commands

import cats.data.EitherT
import cats.effect.{IO, SyncEffect, SyncIO}
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player

import scala.jdk.CollectionConverters._

object AchievementCommand {

  private val operationParser = Parsers.fromOptionParser(
    AchievementOperation.fromString,
    MessageEffect("操作はgive/depriveで与えてください。")
  )
  /**
   * TODO
   * [旧実装](https://github.com/GiganticMinecraft/SeichiAssist/blob/310ee1f438cf7ca5b202392fd77826b2ed245a58/src/main/java/com/github/unchama/seichiassist/commands/legacy/UnlockAchievementCommand.java#L50-L53)
   * には実績の存在確認のロジックが入っていたが、これは必要であったか？
   */
  private val achievementNumberParser =
    Parsers.closedRangeInt(
      1000, 9999,
      MessageEffect(s"${RED}操作の対象として指定できるのはNo1000～9999の実績です。")
    )
  private val scopeParser = Parsers.fromOptionParser(
    ScopeSpecification.fromString,
    MessageEffect(s"${RED}スコープ指定子はuser [ユーザー名], server, worldのみ入力できます。")
  )

  sealed trait AchievementOperation

  sealed trait ScopeSpecification

  object AchievementOperation {

    def fromString(string: String): Option[AchievementOperation] = string match {
      case "give" => Some(GIVE)
      case "deprive" => Some(DEPRIVE)
      case _ => None
    }

    case object GIVE extends AchievementOperation

    case object DEPRIVE extends AchievementOperation

  }

  object ScopeSpecification {

    def fromString(string: String): Option[ScopeSpecification] = string match {
      case "user" => Some(USER)
      case "server" => Some(SERVER)
      case "world" => Some(WORLD)
      case _ => None
    }

    case object USER extends ScopeSpecification

    case object SERVER extends ScopeSpecification

    case object WORLD extends ScopeSpecification

  }

  private val descriptionPrintExecutor = new EchoExecutor(
    MessageEffect(
      List(
        s"$RED/achievement [操作] [実績No] [スコープ指定子]",
        "[操作]にはgive(実績付与)またはdeprive(実績剥奪)のいずれかを入力することができます。",
        "[スコープ指定子]にはuser [ユーザー名], server, worldのいずれかを入力することができます。"
      )
    )
  )

  def executor[
    SyncContext[_] : SyncEffect
  ](implicit service: AchievementBookingService[SyncContext]): TabExecutor = ContextualExecutorBuilder.beginConfiguration()
    .argumentsParsers(
      List(operationParser, achievementNumberParser, scopeParser),
      onMissingArguments = descriptionPrintExecutor
    )
    .execution { context =>
      val sender = context.sender

      val operation = context.args.parsed.head.asInstanceOf[AchievementOperation]
      val achievementNumber = context.args.parsed(1).asInstanceOf[Int]

      def execution(): IO[TargetedEffect[CommandSender]] = {
        val targetPlayerNames: List[String] = context.args.parsed(2).asInstanceOf[ScopeSpecification] match {
          case ScopeSpecification.USER =>
            val targetPlayerName =
              context.args.yetToBeParsed.headOption
                .getOrElse(return IO.pure(MessageEffect(s"${RED}プレーヤー名が未入力です。")))
            List(targetPlayerName)
          case ScopeSpecification.SERVER => Bukkit.getServer.getOnlinePlayers.asScala.map(_.getName).toList
          case ScopeSpecification.WORLD =>
            sender match {
              case player: Player => player.getWorld.getPlayers.asScala.map(_.getName).toList
              case _ => return IO.pure(MessageEffect("コンソール実行の場合は「world」処理は実行できません。"))
            }
        }

        import cats.implicits._
        import cats.effect.implicits._

        targetPlayerNames.map { playerName =>
          IO {
            Option(Bukkit.getPlayer(playerName)) match {
              case Some(player) =>
                val playerData = SeichiAssist.playermap(player.getUniqueId)
                operation match {
                  case AchievementOperation.GIVE => playerData.tryForcefullyUnlockAchievement(achievementNumber)
                  case AchievementOperation.DEPRIVE => playerData.forcefullyDepriveAchievement(achievementNumber)
                }
              case None =>
                service.writeAchivementId(playerName, achievementNumber)
                  .runSync[SyncIO]
                  .unsafeRunSync() match {
                  case Left(errorMessage) => MessageEffect(errorMessage)
                  case Right(_) => MessageEffect(
                    s"$playerName は現在サーバーにログインしていません。\n予約システムに書き込みます。"
                  )
                }
            }
          }
        }.combineAll
      }

      execution()
    }
    .build()
    .asNonBlockingTabExecutor()
}
