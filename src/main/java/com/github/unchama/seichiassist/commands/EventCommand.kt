package com.github.unchama.seichiassist.commands

import arrow.core.None
import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.seichiassist.listener.new_year_event.NewYearBagListener
import com.github.unchama.seichiassist.listener.new_year_event.NewYearItemListener
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object EventCommand {
  val executor = ContextualExecutorBuilder.beginConfiguration()
      .refineSenderWithError<Player>("${ChatColor.GREEN}このコマンドはゲーム内から実行してください。")
      .execution { context ->
        if (context.args.yetToBeParsed.firstOrNull() != "get") return@execution None

        val player = context.sender
        if (Util.isPlayerInventoryFull(player)) {
          Util.dropItem(player, NewYearBagListener.getNewYearBag())
          Util.dropItem(player, NewYearItemListener.getNewYearApple())
        } else {
          Util.addItem(player, NewYearBagListener.getNewYearBag())
          Util.addItem(player, NewYearItemListener.getNewYearApple())
        }

        return@execution None
      }
      .build()
      .asNonBlockingTabExecutor()
}