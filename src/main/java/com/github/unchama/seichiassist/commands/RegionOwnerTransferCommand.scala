package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object RegionOwnerTransferCommand {
  @Suppress("RedundantSuspendModifier")
  private @SuspendingMethod def attemptRegionTransfer(donner: Player, recipient: Player, region: ProtectedRegion): TargetedEffect[Player] = {
    val owners = region.owners

    if (!owners.contains(donner.uniqueId)) {
      return "オーナーではないため権限を譲渡できません。".asMessageEffect()
    }

    if (owners.size() != 1) {
      return "オーナーが複数人いるため権限を譲渡できません。".asMessageEffect()
    }

    owners.clear()
    owners.addPlayer(recipient.uniqueId)

    return s"${recipient.name}に${region.id}のオーナー権限を譲渡しました。".asMessageEffect()
  }

  val executor = playerCommandBuilder
      .argumentsParsers(List(
          Parsers.identity,
          parser { recipientName =>
            val recipient = Bukkit.getPlayer(recipientName)

            if (recipient != null) {
              succeedWith(recipient)
            } else {
              failWith(s"${recipientName}というプレイヤーはサーバーに参加したことがありません。")
            }
          }
      ))
      .execution { context =>
        val regionName = context.args.parsed[0].asInstanceOf[String]
        val newOwner = context.args.parsed[1].asInstanceOf[Player]

        val sender = context.sender

        val region = WorldGuardPlugin.inst().getRegionManager(sender.world).getRegion(regionName)
            ?: return@execution s"${regionName}という名前の保護は存在しません。".asMessageEffect()

        attemptRegionTransfer(sender, newOwner, region)
      }
      .build()
      .asNonBlockingTabExecutor()
}