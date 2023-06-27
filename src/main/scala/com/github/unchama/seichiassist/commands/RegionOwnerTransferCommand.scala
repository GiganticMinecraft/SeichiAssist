package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.util.external.WorldGuardWrapper
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import shapeless.{HNil, ::}

object RegionOwnerTransferCommand {
  import com.github.unchama.contextualexecutor.builder.ParserResponse._

  val executor: TabExecutor = playerCommandBuilder
    .thenParse(Parsers.identity)
    .thenParse(recipientName => {
      Bukkit.getPlayer(recipientName) match {
        case recipient: Player => succeedWith(recipient)
        case _                 => failWith(s"${recipientName}というプレイヤーはサーバーに参加したことがありません。")
      }
    })
    .buildWithExecutionF { context =>
      val (regionName :: newOwner :: HNil) = context.args.parsed
      val sender = context.sender

      val region =
        WorldGuardWrapper.findByRegionName(regionName)

      region match {
        case Some(region) =>
          attemptRegionTransfer(sender, newOwner, region.getRegion(regionName))
        case None => IO(MessageEffect(s"${regionName}という名前の保護は存在しません。"))
      }
    }
    .asNonBlockingTabExecutor()

  private def attemptRegionTransfer(
    donner: Player,
    recipient: Player,
    region: ProtectedRegion
  ): IO[TargetedEffect[Player]] = IO {
    val owners = region.getOwners
    val regionWorld = donner.getWorld

    val recipientLimit = WorldGuardWrapper.getMaxRegion(recipient, regionWorld)
    val recipientHas = WorldGuardWrapper.getNumberOfRegions(recipient, regionWorld)

    if (recipientLimit <= recipientHas) {
      MessageEffect(s"相手が保護を上限 ($recipientLimit)まで所持しているため権限を譲渡できません。")
    } else if (owners.contains(WorldGuardPlugin.inst().wrapPlayer(recipient))) {
      MessageEffect("相手がすでにオーナーであるため権限を譲渡できません。")
    } else if (!owners.contains(donner.getUniqueId)) {
      MessageEffect("オーナーではないため権限を譲渡できません。")
    } else if (owners.size() != 1) {
      MessageEffect("オーナーが複数人いるため権限を譲渡できません。")
    } else {
      owners.clear()
      owners.addPlayer(recipient.getUniqueId)

      MessageEffect(s"${recipient.getName}に${region.getId}のオーナー権限を譲渡しました。")
    }
  }
}
