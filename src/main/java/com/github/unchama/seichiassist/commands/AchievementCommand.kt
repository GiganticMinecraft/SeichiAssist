package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.ScopeProvider.parser
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.messaging.EmptyMessage
import com.github.unchama.messaging.asResponseToSender
import com.github.unchama.seichiassist.SeichiAssist
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object AchievementCommand {
  private enum class AchievementOperation { GIVE, DEPRIVE }
  private enum class ScopeSpecification {
    USER, SERVER, WORLD;
    companion object {
      fun fromString(string: String) = when (string) {
        "user" -> USER
        "server" -> SERVER
        "world" -> WORLD
        else -> null
      }
    }
  }

  private val descriptionPrintExecutor = EchoExecutor(
      listOf(
          "${ChatColor.RED}/achievement <操作> <実績No> <スコープ指定子>",
          "<操作>にはgive(実績付与)またはdeprive(実績剥奪)のいずれかを入力することができます。",
          "<スコープ指定子>にはuser <ユーザー名>, server, worldのいずれかを入力することができます。"
      ).asResponseToSender()
  )

  private val operationParser =
      parser { argument ->
        when (argument) {
          "give" -> succeedWith(AchievementOperation.GIVE)
          "deprive" -> succeedWith(AchievementOperation.DEPRIVE)
          else -> failWith("操作はgive/depriveで与えてください。")
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
          "${ChatColor.RED}操作の対象として指定できるのはNo1000～9999の実績です。".asResponseToSender()
      )

  private val scopeParser =
      parser { argument ->
        ScopeSpecification.fromString(argument)
            ?.let { succeedWith(it) }
            ?: failWith("${ChatColor.RED}スコープ指定子はuser <ユーザー名>, server, worldのみ入力できます。")
      }

  val executor = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(
          listOf(operationParser, achievementNumberParser, scopeParser),
          onMissingArguments = descriptionPrintExecutor
      )
      .execution { context ->
        val sender = context.sender

        val operation = context.args.parsed[0] as AchievementOperation
        val achievementNumber = context.args.parsed[1] as Int

        val targetPlayerNames = when (context.args.parsed[2] as ScopeSpecification) {
          ScopeSpecification.USER -> {
            val targetPlayerName =
                context.args.yetToBeParsed.firstOrNull()
                    ?: return@execution "${ChatColor.RED}プレーヤー名が未入力です。".asResponseToSender()

            listOf(targetPlayerName)
          }
          ScopeSpecification.SERVER -> Bukkit.getServer().onlinePlayers.map { it.name }
          ScopeSpecification.WORLD -> {
            if (sender is Player) sender.world.players.map { it.name }
            else return@execution "コンソール実行の場合は「world」処理は実行できません。".asResponseToSender()
          }
        }

        targetPlayerNames.forEach { playerName ->
          val targetPlayer = Bukkit.getPlayer(playerName)

          if (targetPlayer != null) {
            val playerData = SeichiAssist.playermap[targetPlayer.uniqueId]!!
            when (operation) {
              AchievementOperation.GIVE -> playerData.tryForcefullyUnlockAchievement(achievementNumber)
              AchievementOperation.DEPRIVE -> playerData.forcefullyDepriveAchievement(achievementNumber)
            }
          } else {
            sender.sendMessage("$playerName は現在サーバーにログインしていません。")
            // TODO 実績付与予約システムに書き込むようにする
          }
        }

        EmptyMessage
      }
      .build()
      .asNonBlockingTabExecutor()
}