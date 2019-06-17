package com.github.unchama.seichiassist.commands

import arrow.core.left
import arrow.core.right
import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.ScopeProvider.parser
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.builder.ResponseEffectOrResult
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.contextualexecutor.executors.EchoExecutor
import com.github.unchama.effect.asMessageEffect
import com.github.unchama.effect.ops.combineAll
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.external.ExternalPlugins
import com.github.unchama.util.data.merge
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

object RmpCommand {
  private val printDescriptionExecutor = EchoExecutor(
    listOf(
        "${ChatColor.RED}/rmp remove <world名> <日数>",
        "全Ownerが<日数>間ログインしていないRegionを削除します(整地ワールドのみ)",
        "",
        "${ChatColor.RED}/rmp list <world名> <日数>",
        "全Ownerが<日数>間ログインしていないRegionを表示します"
    ).asMessageEffect()
  )

  private val argsAndSenderConfiguredBuilder = ContextualExecutorBuilder.beginConfiguration()
      .refineSenderWithError<ConsoleCommandSender>("${ChatColor.GREEN}このコマンドはコンソールから実行してください")
      .argumentsParsers(listOf(
          parser {
            Bukkit.getWorld(it)
                ?.let { world -> succeedWith(world) }
                ?: failWith("存在しないワールドです: $it")
          },
          Parsers.nonNegativeInteger("${ChatColor.RED}<日数>には非負整数を入力してください".asMessageEffect())
      ), onMissingArguments = printDescriptionExecutor)

  private suspend fun getOldRegionsIn(world: World, daysThreshold: Int): ResponseEffectOrResult<CommandSender, List<ProtectedRegion>> {
    val databaseGateway = SeichiAssist.databaseGateway

    val leavers = databaseGateway.playerDataManipulator.selectLeaversUUIDs(daysThreshold)
        ?: return "${ChatColor.RED}データベースアクセスに失敗しました。".asMessageEffect().left()

    val regions = ExternalPlugins.getWorldGuard().regionContainer.get(world)!!.regions.toMap()
    val oldRegions = regions.values.filter { region ->
      region.id != "__global__" && region.id != "spawn"
          && region.owners.uniqueIds.all { leavers.contains(it) }
    }

    return oldRegions.right()
  }

  private val removeExecutor = argsAndSenderConfiguredBuilder
      .execution { context ->
        val world = context.args.parsed[0] as World
        val days = context.args.parsed[1] as Int

        if (!SeichiAssist.rgSeichiWorldlist.contains(world.name)) {
          return@execution "removeコマンドは保護をかけて整地する整地ワールドでのみ使用出来ます".asMessageEffect()
        }

        getOldRegionsIn(world, days).map { removalTargets ->
          // 削除処理
          removalTargets.forEach { target ->
            ExternalPlugins.getWorldGuard().regionContainer.get(world)!!.removeRegion(target.id)
          }

          // メッセージ生成
          if (removalTargets.isEmpty()) {
            "${ChatColor.GREEN}該当Regionは存在しません".asMessageEffect()
          } else {
            removalTargets
                .map { "${ChatColor.YELLOW}[rmp] Deleted Region -> ${world.name}.${it.id}".asMessageEffect() }
                .combineAll()
          }
        }.merge()
      }
      .build()

  private val listExecutor = argsAndSenderConfiguredBuilder
      .execution { context ->
        val world = context.args.parsed[0] as World
        val days = context.args.parsed[1] as Int

        getOldRegionsIn(world, days).map { removalTargets ->
          if (removalTargets.isEmpty()) {
            "${ChatColor.GREEN}該当Regionは存在しません".asMessageEffect()
          } else {
            removalTargets
                .map { ("${ChatColor.GREEN}[rmp] List Region -> ${world.name}.${it.id}").asMessageEffect() }
                .combineAll()
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
