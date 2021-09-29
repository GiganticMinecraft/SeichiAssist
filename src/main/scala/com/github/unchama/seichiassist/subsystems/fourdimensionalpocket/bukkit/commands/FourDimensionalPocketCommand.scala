package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit.commands

import cats.effect.IO
import org.bukkit.entity.Player
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import com.github.unchama.targetedeffect.UnfocusedEffect
import org.bukkit.command.TabExecutor

/**
 * プレイヤーが自分の四次元ポケットを開くコマンド
 * StickMenuCommand.scalaを参考に作成
 */
object FourDimensionalPocketCommand {
  def executor[F[_]](implicit api: FourDimensionalPocketApi[F, Player]): TabExecutor = {
    playerCommandBuilder
      .execution { context =>
        IO.pure(UnfocusedEffect(api.openPocketInventory(context.sender)))
      }
      .build()
      .asNonBlockingTabExecutor()
  }
}

