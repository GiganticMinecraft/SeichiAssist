package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers._
import com.github.unchama.contextualexecutor.builder.{
  ContextualExecutorBuilder,
  ParserResponse,
  ResponseEffectOrResult
}
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.util.external.WorldGuardWrapper
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, ConsoleCommandSender, TabExecutor}
import org.bukkit.{Bukkit, World}
import shapeless.{::, HNil}

import scala.jdk.CollectionConverters._

object RmpCommand {
  import ParserResponse._

  private val printDescriptionExecutor = new EchoExecutor(MessageEffect {
    List(
      s"$RED/rmp remove [world名] [日数]",
      "全Ownerが[日数]間ログインしていないRegionを削除します(整地ワールドのみ)",
      "",
      s"$RED/rmp list [world名] [日数]",
      "全Ownerが[日数]間ログインしていないRegionを表示します"
    )
  })

  private val argsAndSenderConfiguredBuilder = ContextualExecutorBuilder
    .beginConfiguration
    .refineSenderWithError[ConsoleCommandSender](s"${GREEN}このコマンドはコンソールから実行してください")
    .thenParse((arg: String) => {
      Bukkit.getWorld(arg) match {
        case world: World if world != null => succeedWith(world)
        case _                             => failWith(s"存在しないワールドです: $arg")
      }
    })
    .thenParse(nonNegativeInteger(MessageEffect(s"$RED[日数]には非負整数を入力してください")))
    .ifArgumentsMissing(printDescriptionExecutor)

  private val removeExecutor = argsAndSenderConfiguredBuilder.buildWith { context =>
    val (world :: days :: HNil) = context.args.parsed
    removeRegions(world, days.value)
  }

  private val listExecutor = argsAndSenderConfiguredBuilder.buildWith { context =>
    val (world :: days :: HNil) = context.args.parsed

    IO {
      getOldRegionsIn(world, days.value).map { removalTargets =>
        if (removalTargets.isEmpty) {
          MessageEffect(s"${GREEN}該当Regionは存在しません")
        } else {
          targetedeffect.SequentialEffect(removalTargets.map { target =>
            MessageEffect(s"$GREEN[rmp] List Region => ${world.getName}.${target.getId}")
          })
        }
      }.merge
    }
  }

  private def removeRegions(world: World, days: Int): IO[TargetedEffect[CommandSender]] = IO {
    val isSeichiWorldWithWGRegionsOption =
      ManagedWorld.fromBukkitWorld(world).map(_.isSeichiWorldWithWGRegions)

    if (Bukkit.getServer.hasWhitelist) {
      MessageEffect("ホワイトリストが有効なため、rmpコマンドは利用できません。")
    } else {
      isSeichiWorldWithWGRegionsOption match {
        case None | Some(false) => MessageEffect(s"第1整地以外の保護をかけて整地する整地ワールドでのみ使用出来ます")
        case Some(true)         =>
          getOldRegionsIn(world, days).map { removalTargets =>
            removalTargets.foreach(WorldGuardWrapper.removeByProtectedRegionRegion(world, _))

            // メッセージ生成
            if (removalTargets.isEmpty) {
              MessageEffect(s"${GREEN}該当Regionは存在しません")
            } else {
              targetedeffect.SequentialEffect(removalTargets.map { target =>
                MessageEffect(
                  s"$YELLOW[rmp] Deleted Region => ${world.getName}.${target.getId}"
                )
              })
            }
          }.merge
      }
    }
  }

  private def getOldRegionsIn(
    world: World,
    daysThreshold: Int
  ): ResponseEffectOrResult[CommandSender, List[ProtectedRegion]] = {
    val databaseGateway = SeichiAssist.databaseGateway

    val leavers = databaseGateway.playerDataManipulator.selectLeaversUUIDs(daysThreshold)
    if (leavers == null) {
      return Left(MessageEffect(s"${RED}データベースアクセスに失敗しました。"))
    }

    val regions = WorldGuardWrapper.getRegions(world)

    val oldRegions = regions.filter { region =>
      region.getId != "spawn" && region
        .getOwners
        .getUniqueIds
        .asScala
        .forall(leavers.contains(_))
    }

    Right(oldRegions)
  }

  val executor: TabExecutor =
    BranchedExecutor(
      Map("remove" -> removeExecutor, "list" -> listExecutor),
      whenArgInsufficient = Some(printDescriptionExecutor),
      whenBranchNotFound = Some(printDescriptionExecutor)
    ).asNonBlockingTabExecutor()
}
