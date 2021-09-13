package com.github.unchama.seichiassist.commands

import cats.effect.IO
import org.bukkit.entity.Player
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import com.github.unchama.targetedeffect.UnfocusedEffect
import org.bukkit.command.TabExecutor

/**
 * 四次元ポケットを開くコマンド
 * StickMenuCommand.scalaを参考に作成
 */
object FourDimensionalPocketCommand {
    def executor(implicit api: FourDimensionalPocketApi[IO, Player] ):TabExecutor = {
    playerCommandBuilder
      .execution {player:Player=> IO.pure(UnfocusedEffect(api.openPocketInventory(player:Player)))}
      .build()
      .asNonBlockingTabExecutor()
    }
}

