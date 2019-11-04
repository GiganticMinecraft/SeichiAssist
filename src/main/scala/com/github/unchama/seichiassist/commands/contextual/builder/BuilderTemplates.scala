package com.github.unchama.seichiassist.commands.contextual.builder

import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object BuilderTemplates {

  val playerCommandBuilder: ContextualExecutorBuilder[Player] =
    ContextualExecutorBuilder.beginConfiguration()
      .refineSenderWithError[Player](s"${GREEN}このコマンドはゲーム内から実行してください。")

}