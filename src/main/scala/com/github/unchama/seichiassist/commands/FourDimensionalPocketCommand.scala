package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.ContextualExecutor
import org.bukkit.entity.Player
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
/**
 * 四次元ポケットを開くコマンド
 * StickMenuCommand.scalaを参考に作成
 */
object FourDimensionalPocketCommand{
　def executor(implicit api: FourDimensionalPocketApi[IO, Player] ):ContextualExecutor =
    playerCommandBuilder
      .execution {player => api.openPocketInventory(player)}
      .build()
      .asNonBlockingTabExecutor()
}

