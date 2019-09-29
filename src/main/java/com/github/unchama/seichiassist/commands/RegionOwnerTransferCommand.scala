package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.{ArgumentParserScope, Parsers}
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.MessageEffects._
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object RegionOwnerTransferCommand {
  @SuspendingMethod
  private def attemptRegionTransfer(donner: Player, recipient: Player, region: ProtectedRegion): TargetedEffect[Player] = {
    val owners = region.getOwners

    if (!owners.contains(donner.getUniqueId)) {
      return "オーナーではないため権限を譲渡できません。".asMessageEffect()
    }

    if (owners.size() != 1) {
      return "オーナーが複数人いるため権限を譲渡できません。".asMessageEffect()
    }

    owners.clear()
    owners.addPlayer(recipient.getUniqueId)

    s"${recipient.getName}に${region.getId}のオーナー権限を譲渡しました。".asMessageEffect()
  }

  import ArgumentParserScope._
  val executor: TabExecutor = playerCommandBuilder
    .argumentsParsers(List(
      Parsers.identity,
      recipientName => {
        Bukkit.getPlayer(recipientName) match {
          case recipient: Player => succeedWith(recipient)
          case _ => failWith(s"${recipientName}というプレイヤーはサーバーに参加したことがありません。")
        }
      }
    ))
    .execution { context =>
      val regionName = context.args.parsed[0].asInstanceOf[String]
      val newOwner = context.args.parsed[1].asInstanceOf[Player]

      val sender = context.sender

      val region = WorldGuardPlugin.inst().getRegionManager(sender.getWorld).getRegion(regionName)
      if (region == null) {
        IO(s"${regionName}という名前の保護は存在しません。".asMessageEffect())
      }

      IO(attemptRegionTransfer(sender, newOwner, region))
    }
    .build()
    .asNonBlockingTabExecutor()
}