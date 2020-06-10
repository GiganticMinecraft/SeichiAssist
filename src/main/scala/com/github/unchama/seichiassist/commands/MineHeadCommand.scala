package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.{StaticGachaPrizeFactory, Util}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object MineHeadCommand {
  import com.github.unchama.targetedeffect._

  val effect: TargetedEffect[Player] =
    SequentialEffect(
      Util.grantItemStacksEffect(StaticGachaPrizeFactory.getMineHeadItem),
      MessageEffect(s"${GREEN}専用アイテムを付与しました。")
    )

  val executor: TabExecutor = playerCommandBuilder
    .execution { _ => IO.pure(effect) }
    .build()
    .asNonBlockingTabExecutor()
}
