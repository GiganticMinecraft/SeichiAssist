package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.listener.new_year_event.{NewYearBagListener, NewYearItemListener}
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.{TargetedEffect, emptyEffect}
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object EventCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      def execution(): TargetedEffect[Player] = {
        if (context.args.yetToBeParsed.head != "get") return emptyEffect

        targetedeffect.delay { player: Player =>
          Util.addItemToPlayerSafely(player, NewYearBagListener.getNewYearBag)
          Util.addItemToPlayerSafely(player, NewYearItemListener.getNewYearApple)
        }
      }

      IO(execution())
    }
    .build()
    .asNonBlockingTabExecutor()
}