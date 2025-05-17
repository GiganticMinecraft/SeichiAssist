package com.github.unchama.seichiassist.commands.contextual.builder

import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import shapeless.HNil

object BuilderTemplates {

  def playerCommandBuilder: ContextualExecutorBuilder[Player, HNil] =
    ContextualExecutorBuilder
      .beginConfiguration
      .refineSenderWithError[Player](s"${GREEN}このコマンドはゲーム内から実行してください。")

}
