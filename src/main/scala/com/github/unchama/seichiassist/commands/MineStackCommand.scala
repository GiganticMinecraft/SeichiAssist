package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.UnfocusedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.TabExecutor

object MineStackCommand {
  val executor: TabExecutor = playerCommandBuilder
    .argumentsParsers(
      List(
        Parsers.fromOptionParser(
          {
            case "on"  => Some(true)
            case "off" => Some(false)
            case _     => None
          },
          MessageEffect("/minestack <on | off> … MineStackの対象アイテムを自動収集するか切り替えます")
        )
      )
    )
    .execution { context =>
      IO {
        val newState = context.args.parsed.head.asInstanceOf[Boolean]
        val sender = context.sender
        val pd = SeichiAssist.playermap(sender.getUniqueId).settings
        UnfocusedEffect {
          pd.autoMineStack = newState
        }
      }
    }
    .build()
    .asNonBlockingTabExecutor()
}
