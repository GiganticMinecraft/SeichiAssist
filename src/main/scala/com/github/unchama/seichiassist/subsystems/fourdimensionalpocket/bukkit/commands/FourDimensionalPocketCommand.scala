package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit.commands

import cats.effect.Async
import cats.effect.IO
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * プレイヤーが自分の四次元ポケットを開くコマンド StickMenuCommand.scalaを参考に作成
 */
object FourDimensionalPocketCommand {
  def executor[F[_]: Async: [f[_]] =>> ContextCoercion[f, IO]](
    implicit api: FourDimensionalPocketApi[F, Player]
  ): TabExecutor = {
    playerCommandBuilder
      .buildWithExecutionF(context => api.openPocketInventory(context.sender))
      .asNonBlockingTabExecutor()
  }
}
