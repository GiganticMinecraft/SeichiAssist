package com.github.unchama.seichiassist.commands

import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
/*
* 棒メニューを開くコマンド
* @author KisaragiEffective
*/
object StickMenuCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = contxt.sender
      // TODO: 棒メニューを開くエフェクト
      IO(FirstPage.open)
    }
    .build
}
