package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ArgumentParserScope.ScopeProvider.parser
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.effect.TargetedEffect
import com.github.unchama.effect.asMessageEffect
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object RegionOwnerTransferCommand {
  @Suppress("RedundantSuspendModifier")
  private suspend fun attemptRegionTransfer(donner: Player, recipient: Player, region: ProtectedRegion): TargetedEffect<Player> {
    val owners = region.owners

    if (!owners.contains(donner.uniqueId)) {
      return "オーナーではないため権限を譲渡できません。".asMessageEffect()
    }

    if (owners.size() != 1) {
      return "オーナーが複数人いるため権限を譲渡できません。".asMessageEffect()
    }

    owners.clear()
    owners.addPlayer(recipient.uniqueId)

    return "${recipient.name}に${region.id}のオーナー権限を譲渡しました。".asMessageEffect()
  }

  val executor = playerCommandBuilder
      .argumentsParsers(listOf(
          Parsers.identity,
          parser { recipientName ->
            val recipient = Bukkit.getPlayer(recipientName)

            if (recipient != null) {
              succeedWith(recipient)
            } else {
              failWith("${recipientName}というプレイヤーはサーバーに参加したことがありません。")
            }
          }
      ))
      .execution { context ->
        val regionName = context.args.parsed[0] as String
        val newOwner = context.args.parsed[1] as Player

        val sender = context.sender

        val region = WorldGuardPlugin.inst().getRegionManager(sender.world).getRegion(regionName)
            ?: return@execution "${regionName}という名前の保護は存在しません。".asMessageEffect()

        attemptRegionTransfer(sender, newOwner, region)
      }
      .build()
      .asNonBlockingTabExecutor()
}