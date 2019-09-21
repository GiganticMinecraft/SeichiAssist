package com.github.unchama.seichiassist.commands

import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.listener.new_year_event.{NewYearBagListener, NewYearItemListener}
import com.github.unchama.seichiassist.util.Util

object EventCommand {
  val executor = playerCommandBuilder
      .execution { context =>
        if (context.args.yetToBeParsed.firstOrNull() != "get") return@execution EmptyEffect

        val player = context.sender
        if (Util.isPlayerInventoryFull(player)) {
          Util.dropItem(player, NewYearBagListener.newYearBag())
          Util.dropItem(player, NewYearItemListener.newYearApple())
        } else {
          Util.addItem(player, NewYearBagListener.newYearBag())
          Util.addItem(player, NewYearItemListener.newYearApple())
        }

        return@execution EmptyEffect
      }
      .build()
      .asNonBlockingTabExecutor()
}