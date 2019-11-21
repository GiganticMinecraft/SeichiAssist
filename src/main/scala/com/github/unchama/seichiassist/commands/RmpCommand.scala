package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers._
import com.github.unchama.contextualexecutor.builder.TypeAliases.ResponseEffectOrResult
import com.github.unchama.contextualexecutor.builder.{ArgumentParserScope, ContextualExecutorBuilder}
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, ConsoleCommandSender, TabExecutor}
import org.bukkit.{Bukkit, World}

import scala.jdk.CollectionConverters._

object RmpCommand {
  import ArgumentParserScope._
  import targetedeffect.syntax._

  private val printDescriptionExecutor = new EchoExecutor(
    List(
      s"$RED/rmp remove [world名] [日数]",
      "全Ownerが[日数]間ログインしていないRegionを削除します(整地ワールドのみ)",
      "",
      s"$RED/rmp list [world名] [日数]",
      "全Ownerが[日数]間ログインしていないRegionを表示します"
    ).asMessageEffect()
  )
  private val argsAndSenderConfiguredBuilder = ContextualExecutorBuilder.beginConfiguration()
    .refineSenderWithError[ConsoleCommandSender](s"${GREEN}このコマンドはコンソールから実行してください")
    .argumentsParsers(List(
      arg => {
        Bukkit.getWorld(arg) match {
          case world: World => succeedWith(world)
          case _ => failWith(s"存在しないワールドです: $arg")
        }
      },
      nonNegativeInteger(s"$RED[日数]には非負整数を入力してください".asMessageEffect())
    ), onMissingArguments = printDescriptionExecutor)
  private val removeExecutor = argsAndSenderConfiguredBuilder
    .execution { context =>
      val world = context.args.parsed(0).asInstanceOf[World]
      val days = context.args.parsed(1).asInstanceOf[Int]

      val isSeichiWorldWithWGRegionsOption = ManagedWorld.fromBukkitWorld(world).map(_.isSeichiWorldWithWGRegions)

      def execute(): TargetedEffect[ConsoleCommandSender] = {
        isSeichiWorldWithWGRegionsOption match {
          case None | Some(false) => return "removeコマンドは保護をかけて整地する整地ワールドでのみ使用出来ます".asMessageEffect()
          case Some(true) =>
        }

        // 削除処理
        getOldRegionsIn(world, days).map { removalTargets =>
          removalTargets.foreach { target =>
            ExternalPlugins.getWorldGuard.getRegionContainer.get(world).removeRegion(target.getId)
          }

          // メッセージ生成
          if (removalTargets.isEmpty) {
            s"${GREEN}該当Regionは存在しません".asMessageEffect()
          } else {
            removalTargets.map { target =>
              s"$YELLOW[rmp] Deleted Region => ${world.getName}.${target.getId}".asMessageEffect()
            }.asSequentialEffect()
          }
        }.merge
      }

      IO(execute())
    }
    .build()

  private val listExecutor = argsAndSenderConfiguredBuilder
    .execution { context =>
      val world = context.args.parsed(0).asInstanceOf[World]
      val days = context.args.parsed(1).asInstanceOf[Int]

      IO {
        getOldRegionsIn(world, days).map { removalTargets =>
          if (removalTargets.isEmpty) {
            s"${GREEN}該当Regionは存在しません".asMessageEffect()
          } else {
            removalTargets
              .map { target =>
                s"$GREEN[rmp] List Region => ${world.getName}.${target.getId}".asMessageEffect()
              }
              .asSequentialEffect()
          }
        }.merge
      }
    }
    .build()

  private def getOldRegionsIn(world: World, daysThreshold: Int): ResponseEffectOrResult[CommandSender, List[ProtectedRegion]] = {
    val databaseGateway = SeichiAssist.databaseGateway

    val leavers = databaseGateway.playerDataManipulator.selectLeaversUUIDs(daysThreshold)
    if (leavers == null) {
      return Left(s"${RED}データベースアクセスに失敗しました。".asMessageEffect())
    }

    val regions = ExternalPlugins.getWorldGuard.getRegionContainer.get(world).getRegions.asScala

    val oldRegions = regions.values.filter { region =>
      region.getId != "__global__" && region.getId != "spawn" &&
        region.getOwners.getUniqueIds.asScala.forall(leavers.contains(_))
    }.toList

    Right(oldRegions)
  }

  val executor: TabExecutor =
    BranchedExecutor(
      Map(
        "remove" -> removeExecutor,
        "list" -> listExecutor
      ),
      whenArgInsufficient = Some(printDescriptionExecutor),
      whenBranchNotFound = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
}
