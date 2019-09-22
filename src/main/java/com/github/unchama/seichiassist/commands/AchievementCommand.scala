package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.EmptyEffect
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object AchievementCommand {
  private enum class AchievementOperation { GIVE, DEPRIVE }
  private enum class ScopeSpecification {
    USER, SERVER, WORLD;
  }
  object ScopeSpecification {
    def fromString(string: String) = when (string) {
      "user" => USER
      "server" => SERVER
      "world" => WORLD
      else => null
    }
  }

  private val descriptionPrintExecutor = EchoExecutor(
      List(
        s"${RED}/achievement [操作] [実績No] [スコープ指定子]",
          "[操作]にはgive(実績付与)またはdeprive(実績剥奪)のいずれかを入力することができます。",
          "[スコープ指定子]にはuser [ユーザー名], server, worldのいずれかを入力することができます。"
      ).asMessageEffect()
  )

  private val operationParser =
      parser { argument =>
        when (argument) {
          "give" => succeedWith(AchievementOperation.GIVE)
          "deprive" => succeedWith(AchievementOperation.DEPRIVE)
          else => failWith("操作はgive/depriveで与えてください。")
        }
      }

  /**
   * TODO
   * [旧実装](https://github.com/GiganticMinecraft/SeichiAssist/blob/310ee1f438cf7ca5b202392fd77826b2ed245a58/src/main/java/com/github/unchama/seichiassist/commands/legacy/UnlockAchievementCommand.java#L50-L53)
   * には実績の存在確認のロジックが入っていたが、これは必要であったか？
   */
  private val achievementNumberParser =
      Parsers.closedRangeInt(
          1000, 9999,
        s"${RED}操作の対象として指定できるのはNo1000～9999の実績です。".asMessageEffect()
      )

  private val scopeParser =
      parser { argument =>
        ScopeSpecification.fromString(argument)
            ?.let { succeedWith(it) }
        ?: failWith (s"${RED}スコープ指定子はuser [ユーザー名], server, worldのみ入力できます。")
      }

  val executor = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(
          List(operationParser, achievementNumberParser, scopeParser),
          onMissingArguments = descriptionPrintExecutor
      )
      .execution { context =>
        val sender = context.sender

        val operation = context.args.parsed[0].asInstanceOf[AchievementOperation]
        val achievementNumber = context.args.parsed[1].asInstanceOf[Int]

        val targetPlayerNames = when (context.args.parsed[2].asInstanceOf[ScopeSpecification]) {
          ScopeSpecification.USER => {
            val targetPlayerName =
                context.args.yetToBeParsed.firstOrNull()
            ?: return@execution s"${RED}プレーヤー名が未入力です。".asMessageEffect()

            List(targetPlayerName)
          }
          ScopeSpecification.SERVER => Bukkit.getServer().onlinePlayers.map { it.name }
          ScopeSpecification.WORLD => {
            if (sender is Player) sender.world.players.map { it.name }
            else return@execution "コンソール実行の場合は「world」処理は実行できません。".asMessageEffect()
          }
        }

        targetPlayerNames.forEach { playerName =>
          val targetPlayer = Bukkit.getPlayer(playerName)

          if (targetPlayer != null) {
            val playerData = SeichiAssist.playermap[targetPlayer.uniqueId]
            when (operation) {
              AchievementOperation.GIVE => playerData.tryForcefullyUnlockAchievement(achievementNumber)
              AchievementOperation.DEPRIVE => playerData.forcefullyDepriveAchievement(achievementNumber)
            }
          } else {
            sender.sendMessage(s"$playerName は現在サーバーにログインしていません。")
            // TODO 実績付与予約システムに書き込むようにする
          }
        }

        EmptyEffect
      }
      .build()
      .asNonBlockingTabExecutor()
}