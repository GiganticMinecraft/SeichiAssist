package com.github.unchama.seichiassist.subsystems.anywhereender.bukkit.command

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.anywhereender.AnywhereEnderChestAPI
import org.bukkit.command.TabExecutor

/**
 * エンダーチェストを開くコマンド
 */
object EnderChestCommand {
  def executor(implicit enderChestAccessApi: AnywhereEnderChestAPI[IO]): TabExecutor =
    playerCommandBuilder
      .argumentsParsers(List())
      .execution { _ =>
        IO {
          enderChestAccessApi.openEnderChestOrNotifyInsufficientLevel
        }
      }
      .build()
      .asNonBlockingTabExecutor()
}
