package com.github.unchama.seichiassist.commands.contextual.builder

import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import org.bukkit.entity.Player

object BuilderTemplates {

  val playerCommandBuilder = ContextualExecutorBuilder.beginConfiguration()
      .refineSenderWithError[Player]("${ChatColor.GREEN}このコマンドはゲーム内から実行してください。")

}