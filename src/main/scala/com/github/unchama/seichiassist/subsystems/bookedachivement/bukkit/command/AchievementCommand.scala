package com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.command

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.AchievementOperation
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.auto._
import org.bukkit.Bukkit
import org.bukkit.ChatColor.RED
import org.bukkit.command.{CommandSender, TabExecutor}
import org.bukkit.entity.Player
import shapeless.HNil

import scala.jdk.CollectionConverters._

/**
 * Created by karayuu on 2020/11/21
 */
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
    Parsers.closedRangeInt[Int Refined Positive](
      1000,
      9999,
      MessageEffect(s"${RED}操作の対象として指定できるのはNo1000～9999の実績です。")
    )
  private val scopeParser = Parsers.fromOptionParser(
    ScopeSpecification.fromString,
    MessageEffect(s"${RED}スコープ指定子はuser [ユーザー名], server, worldのみ入力できます。")
  )

  sealed trait ScopeSpecification

  object ScopeSpecification {

    def fromString(string: String): Option[ScopeSpecification] = string match {
      case "user"   => Some(USER)
      case "server" => Some(SERVER)
      case "world"  => Some(WORLD)
      case _        => None
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

  // TODO: パーサーを分けるべき
  def executor[F[_]: ConcurrentEffect](
    implicit service: AchievementBookingService[F]
  ): TabExecutor = ContextualExecutorBuilder
    .beginConfiguration
    .thenParse(operationParser)
    .thenParse(achievementNumberParser)
    .thenParse(scopeParser)
    .ifArgumentsMissing(descriptionPrintExecutor)
    .buildWithExecutionF { context =>
      import shapeless.::

      val sender = context.sender

      val operation :: achievementNumber :: scopeSpec :: HNil = context.args.parsed

      def execution(): IO[TargetedEffect[CommandSender]] = {
        val targetPlayerNames: List[String] =
          scopeSpec match {
            case ScopeSpecification.USER =>
              val targetPlayerName =
                context
                  .args
                  .yetToBeParsed
                  .headOption
                  .getOrElse(return IO.pure(MessageEffect(s"${RED}プレーヤー名が未入力です。")))
              List(targetPlayerName)
            case ScopeSpecification.SERVER =>
              Bukkit.getServer.getOnlinePlayers.asScala.map(_.getName).toList
            case ScopeSpecification.WORLD =>
              sender match {
                case player: Player => player.getWorld.getPlayers.asScala.map(_.getName).toList
                case _ => return IO.pure(MessageEffect("コンソール実行の場合は「world」処理は実行できません。"))
              }
          }

        import cats.effect.implicits._
        import cats.implicits._

        targetPlayerNames.map { playerName =>
          IO {
            Option(Bukkit.getPlayer(playerName)) match {
              case Some(player) =>
                val playerData = SeichiAssist.playermap(player.getUniqueId)
                operation match {
                  case AchievementOperation.GIVE =>
                    playerData.tryForcefullyUnlockAchievement(achievementNumber)
                  case AchievementOperation.DEPRIVE =>
                    playerData.forcefullyDepriveAchievement(achievementNumber)
                }

              case None =>
                SequentialEffect(
                  Kleisli.liftF(
                    service
                      .writeAchivementId(playerName, achievementNumber, operation)
                      .start
                      .toIO
                      .as(())
                  ),
                  MessageEffect(
                    List(
                      s"$playerName の No.$achievementNumber の実績を${operation match {
                          case AchievementOperation.GIVE    => "付与"
                          case AchievementOperation.DEPRIVE => "剥奪"
                        }}します。",
                      s"$playerName は現在サーバーにログインしていません。\n予約システムに書き込みました。"
                    )
                  )
                )
            }
          }
        }.combineAll
      }

      execution().flatMap(_.apply(sender))
    }
    .asNonBlockingTabExecutor()
}
