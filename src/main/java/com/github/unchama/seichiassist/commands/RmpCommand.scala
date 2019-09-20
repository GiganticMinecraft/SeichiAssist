package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.command.{CommandSender, ConsoleCommandSender}
import org.bukkit.{Bukkit, World}

object RmpCommand {
  private val printDescriptionExecutor = EchoExecutor(
    listOf(
        s"${ChatColor.RED}/rmp remove [world名] [日数]",
        "全Ownerが[日数]間ログインしていないRegionを削除します(整地ワールドのみ)",
        "",
        s"${ChatColor.RED}/rmp list [world名] [日数]",
        "全Ownerが[日数]間ログインしていないRegionを表示します"
    ).asMessageEffect()
  )

  private val argsAndSenderConfiguredBuilder = ContextualExecutorBuilder.beginConfiguration()
      .refineSenderWithError[ConsoleCommandSender](s"${ChatColor.GREEN}このコマンドはコンソールから実行してください")
      .argumentsParsers(listOf(
          parser {
            Bukkit.getWorld(it)
                ?.let { world => succeedWith(world) }
                ?: failWith(s"存在しないワールドです: $it")
          },
          Parsers.nonNegativeInteger(s"${ChatColor.RED}[日数]には非負整数を入力してください".asMessageEffect())
      ), onMissingArguments = printDescriptionExecutor)

  private suspend def getOldRegionsIn(world: World, daysThreshold: Int): ResponseEffectOrResult[CommandSender, List[ProtectedRegion]] {
    val databaseGateway = SeichiAssist.databaseGateway

    val leavers = databaseGateway.playerDataManipulator.selectLeaversUUIDs(daysThreshold)
        ?: return s"${ChatColor.RED}データベースアクセスに失敗しました。".asMessageEffect().left()

    val regions = ExternalPlugins.getWorldGuard().regionContainer.get(world)!!.regions.toMap()
    val oldRegions = regions.values.filter { region =>
      region.id != "__global__" && region.id != "spawn"
          && region.owners.uniqueIds.all { leavers.contains(it) }
    }

    return oldRegions.right()
  }

  private val removeExecutor = argsAndSenderConfiguredBuilder
      .execution { context =>
        val world = context.args.parsed[0] as World
        val days = context.args.parsed[1] as Int

        if (ManagedWorld.fromBukkitWorld(world)?.isSeichiWorldWithWGRegions == false) {
          return@execution "removeコマンドは保護をかけて整地する整地ワールドでのみ使用出来ます".asMessageEffect()
        }

        getOldRegionsIn(world, days).map { removalTargets =>
          // 削除処理
          removalTargets.forEach { target =>
            ExternalPlugins.getWorldGuard().regionContainer.get(world)!!.removeRegion(target.id)
          }

          // メッセージ生成
          if (removalTargets.isEmpty()) {
            s"${ChatColor.GREEN}該当Regionは存在しません".asMessageEffect()
          } else {
            removalTargets
                .map { s"${ChatColor.YELLOW}[rmp] Deleted Region => ${world.name}.${it.id}".asMessageEffect() }
                .asSequentialEffect()
          }
        }.merge()
      }
      .build()

  private val listExecutor = argsAndSenderConfiguredBuilder
      .execution { context =>
        val world = context.args.parsed[0] as World
        val days = context.args.parsed[1] as Int

        getOldRegionsIn(world, days).map { removalTargets =>
          if (removalTargets.isEmpty()) {
            s"${ChatColor.GREEN}該当Regionは存在しません".asMessageEffect()
          } else {
            removalTargets
                .map { (s"${ChatColor.GREEN}[rmp] List Region => ${world.name}.${it.id}").asMessageEffect() }
                .asSequentialEffect()
          }
        }.merge()
      }
      .build()

  val executor =
      BranchedExecutor(
          mapOf(
              "remove" to removeExecutor,
              "list" to listExecutor
          ),
          whenArgInsufficient = printDescriptionExecutor,
          whenBranchNotFound = printDescriptionExecutor
      )
      .asNonBlockingTabExecutor()
}
