package com.github.unchama.seichiassist.commands

import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import cats.effect.IO
import org.bukkit.command.TabExecutor

/*
* 棒メニューを開くコマンド
* @author KisaragiEffective
*/
object StickMenuCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender
      // TODO: 棒メニューを開くエフェクト
      IO(FirstPage.open)
    }
    .build()
}
