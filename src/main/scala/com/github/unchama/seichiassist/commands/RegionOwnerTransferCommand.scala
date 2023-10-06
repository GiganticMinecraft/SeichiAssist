package com.github.unchama.seichiassist.commands

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.TargetedEffectF
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import com.github.unchama.util.external.WorldGuardWrapper
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import shapeless.{::, HNil}

object RegionOwnerTransferCommand {
  import com.github.unchama.contextualexecutor.builder.ParserResponse._

  val executor: TabExecutor = playerCommandBuilder
    .thenParse(Parsers.identity)
    .thenParse { recipientName =>
      Bukkit.getPlayer(recipientName) match {
        case recipient: Player => succeedWith(recipient)
        case _                 => failWith(s"${recipientName}というプレイヤーはサーバーに参加したことがありません。")
      }
    }
    .buildWithExecutionCSEffect { context =>
      val regionName :: newOwner :: HNil = context.args.parsed
      val sender = context.sender

      val region =
        WorldGuardWrapper.findByRegionName(regionName)

      region match {
        case Some(region) =>
          attemptRegionTransfer(sender, newOwner, region.getRegion(regionName))
        case None => MessageEffectF[IO](s"${regionName}という名前の保護は存在しません。")
      }
    }
    .asNonBlockingTabExecutor()

  import cats.implicits._

  private def attemptRegionTransfer(
    donner: Player,
    recipient: Player,
    region: ProtectedRegion
  ): TargetedEffectF[IO, Player] = (for {
    owners <- Kleisli.liftF(IO(region.getOwners))
    regionWorld <- Kleisli.liftF(IO(donner.getWorld))
    recipientLimit <- Kleisli.liftF(IO(WorldGuardWrapper.getMaxRegion(recipient, regionWorld)))
    recipientHas <- Kleisli.liftF(
      IO(WorldGuardWrapper.getNumberOfRegions(recipient, regionWorld))
    )
  } yield {
    if (recipientLimit <= recipientHas) {
      MessageEffectF[IO](s"相手が保護を上限 ($recipientLimit)まで所持しているため権限を譲渡できません。")
    } else if (owners.contains(WorldGuardPlugin.inst().wrapPlayer(recipient))) {
      MessageEffectF[IO]("相手がすでにオーナーであるため権限を譲渡できません。")
    } else if (!owners.contains(donner.getUniqueId)) {
      MessageEffectF[IO]("オーナーではないため権限を譲渡できません。")
    } else if (owners.size() != 1) {
      MessageEffectF[IO]("オーナーが複数人いるため権限を譲渡できません。")
    } else {
      owners.clear()
      owners.addPlayer(recipient.getUniqueId)

      MessageEffectF[IO](s"${recipient.getName}に${region.getId}のオーナー権限を譲渡しました。")
    }
  }).flatten
}
