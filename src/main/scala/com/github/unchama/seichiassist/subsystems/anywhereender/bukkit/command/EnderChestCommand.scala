package com.github.unchama.seichiassist.subsystems.anywhereender.bukkit.command

import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.anywhereender.AnywhereEnderChestAPI
import org.bukkit.command.TabExecutor

/**
 * エンダーチェストを開くコマンド
 */
object EnderChestCommand {
  def executor[F[_]: [f[_]] =>> ContextCoercion[f, cats.effect.IO]](
    implicit enderChestAccessApi: AnywhereEnderChestAPI[F]
  ): TabExecutor =
    playerCommandBuilder
      .buildWithEffectAsExecution {
        enderChestAccessApi
          .openEnderChestOrNotifyInsufficientLevel
          .mapK(ContextCoercion.asFunctionK[F, cats.effect.IO])
      }
      .asNonBlockingTabExecutor()
}
